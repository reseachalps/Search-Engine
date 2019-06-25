package com.datapublica.companies.workflow.service.impl;

import com.datapublica.companies.model.scheduler.ExecutionStatus;
import com.datapublica.companies.model.scheduler.LastExecution;
import com.datapublica.companies.model.scheduler.ScheduledMessage;
import com.datapublica.companies.model.scheduler.TriggerInfo;
import com.datapublica.companies.model.scheduler.TriggerType;
import com.datapublica.companies.repository.mongo.ScheduledMessageRepository;
import com.datapublica.companies.workflow.MessageQueue;
import com.datapublica.companies.workflow.dto.ScheduledJobResponse;
import com.datapublica.companies.workflow.service.QueueComponent;
import com.datapublica.companies.workflow.service.QueueListener;
import com.datapublica.companies.workflow.service.QueueService;
import com.datapublica.companies.workflow.service.scheduler.QueueScheduler;
import com.datapublica.companies.workflow.service.scheduler.ScheduledJobInput;
import com.datapublica.companies.workflow.service.scheduler.ScheduledJobMessage;
import com.datapublica.companies.workflow.service.scheduler.ScheduledJobOutput;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.Trigger;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

/**
 *
 */
@Service
@Profile("!queue-push")
public class QueueSchedulerImpl extends QueueComponent implements QueueScheduler, QueueListener<ScheduledJobMessage> {
    public static final MessageQueue<ScheduledJobMessage> QUEUE = MessageQueue.get("SCHEDULER", ScheduledJobResponse.class, ScheduledJobMessage.class);

    private static final Logger log = LoggerFactory.getLogger(QueueSchedulerImpl.class);
    private ObjectMapper mapper = new ObjectMapper();

    @Value("${scheduler.active:true}")
    private boolean active;

    private QueueService service;

    @Autowired
    private ScheduledMessageRepository repository;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public synchronized <E> ScheduledMessage scheduleJob(String provider, String id, E message, TriggerInfo trigger, MessageQueue queue, MessageQueue replyTo, Integer priority) {
        id = ScheduledMessage.idFromProvider(provider, id);
        ScheduledMessage scheduled = repository.findOne(id);
        if (scheduled == null) {
            scheduled = new ScheduledMessage();
        }
        scheduled.setProvider(provider);
        scheduled.setId(id);
        scheduled.setTriggerInfo(trigger);
        scheduled.setQueue(queue);
        scheduled.setReplyTo(replyTo);
        scheduled.setPriority(priority);

        if (scheduled.getLastExecution() == null) {
            scheduled.setLastExecution(new LastExecution());
        }

        try {
            scheduled.setMessage(mapper.writeValueAsString(message));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Could not generate json from message", e);
        }

        ExecutionStatus status = scheduled.getStatus();
        if (status == null || ExecutionStatus.ERROR.equals(status)) {
            // reset schedule trigger
            scheduled.getLastExecution().lastScheduledExecutionTime = null;
            scheduled.setStatus(ExecutionStatus.PLANNED);
            Date date = computeNextExecution(scheduled);
            scheduled.setNextExecution(date);
        } else if (ExecutionStatus.CANCELLED.equals(status)) {
            scheduled.setStatus(ExecutionStatus.PLANNED);
        }
        repository.save(scheduled);
        return scheduled;
    }

    @Override
    public void ready(QueueService service) {
        this.service = service;
        if (active) {
            ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1, new ThreadPoolExecutor.DiscardPolicy());
            executor.scheduleAtFixedRate(this::schedule, 0, 1, TimeUnit.MINUTES);
        }
    }

    public void schedule() {
        try {
            schedule(new Date());
        } catch (Exception e) {
            log.error("Error while executing the scheduler", e);
        }
    }

    public synchronized void schedule(Date d) {
        log.debug("Schedule task running {}", d);
        long c = repository.findAllForExecution(d).filter(message -> {
            boolean success = sendScheduledMessage(d, message);
            repository.save(message);
            return success;
        }).count();
        if (c > 0) {
            log.debug("Scheduled {} messages", c);
        }
    }

    protected boolean sendScheduledMessage(Date d, ScheduledMessage message) {
        boolean success = true;
        try {
            log.debug("Launching scheduled job {}", message.getId());
            ScheduledJobMessage<JsonNode, JsonNode> dto = new ScheduledJobInput<>(message.getId(), d, message.getLastExecution().lastActualExecutionTime, objectMapper.readTree(message.getMessage()), message.getLastExecution().status == null ? null : objectMapper.readTree(message.getLastExecution().status));
            service.pushRaw(objectMapper.writeValueAsString(dto), message.getQueue(), this.getQueue().getName(), message.getPriority());
            message.getLastExecution().lastActualExecutionTime = d;
            message.getLastExecution().lastScheduledExecutionTime = message.getNextExecution();
            message.getLastExecution().lastCompletionTime = null;
            message.setStatus(ExecutionStatus.SUBMITTED);
        } catch (Exception e) {
            message.setStatus(ExecutionStatus.ERROR);
            log.error("Could not send scheduled message", e);
            success = false;
        }
        return success;
    }

    private Date computeNextExecution(ScheduledMessage message) {
        if (TriggerType.CUSTOM.equals(message.getTriggerInfo().type)) {
            return null;
        }
        Trigger trigger = message.getTriggerInfo().type.getTrigger(message.getTriggerInfo());
        return trigger.nextExecutionTime(message.getLastExecution().asContext());
    }

    @Override
    public synchronized boolean cancelJob(String id) {
        ScheduledMessage scheduled = repository.findOne(id);
        if (scheduled == null)
            return false;
        switch (scheduled.getStatus()) {
            case PLANNED:
            case ERROR:
                repository.delete(scheduled);
                break;
            case SUBMITTED:
            case CANCELLED:
                scheduled.setStatus(ExecutionStatus.CANCELLED);
                repository.save(scheduled);
                break;
        }
        return true;
    }

    @Override
    public synchronized void adjustNextExecution(String id, Date nextDate) {
        ScheduledMessage storedMessage = repository.findOne(id);
        if (ExecutionStatus.ERROR.equals(storedMessage.getStatus())) {
            storedMessage.setStatus(ExecutionStatus.PLANNED);
        }
        storedMessage.setNextExecution(nextDate);
        repository.save(storedMessage);
    }

    @Override
    public Stream<String> listJobs(String provider) {
        return repository.findAllFromProvider(provider);
    }

    protected synchronized void process(ScheduledJobResponse message, Date currentDate) {
        ScheduledMessage storedMessage = repository.findOne(message.id);
        if (storedMessage == null) {
            // Already cancelled message
            log.warn("Unknown message " + message.id + ", ignoring");
            return;
        }
        if (ExecutionStatus.CANCELLED.equals(storedMessage.getStatus())) {
            repository.delete(storedMessage.getId());
        } else {
            storedMessage.setStatus(ExecutionStatus.PLANNED);
            storedMessage.getLastExecution().lastCompletionTime = currentDate;
            storedMessage.setNextExecution(computeNextExecution(storedMessage));
            String replyTo = storedMessage.getReplyTo();
            try {
                storedMessage.getLastExecution().status = objectMapper.writeValueAsString(message.status);
                // Supports empty replyTo entries (if the scheduled job is an
                // endpoint)
                if (replyTo != null) {
                    service.pushRaw(objectMapper.writeValueAsString(message), replyTo, null, storedMessage.getPriority());
                }
            } catch (JsonProcessingException e) {
                throw new IllegalStateException(e);
            }
            if (message.reschedule) {
                storedMessage.setNextExecution(currentDate);
                sendScheduledMessage(storedMessage.getNextExecution(), storedMessage);
            }
            repository.save(storedMessage);
        }
    }

    @Override
    public void receive(ScheduledJobMessage message) {
        this.process((ScheduledJobResponse) message, new Date());
    }

    @Override
    public MessageQueue<ScheduledJobMessage> getQueue() {
        return QUEUE;
    }

    @Override
    public int getConcurrentConsumers() {
        return active ? 1 : 0;
    }
}
