package com.datapublica.companies.workflow.service.impl;

import com.datapublica.companies.AbstractTest;
import com.datapublica.companies.mock.MockQueueService;
import com.datapublica.companies.mock.PluginFetchMock;
import com.datapublica.companies.mock.PluginMock;
import com.datapublica.companies.mock.RssInput;
import com.datapublica.companies.mock.RssOut;
import com.datapublica.companies.model.scheduler.ExecutionStatus;
import com.datapublica.companies.model.scheduler.ScheduledMessage;
import com.datapublica.companies.model.scheduler.TriggerInfo;
import com.datapublica.companies.repository.mongo.ScheduledMessageRepository;
import com.datapublica.companies.workflow.MessageQueue;
import com.datapublica.companies.workflow.dto.ScheduledJobResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class QueueSchedulerImplTest extends AbstractTest {

    @Autowired
    private QueueSchedulerImpl service;

    @Autowired
    private ScheduledMessageRepository repository;
    @Autowired
    private MockQueueService queueService;

    private static final MessageQueue rssFetcherMockQueue = MessageQueue.get("RSS_FETCHER", RssInput.class);

    private Date currentDate;
    private static final MessageQueue<RssOut> fakeQueue = MessageQueue.get("MEDIA_RSS_OUT", RssOut.class);
    private static final MessageQueue<Object> signalFakeQueue = MessageQueue.get("SIGNAL_CATEGORIZER_OUT", Object.class);

    private class FakeRssPlugin implements PluginMock<RssInput> {
        private final String messageId;
        private final Date executionTime;
        private RssOut outDTO;
        private MessageQueue replyTo;
        private boolean fired;

        private FakeRssPlugin(String messageId, Date executionTime) {
            this.messageId = messageId;
            this.executionTime = executionTime;
        }

        @Override
        public void fire(RssInput dto, MessageQueue replyTo, MockQueueService mql) {
            this.replyTo = replyTo;
            fired = true;
            RssOut.Body body = new RssOut.Body();
            body.title = "Hiyo!";
            outDTO = new RssOut(dto, body, executionTime);

            assertEquals(messageId, dto.id);
            assertEquals(executionTime, dto.timestamp);
            assertEquals(service.getQueue(), replyTo);
        }

        @Override
        public Object apply(RssInput rssFetcherInDTO) {
            assert fired;
            queueService.push(outDTO, replyTo, null);
            return outDTO;
        }
    }

    @Before
    @After
    public void clean() {
        repository.deleteAll();
        queueService.clearMocks();
        currentDate = new Date();
        queueService.<ScheduledJobResponse>putMock(service.getQueue(), dto -> {
            service.process(dto, currentDate);
            return null;
        });
        queueService.putFetchMock(fakeQueue);
        queueService.putFetchMock(signalFakeQueue);
        queueService.putFetchMock(rssFetcherMockQueue);

    }

    @Test
    public void testSimpleWorkflow() {
        String provider = "signal";
        String id = "RSS:123";

        Date start = new Date();
        Date scheduled = new Date(start.getTime()+ 1000*10); // add 10s
        RssInput.Input<String> jobMessage = new RssInput.Input<>("http://blabla/feed", id);

        // Schedule the job
        ScheduledMessage message = service.scheduleJob(provider, id, jobMessage, new TriggerInfo(30, 10),
                rssFetcherMockQueue,fakeQueue, null);

        final String messageId = message.getId();

        // Make sure that the message is in the right state
        assertNotNull(message);
        assertEquals(ExecutionStatus.PLANNED, message.getStatus());
        assertNull(message.getLastExecution().lastActualExecutionTime);
        assertNotNull(message.getNextExecution());
        Date maxScheduled = new Date(new Date().getTime()+ 1000*10); // add 10s
        // the schedule is somewhere between start + 10s and now + 10s
        assertTrue(message.getNextExecution().compareTo(scheduled) >= 0);
        assertTrue(message.getNextExecution().compareTo(maxScheduled) <= 0);

        // setup the execution time 1s after the planned execution time
        final Date executionTime = new Date(message.getNextExecution().getTime()+1000);

        FakeRssPlugin mockPlugin = new FakeRssPlugin(messageId, executionTime);

        queueService.putMock(rssFetcherMockQueue, mockPlugin);
        PluginFetchMock<RssOut> outputMock = queueService.putFetchMock(fakeQueue);

        // There is a current job in the queue
        assertEquals(1, repository.findAllForExecution(executionTime).count());
        // But it has not been fired
        assertFalse(mockPlugin.fired);

        // Go scheduling at the start time (should not have any impact)
        service.schedule(start);
        queueService.ensureFired();

        // No changes
        assertEquals(1, repository.findAllForExecution(executionTime).count());
        assertFalse(mockPlugin.fired);

        // Go scheduling after the
        service.schedule(executionTime);
        queueService.ensureFired();

        // No other scheduled job is ready
        assertEquals(0, repository.findAllForExecution(executionTime).count());

        // fetch latest status
        message = repository.findOne(message.getId());

        // The job is indeed sent
        assertTrue(mockPlugin.fired);
        assertEquals(ExecutionStatus.SUBMITTED, message.getStatus());
        assertEquals(executionTime, message.getLastExecution().lastActualExecutionTime);

        // still havent seen the output
        assertTrue(outputMock.isEmpty());

        // apply the reply 1s later than the execution time
        currentDate = new Date(executionTime.getTime()+1000);
        mockPlugin.apply(null);

        // Now we have seen the
        assertFalse(outputMock.isEmpty());

        // OK! We've got the output from the plugin :)
        assertEquals("Hiyo!", outputMock.getDTO().body.title);

        // Let's check the db status now
        message = repository.findOne(message.getId());
        // Make sure that the message is in the right state
        assertNotNull(message);
        assertEquals(ExecutionStatus.PLANNED, message.getStatus());
        assertEquals(currentDate, message.getLastExecution().lastCompletionTime);
        assertNotNull(message.getNextExecution());
        assertEquals(new Date(message.getLastExecution().lastCompletionTime.getTime()+30*1000), message.getNextExecution());
    }

    @Test
    public void testReschedule() {

        String provider = "signal";
        String id = "RSS:123";
        RssInput.Input<String> jobMessage = new RssInput.Input<>("http://blabla/feed", id);

        // Schedule the job
        assertEquals(0, repository.count());
        ScheduledMessage message = service.scheduleJob(provider, id, jobMessage, new TriggerInfo(30, 10),
                rssFetcherMockQueue, fakeQueue, null);
        // routine check
        assertEquals(1, repository.count());
        Date nextExecution = message.getNextExecution();

        Date start;
        // Re-schedule the same job
        message = service.scheduleJob(provider, id, jobMessage, new TriggerInfo(30, 10),
                rssFetcherMockQueue, fakeQueue, null);

        // we do not add anything
        assertEquals(1, repository.count());
        // the schedule is still the same
        assertEquals(nextExecution, message.getNextExecution());

        nextExecution = message.getNextExecution();
        service.schedule(nextExecution);
        queueService.ensureFired();

        message = repository.findOne(message.getId());
        assertEquals(ExecutionStatus.SUBMITTED, message.getStatus());
        // Re-schedule the same job with status SUBMITTED, but reroute RSS_FETCHER and change trigger
        message = service.scheduleJob(provider, id, jobMessage, new TriggerInfo(60, 20),
                rssFetcherMockQueue, signalFakeQueue, null);

        assertEquals(ExecutionStatus.SUBMITTED, message.getStatus());
        assertEquals(nextExecution, message.getNextExecution());
        assertEquals(60, message.getTriggerInfo().period);
        assertEquals(signalFakeQueue.getName(), message.getReplyTo());


        message.setStatus(ExecutionStatus.CANCELLED);
        repository.save(message);

        message = service.scheduleJob(provider, id, jobMessage, new TriggerInfo(30, 10),
                rssFetcherMockQueue, fakeQueue, null);

        assertEquals(ExecutionStatus.PLANNED, message.getStatus());
        assertEquals(nextExecution, message.getNextExecution());
        assertEquals(30, message.getTriggerInfo().period);
        assertEquals(fakeQueue.getName(), message.getReplyTo());

        message.setStatus(ExecutionStatus.ERROR);
        repository.save(message);

        start = new Date();
        message = service.scheduleJob(provider, id, jobMessage, new TriggerInfo(60, 20),
                rssFetcherMockQueue, signalFakeQueue, null);

        assertEquals(ExecutionStatus.PLANNED, message.getStatus());
        assertNotEquals(nextExecution, message.getNextExecution());
        assertTrue(message.getNextExecution().compareTo(new Date(start.getTime() + 1000 * 20)) >= 0);
        assertTrue(message.getNextExecution().compareTo(new Date(new Date().getTime() + 1000 * 20)) <= 0);
        assertEquals(60, message.getTriggerInfo().period);
        assertEquals(signalFakeQueue.getName(), message.getReplyTo());
    }


    @Test
    public void testCancel() {
        String provider = "signal";
        String id = "RSS:123";
        RssInput.Input<String> jobMessage = new RssInput.Input<>("http://blabla/feed", id);
        PluginFetchMock<RssInput> inFetcher = queueService.putFetchMock(rssFetcherMockQueue);
        PluginFetchMock<RssOut> outFetcher = queueService.putFetchMock(fakeQueue);

        ScheduledMessage message = service.scheduleJob(provider, id, jobMessage, new TriggerInfo(30, 10),
                rssFetcherMockQueue, fakeQueue, null);

        // cancel an unknown job
        assertFalse(service.cancelJob(message.getId() + "/"));

        queueService.ensureFired();
        message = repository.findOne(message.getId());
        assertEquals(ExecutionStatus.PLANNED, message.getStatus());

        // cancel a planned job
        assertTrue(service.cancelJob(message.getId()));
        assertNull(repository.findOne(message.getId()));

        message = service.scheduleJob(provider, id, jobMessage, new TriggerInfo(30, 10),
                rssFetcherMockQueue, fakeQueue, null);
        service.schedule(message.getNextExecution());
        queueService.ensureFired();
        message = repository.findOne(message.getId());
        assertEquals(ExecutionStatus.SUBMITTED, message.getStatus());

        // cancel a submitted job
        assertTrue(service.cancelJob(message.getId()));
        message = repository.findOne(message.getId());
        assertEquals(ExecutionStatus.CANCELLED, message.getStatus());

        // cancel a cancelled job
        assertTrue(service.cancelJob(message.getId()));
        message = repository.findOne(message.getId());
        assertEquals(ExecutionStatus.CANCELLED, message.getStatus());

        RssInput dto = inFetcher.getDTO();
        assertNotNull(dto);
        queueService.push(new RssOut(dto, new RssOut.Body(), new Date()), service.getQueue(), null);
        queueService.ensureFired();
        assertNull(repository.findOne(message.getId()));
        assertNull(outFetcher.getDTO());
    }


    @Test
    public void testAdjust() {
        String provider = "signal";
        String id = "RSS:123";
        RssInput.Input<String> jobMessage = new RssInput.Input<>("http://blabla/feed", id);

        ScheduledMessage message = service.scheduleJob(provider, id, jobMessage, new TriggerInfo(30, 10),
                rssFetcherMockQueue, fakeQueue, null);

        Date date = new Date();
        service.adjustNextExecution(message.getId(), date);
        message = repository.findOne(message.getId());
        assertEquals(date, message.getNextExecution());

        message.setStatus(ExecutionStatus.ERROR);
        repository.save(message);
        date = new Date();
        service.adjustNextExecution(message.getId(), date);
        message = repository.findOne(message.getId());
        assertEquals(ExecutionStatus.PLANNED, message.getStatus());
        assertEquals(date, message.getNextExecution());
    }

    @Test
    public void testEmptyReplyTo() throws JsonProcessingException {
        String provider = "signal";
        String id = "RSS:123";

        Date start = new Date();
        RssInput.Input<String> jobMessage = new RssInput.Input<>("http://blabla/feed", id);

        // Schedule the job
        ScheduledMessage message = service.scheduleJob(provider, id, jobMessage, new TriggerInfo(30, 0),
                rssFetcherMockQueue, null, null);

        final String messageId = message.getId();

        Date executionTime = new Date(start.getTime() + 1000);
        FakeRssPlugin mockPlugin = new FakeRssPlugin(messageId, executionTime);

        queueService.putMock(rssFetcherMockQueue, mockPlugin);
        PluginFetchMock<RssOut> outputMock = queueService.putFetchMock(fakeQueue);

        service.schedule(executionTime);
        queueService.ensureFired();
        mockPlugin.apply(null);

        // It has not output at all
        assertTrue(outputMock.isEmpty());

        // Let's check the db status now
        message = repository.findOne(message.getId());

        // Make sure that the message is in the right state
        assertNotNull(message);
        assertEquals(ExecutionStatus.PLANNED, message.getStatus());
        // Status has been updated
        assertEquals(new ObjectMapper().writeValueAsString(executionTime), message.getLastExecution().status);
    }

    @Test
    public void testReplyReschedule() throws JsonProcessingException {
        String provider = "signal";
        String id = "RSS:123";

        Date start = new Date();
        RssInput.Input<String> jobMessage = new RssInput.Input<>("http://blabla/feed", id);

        // Schedule the job
        ScheduledMessage message = service.scheduleJob(provider, id, jobMessage, new TriggerInfo(30, 0),
                rssFetcherMockQueue, null, null);

        PluginFetchMock<RssInput> inputMock = queueService.putFetchMock(rssFetcherMockQueue);
        PluginFetchMock<RssOut> outputMock = queueService.putFetchMock(fakeQueue);

        service.schedule(new Date());
        queueService.ensureFired();

        // It has not output at all
        assertTrue(!inputMock.isEmpty());
        assertTrue(outputMock.isEmpty());

        RssInput dto = inputMock.getDTO();

        RssOut.Body body = new RssOut.Body();
        body.title = "Hiyo!";
        Date status = new Date();
        RssOut out = new RssOut(dto, body, status);
        out.reschedule = true;
        inputMock.clear();

        // Let's check the db status now
        message = repository.findOne(message.getId());
        // Make sure that the message is in the right state
        assertNotNull(message);
        assertEquals(ExecutionStatus.SUBMITTED, message.getStatus());

        queueService.push(out, QueueSchedulerImpl.QUEUE, null);
        queueService.ensureFired();

        // Let's check the db status now
        message = repository.findOne(message.getId());
        assertNotNull(message);
        // we've stored the data
        assertEquals(String.valueOf(status.getTime()), message.getLastExecution().status);
        // Still submitted because it has been resent
        assertEquals(ExecutionStatus.SUBMITTED, message.getStatus());

        // DTO is there!
        assertTrue(!inputMock.isEmpty());
        // And it is up to date
        assertEquals(String.valueOf(status.getTime()), inputMock.getDTO().status);
    }
}