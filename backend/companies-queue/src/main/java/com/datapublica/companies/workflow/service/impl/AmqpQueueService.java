package com.datapublica.companies.workflow.service.impl;

import com.datapublica.companies.config.RabbitConfiguration;
import com.datapublica.companies.model.error.ErrorMessage;
import com.datapublica.companies.util.NormalizeText;
import com.datapublica.companies.workflow.ExchangeNode;
import com.datapublica.companies.workflow.MessageQueue;
import com.datapublica.companies.workflow.service.ErrorHandler;
import com.datapublica.companies.workflow.service.PluginService;
import com.datapublica.companies.workflow.service.QueueBulkListener;
import com.datapublica.companies.workflow.service.QueueComponent;
import com.datapublica.companies.workflow.service.QueueListener;
import com.datapublica.companies.workflow.service.QueueService;
import com.datapublica.companies.workflow.service.QueueStats;
import com.datapublica.companies.workflow.service.QueueSubscriber;
import org.springframework.amqp.rabbit.listener.BufferMessageContainer;
import org.springframework.amqp.rabbit.listener.MultiMessage;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.util.concurrent.RateLimiter;
import com.rabbitmq.client.AMQP;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.AbstractMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 */
@Profile("!test")
@Service
@Order(2)
public class AmqpQueueService implements QueueService {
    public static final int MAX_PRIORITY = 3;

    public static final String MESSAGE_ENCODING = "UTF-8";
    // 1 minute
    private static final long STATS_ROLLING_TIME = 60 * 1000;

    private static final Logger log = LoggerFactory.getLogger(AmqpQueueService.class);

    // Queue throttler
    private RateLimiter sendThrottler;

    @Autowired
    private ConnectionFactory cf;

    @Autowired
    private AmqpAdmin amqpAdmin;

    @Autowired(required = false)
    private ErrorHandler errorHandler;

    @Autowired
    private RabbitTemplate amqpTemplate;

    @Autowired
    private RabbitConfiguration configuration;

    private ObjectMapper mapper = new ObjectMapper();

    @Autowired(required = false)
    private List<QueueSubscriber> subscribers = Lists.newArrayList();

    // Makes sure all queue components are correctly loaded
    @Autowired(required = false)
    private List<QueueComponent> components = Lists.newArrayList();

    private Collection<ExchangeNode<?>> exchangeNodes;

    /**
     * The level of priority is thread-local. So if a thread sets its priority to 1 others threads are not affected by
     * this change.
     */
    private ThreadLocal<Integer> threadLevelPriority = new ThreadLocal<>();

    private Map<QueueSubscriber, AbstractMessageListenerContainer> containers = Maps.newHashMap();
    private Map<MessageQueue, RollingWindow> inStats = new HashMap<>();
    private Map<MessageQueue, RollingWindow> outStats = new HashMap<>();
    private Map<ExchangeNode, RollingWindow> nodeStats = new HashMap<>();
    private Map<String, MessageQueue> messageQueuesMap;

    public ObjectMapper getMapper() {
        return mapper;
    }

    @PostConstruct
    private void init() {
        sendThrottler = RateLimiter.create(configuration.getDefaultThrottle());
        exchangeNodes = ExchangeNode.all();

        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        if (!CollectionUtils.isEmpty(exchangeNodes)) {
            for (ExchangeNode node : exchangeNodes) {
                nodeStats.put(node, new RollingWindow(STATS_ROLLING_TIME));
                amqpAdmin.declareExchange(new FanoutExchange(node.getName()));
            }
        }

        messageQueuesMap = MessageQueue.all().stream().collect(Collectors.toMap(MessageQueue::getName, it -> it));
        messageQueuesMap.values().forEach(this::declareQueue);
        // Try to found out why we need such hack
        subscribers.stream()
                .filter(listener -> listener.getConcurrentConsumers() > 0)
                .filter(listener -> messageQueuesMap.get(listener.getQueue().getName()) == null)
                .map(QueueSubscriber::getQueue)
                .forEach(queue -> {
                    log.warn("Strange state, QUEUE : " + queue.getName() + " not present in MessageQueue static list");
                    this.declareQueue(queue);
                    messageQueuesMap.put(queue.getName(), queue);
                });
        registerListeners();
    }

    private void declareQueue(MessageQueue q) {
        inStats.put(q, new RollingWindow(STATS_ROLLING_TIME));
        outStats.put(q, new RollingWindow(STATS_ROLLING_TIME));
        amqpAdmin.declareQueue(new Queue(q.getName(), true, false, false, ImmutableMap.of("x-max-priority", MAX_PRIORITY)));
        ExchangeNode exchange = q.getExchangeNode();
        if (exchange != null) {
            log.info("Binding queue " + q.getName() + " to exchange " + exchange.getName());
            amqpAdmin.declareBinding(new Binding(q.getName(), Binding.DestinationType.QUEUE, exchange.getName(), "*",
                    null));
        }
    }

    private void registerListeners() {
        Multimap<MessageQueue, String> registry = HashMultimap.create();
        List<AbstractMessageListenerContainer> containers = new ArrayList<>(subscribers.size());
        // Create the containers
        for (final QueueSubscriber listener : subscribers) {
            if (listener.getConcurrentConsumers() <= 0) continue;

            final MessageQueue messageQueue = listener.getQueue();

            registry.put(messageQueue, listener.getClass().getSimpleName());
            AbstractMessageListenerContainer container = register(message -> {
                try {
                    RollingWindow window = outStats.get(messageQueue);
                    window.put();
                    // Add multi messages to the out statistics
                    if (message instanceof MultiMessage) {
                        int messages = ((MultiMessage) message).getMessages().size();
                        // i=1 because we already did one put()
                        for (int i = 1; i < messages; i++) {
                            window.put();
                        }
                    }

                    // If a priority is set, then set the current thread with the given priority
                    Integer priority = message.getMessageProperties().getPriority();
                    if (priority != null)
                        threadLevelPriority.set(priority);

                    log.trace("Received a new message in queue " + messageQueue.getName() + ", priority: " + priority);
                    String replyTo = message.getMessageProperties().getReplyTo();
                    MessageQueue queueReplyTo = null;
                    if (listener instanceof PluginService) {
                        if (replyTo != null) {
                            queueReplyTo = messageQueuesMap.get(replyTo);
                        }

                        Object parsed = parseSingleMessage(message, messageQueue.getMessageClass());
                        //noinspection unchecked
                        Object result = ((PluginService) listener).receiveAndReply(parsed);
                        if (queueReplyTo != null) {
                            //noinspection unchecked
                            push(result, queueReplyTo, null);
                        } else {
                            log.warn("Received an unknown reply-to queue name: " + replyTo);
                        }
                    } else if (listener instanceof QueueListener) {
                        Object parsed = parseSingleMessage(message, messageQueue.getMessageClass());
                        //noinspection unchecked
                        ((QueueListener) listener).receive(parsed);
                    } else if (listener instanceof QueueBulkListener) {
                        if (!(message instanceof MultiMessage)) {
                            throw new IllegalStateException("Should never happen: driver MUST resolve a bulk as a multimessage");
                        }
                        List parsed = parseMultiMessage((MultiMessage) message, messageQueue);
                        //noinspection unchecked
                        ((QueueBulkListener) listener).receive(parsed);
                    }
                } catch (Throwable e) {
                    handleFaultyMessage(messageQueue, message, e);
                } finally {
                    // Remove all priority information if available
                    threadLevelPriority.remove();
                }
            }, messageQueue, listener);
            this.containers.put(listener, container);
            containers.add(container);
        }

        for (Map.Entry<MessageQueue, Collection<String>> entry : registry.asMap().entrySet()) {
            if (entry.getValue().size() > 1)
                log.warn("Registered more than one listener (" + entry.getValue().size() + ") for queue "
                        + entry.getKey() + ": " + entry.getValue());
            else
                log.info("Registered " + entry.getValue().size() + " listener(s) for queue " + entry.getKey() + ": "
                        + entry.getValue());
        }

        // Make the components ready
        components.forEach(it -> it.ready(this));

        // Start all the containers at once
        containers.forEach(AbstractMessageListenerContainer::start);
    }

    private void handleFaultyMessage(MessageQueue messageQueue, Message message, Throwable t) {
        if (message instanceof MultiMessage) {
            ((MultiMessage) message).getMessages().forEach(it -> handleFaultyMessage(messageQueue, it, t));
        } else {
            handleSingleFaultyMessage(messageQueue, message, t);
        }
    }

    private void handleSingleFaultyMessage(MessageQueue messageQueue, Message message, Throwable t) {
        String msgText;
        // Try with correct encoding
        try {
            msgText = new String(message.getBody(), MESSAGE_ENCODING);
        } catch (UnsupportedEncodingException e1) {
            msgText = null;
        }
        log.error("[" + messageQueue + "] Exception while receiving the message: " + (msgText == null ? "*hidden because not UTF-8*" : null), t);

        boolean recover;
        String stackTrace;
        if (msgText == null) {
            recover = false;
            msgText = NormalizeText.readableBadlyEncodedString(message.getBody());
            log.warn("It is impossible to requeue correctly a content which is badly encoded: " + msgText);
            stackTrace = "This string is not encoded with UTF-8 (should NEVER happen)";
        } else {
            recover = true;
            stackTrace = ExceptionUtils.getStackTrace(t);
        }
        Integer priority = message.getMessageProperties().getPriority();
        MessageQueue replyTo = null;
        try {
            String replyToStr = message.getMessageProperties().getReplyTo();
            if (replyToStr != null)
                replyTo = messageQueuesMap.get(replyToStr);
        } catch (Exception ignored) {
            // Invalid replyTo
            log.warn("Impossible to parse reply_to entry in message " + message.getMessageProperties().getReplyTo() + " setting it to null");
        }
        errorHandler.handle(new ErrorMessage(messageQueue.getName(), msgText, replyTo != null ? replyTo.getName() : null, stackTrace, recover, priority));
    }

    private <E> List<E> parseMultiMessage(MultiMessage m, MessageQueue<E> messageQueue) {
        Class<? extends E> messageClass = messageQueue.getMessageClass();
        return m.getMessages().stream().map(it -> {
            try {
                return (E) parseSingleMessage(it, messageClass);
            } catch (Throwable t) {
                handleFaultyMessage(messageQueue, it, t);
            }
            return null;
        }).filter(it -> it != null).collect(Collectors.toList());
    }

    private <E> E parseSingleMessage(Message m, Class<E> clazz) {
        String msgText = null;
        try {
            msgText = new String(m.getBody(), MESSAGE_ENCODING);
            return mapper.readValue(msgText, clazz);
        } catch (Exception e) {
            throw new IllegalStateException("Cannot parse message " + msgText, e);
        }
    }

    @Override
    public <E> E fetch(MessageQueue<E> queue) {
        Message msg = amqpTemplate.receive(queue.getName());
        if (msg == null)
            return null;
        try {
            return parseSingleMessage(msg, queue.getMessageClass());
        } catch (Exception e) {
            handleFaultyMessage(queue, msg, e);
            return null;
        }
    }

    private AbstractMessageListenerContainer register(MessageListener msg, MessageQueue messageQueue, QueueSubscriber concurrent) {

        AbstractMessageListenerContainer container;
        if (concurrent instanceof QueueBulkListener) {
            Integer bulk = ((QueueBulkListener) concurrent).getBulkSize();
            BufferMessageContainer simple = new BufferMessageContainer(cf);
            simple.setConcurrentConsumers(concurrent.getConcurrentConsumers());
            Long timeout = concurrent.receiveTimeout();
            if (timeout != null) {
                simple.setReceiveTimeout(timeout);
            }
            simple.setTxSize(bulk);
            simple.setPrefetchCount(bulk);
            container = simple;
        } else {
            SimpleMessageListenerContainer simple = new SimpleMessageListenerContainer(cf);
            simple.setConcurrentConsumers(concurrent.getConcurrentConsumers());
            Long timeout = concurrent.receiveTimeout();
            if (timeout != null) {
                simple.setReceiveTimeout(timeout);
            }
            container = simple;
        }
        MessageListenerAdapter adapter = new MessageListenerAdapter(msg);
        container.setMessageListener(adapter);
        container.setQueueNames(messageQueue.getName());
        container.start();
        return container;
    }

    @Override
    public <DTO> void push(final DTO dto, final MessageQueue<DTO> messageQueue, MessageQueue messageQueueReply) {
        inStats.get(messageQueue).put();
        doSend(dto, messageQueue.getName(), messageQueue.getCheckClass(), messageQueueReply, true);
    }

    @Override
    public void setThreadPriority(Integer priority) {
        if (priority == null) {
            threadLevelPriority.remove();
            return;
        }
        threadLevelPriority.set(priority);
    }

    public Integer getThreadPriority() {
        return threadLevelPriority.get();
    }

    public void pushRaw(final String message, final MessageQueue messageQueue, MessageQueue messageQueueReply, Integer priority) {
        String exchange = messageQueue.getName();
        log.trace("Sending message in exchange " + exchange);
        try {
            Message m = createMessage(message, messageQueueReply, priority);
            amqpTemplate.send(exchange, m);
        } catch (AmqpException | IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public void pushRaw(final String message, final String messageQueue, String messageQueueReply, Integer priority) {
        log.trace("Sending message in exchange " + messageQueue);
        try {
            Message m = createMessage(message, messageQueueReply, priority);
            amqpTemplate.send(messageQueue, m);
        } catch (AmqpException | IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void stop(QueueSubscriber listener) {
        if (listener == null)
            throw new IllegalArgumentException("Null listener");
        AbstractMessageListenerContainer container = containers.get(listener);
        if (container == null)
            throw new IllegalArgumentException("Unknown listener");
        if (container.isActive())
            container.stop();
    }

    @Override
    public void resume(QueueSubscriber listener) {
        if (listener == null)
            throw new IllegalArgumentException("Null queue listener");
        AbstractMessageListenerContainer container = containers.get(listener);
        if (container == null)
            throw new IllegalArgumentException("Unknown queue listener");
        if (!container.isActive())
            container.start();
    }

    public List<QueueSubscriber> getSubscribers() {
        return ImmutableList.copyOf(subscribers);
    }

    private void doSend(Object dto, String exchange, Class<?> messageClass, MessageQueue messageQueueReply,
                        boolean direct) {
        if (!messageClass.isInstance(dto)) {
            throw new IllegalStateException("Trying to send into " + exchange + " a object of class "
                    + dto.getClass().getName());
        }
        try {
            String message = mapper.writeValueAsString(dto);
            log.trace("Sending message in exchange " + exchange);
            Message m = createMessage(message, messageQueueReply, threadLevelPriority.get());
            sendThrottler.acquire();
            if (direct) {
                amqpTemplate.send(exchange, m);
            } else {
                amqpTemplate.send(exchange, "", m);
            }
        } catch (AmqpException | IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private Message createMessage(String message, MessageQueue queueReply, Integer priority) throws UnsupportedEncodingException {
        return createMessage(message, queueReply != null ? queueReply.getName() : null, priority);
    }

    private Message createMessage(String message, String queueReply, Integer priority) throws UnsupportedEncodingException {
        MessageProperties properties = new MessageProperties();
        if (queueReply != null)
            properties.setReplyTo(queueReply);
        if (priority != null)
            properties.setPriority(priority);
        return new Message(message.getBytes(MESSAGE_ENCODING), properties);
    }

    @Override
    public Map<String, QueueStats> queueStats() {
        return amqpTemplate.execute(channel -> {
            final Map<String, QueueStats> result = new HashMap<>();
            for (MessageQueue queue : messageQueuesMap.values()) {
                AMQP.Queue.DeclareOk status = channel.queueDeclarePassive(queue.getName());

                result.put(queue.getName(), new QueueStats(status.getMessageCount(), inStats.get(queue).count(), outStats
                        .get(queue).count(), status.getConsumerCount()));
            }
            return result;
        });
    }

    @Override
    public Map<String, Integer> exchangeStats() {
        final Map<String, Integer> result = new HashMap<>();
        for (ExchangeNode queue : exchangeNodes) {
            result.put(queue.getName(), nodeStats.get(queue).count());
        }
        return result;
    }

    @Override
    public <DTO> void publish(final DTO dto, final ExchangeNode<DTO> node) {
        nodeStats.get(node).put();
        doSend(dto, node.getName(), node.getMessageClass(), null, false);
    }

    @Override
    public void purgeQueue(MessageQueue queue) {
        amqpAdmin.purgeQueue(queue.getName(), false);
    }

    public void setThrottleLimit(int messagesPerSecond) {
        this.sendThrottler = RateLimiter.create(messagesPerSecond);
    }
}
