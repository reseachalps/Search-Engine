package com.datapublica.companies.mock;

import com.datapublica.companies.workflow.ExchangeNode;
import com.datapublica.companies.workflow.MessageQueue;
import com.datapublica.companies.workflow.service.*;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import org.apache.commons.lang.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The queue service mocking try to replicate the same behaviour as a real-life
 * service as much as possible.
 * <p>
 * 1) The service uses json-serialization to make sure that we have the right
 * data type transformations 2) The service launches threads to manage new
 * messages (the ensureFired waits for threads terminations)
 */
@Service
public class MockQueueService implements QueueService {
    private static final Logger log = LoggerFactory.getLogger(MockQueueService.class);

    @Autowired(required = false)
    private List<QueueComponent> components = Lists.newArrayList();

    @Autowired(required = false)
    private List<QueueSubscriber> subscribers = Lists.newArrayList();

    private Multimap<MessageQueue, QueueSubscriber> subscriberIdx = HashMultimap.create();

    private Map<MessageQueue, PluginMock> mocks = Maps.newHashMap();
    private ObjectMapper objectMapper = new ObjectMapper();

    private ThreadLocal<Integer> prioThread = new ThreadLocal<>();

    @PostConstruct
    private void init() {
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        for (QueueComponent listener : components) {
            listener.ready(this);
        }

        for (QueueSubscriber listener : subscribers) {
            subscriberIdx.put(listener.getQueue(), listener);
        }
    }

    public synchronized void clearMocks() {
        mocks.clear();
        threads.clear();
    }

    public <E> void putMock(MessageQueue queue, PluginMock<E> mock) {
        mocks.put(queue, mock);
    }

    public <E> PluginFetchMock<E> putFetchMock(MessageQueue queue) {
        PluginFetchMock<E> p = new PluginFetchMock<>(this);
        putMock(queue, p);
        return p;
    }

    @SuppressWarnings("unchecked")
    public void fire(MessageQueue queue, Object dto) {
        for (QueueSubscriber listener : subscriberIdx.get(queue)) {
            if (listener instanceof QueueBulkListener) {
                ((QueueBulkListener) listener).receive(Lists.newArrayList(dto));
            } else if (listener instanceof QueueListener) {
                ((QueueListener) listener).receive(dto);
            } else if (listener instanceof PluginService) {
                // TODO: No routing?
                ((PluginService) listener).receiveAndReply(dto);
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void push(Object dto, MessageQueue messageQueue, MessageQueue messageQueueReply) {
        PluginMock pluginMock = mocks.get(messageQueue);
        if (pluginMock != null) {
            String s = "{could not serialize}";
            try {
                // ensure that mappings are ok by simulating a serialization /
                // deserialization like in real life
                s = objectMapper.writeValueAsString(dto);
                dto = objectMapper.readValue(s, messageQueue.getMessageClass());
            } catch (IOException e) {
                throw new IllegalStateException("Types did not match!\n" + s + "\nFor type " + messageQueue.getMessageClass().getName(), e);
            }
            final Object toSend = dto;
            Thread t = new Thread(() -> pluginMock.fire(toSend, messageQueueReply, this));
            synchronized (this) {
                threads.add(t);
            }
        } else {
            log.warn("Trying to submit a DTO to a queue without mock... ignoring");
        }
    }

    public List<Thread> threads = Lists.newArrayList();

    public void ensureFired() {
        List<Thread> list;
        synchronized (this) {
            list = Lists.newArrayList(threads);
        }
        for (Thread thread : list) {
            try {
                thread.start();
                thread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        synchronized (this) {
            threads.removeAll(list);
            // If new threads were added while joining previous threads, we have
            // to make sure that they finish
            if (threads.size() > 0)
                ensureFired();
        }
    }

    @Override
    public void publish(Object dto, ExchangeNode node) {
        log.warn("Trying to publish a DTO but it's not yet implemented");
        // throw new RuntimeException("Publish is not implemented in the mock
        // queue service");
    }

    @Override
    public <E> E fetch(MessageQueue<E> queue) {
        throw new NotImplementedException("fetchMessage");
    }

    @Override
    public Map<String, QueueStats> queueStats() {
        return new HashMap<>();
        // throw new NotImplementedException("queueStats");
    }

    @Override
    public Map<String, Integer> exchangeStats() {
        return new HashMap<>();
        // throw new NotImplementedException("exchangeStats");
    }

    @Override
    public void purgeQueue(MessageQueue queue) {
        throw new NotImplementedException();
    }

    @Override
    public void pushRaw(String message, MessageQueue queue, MessageQueue replyTo, Integer priority) {
        log.debug("Pushing raw message into queue {}, {}", queue.getName(), message);
        try {
            push(objectMapper.readValue(message, queue.getMessageClass()), queue, replyTo);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void pushRaw(String message, String queue, String replyTo, Integer priority) {
        MessageQueue msgQueue = mocks.keySet().stream().filter(it -> it.getName().compareTo(queue) == 0).findFirst().orElse(null);
        MessageQueue msgReply = null;
        if(replyTo != null) {
            msgReply = mocks.keySet().stream().filter(it -> it.getName().compareTo(replyTo) == 0).findFirst().orElse(null);
        }
        pushRaw(message, msgQueue, msgReply, priority);
    }

    @Override
    public void stop(QueueSubscriber listener) {
        throw new NotImplementedException("stop");
    }

    @Override
    public void resume(QueueSubscriber listener) {
        throw new NotImplementedException("resume");
    }

    @Override
    public void setThreadPriority(Integer priority) {
        prioThread.set(priority);
    }

    @Override
    public Integer getThreadPriority() {
        if (prioThread.get() == null) {
            return 2;
        }
        return prioThread.get();
    }

    public List<QueueSubscriber> getSubscribers() {
        return subscribers;
    }
}
