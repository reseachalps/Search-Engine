package com.datapublica.companies.workflow.service;

import com.datapublica.companies.workflow.ExchangeNode;
import com.datapublica.companies.workflow.MessageQueue;

import java.util.List;
import java.util.Map;


/**
 * Generic queue service
 *
 * This service is able to publish data inside message queues/exchange nodes. It is also responsible of registering
 * MessageQueue, ExchangeNode and QueueListener instances in order to make the whole system functional.
 */
public interface QueueService {
    /**
     * Non-blocking message queue fetcher. You should not use this is in a normal behaviour. Although, it might be
     * of some use if you need to manually purge a queue for instance.
     *
     * In case of a wrong cast or a parsing error, the message is handled by the error handler and will be reported.
     *
     * @param queue The queue
     * @param <E> The casting class (must be an instance of the message class)
     * @return A correctly casted message or null if queue is empty or a message was badly cast
     */
    <E> E fetch(MessageQueue<E> queue);

    /**
     * Publish a message inside the selected message queue. The message will surely be converted as JSON via Jackson so
     * be sure to have all the righteous annotations.
     *
     * @param dto The message
     * @param messageQueue The target queue
     * @param messageQueueReply The queue to reply to
     * @throws IllegalStateException if the dto is not an instance of the declared message class of the queue
     */
    <DTO> void push(DTO dto, MessageQueue<DTO> messageQueue, MessageQueue messageQueueReply);

    /**
     * Publish a message inside the selected message queue. The message will surely be converted as JSON via Jackson so
     * be sure to have all the righteous annotations.
     *
     * @param dto The message
     * @param messageQueue The target queue
     * @throws IllegalStateException if the dto is not an instance of the declared message class of the queue
     */
    default <DTO> void push(DTO dto, MessageQueue<DTO> messageQueue) {
        this.push(dto, messageQueue, null);
    };

    /**
     * Publish a message inside a node. Nodes are bound to queues via the MessageQueue enum
     *
     * @param dto The message
     * @param node The target node
     * @throws IllegalStateException if the dto is not an instance of the declared message class of the node
     */
    <DTO> void publish(DTO dto, ExchangeNode<DTO> node);

    /**
     * Give stats for all the persistent queues declared in the system
     *
     * @return A map Queue -> Stats object
     */
    Map<String, QueueStats> queueStats();

    /**
     * Give stats for all the exchange nodes (fanout) declared in the system
     *
     * @return A map Queue -> Integer (in throughput)
     */
    Map<String, Integer> exchangeStats();

    /**
     * Purge a given queue
     * @param queue the queue to purge
     */
    void purgeQueue(MessageQueue queue);

    /**
     * Push directly a message as a string, absolutely no check is performed, it is just encoded, nothing more
     *
     * @param message The message as a string
     * @param queue The target queue
     * @param replyTo The queue to reply to
     * @param priority The priority to use
     */
    void pushRaw(String message, MessageQueue queue, MessageQueue replyTo, Integer priority);

    /**
     * Push directly a message as a string, absolutely no check is performed, it is just encoded, nothing more
     *
     * @param message The message as a string
     * @param queue The target queue
     * @param replyTo The queue to reply to
     * @param priority The priority to use
     */
    void pushRaw(String message, String queue, String replyTo, Integer priority);

    /**
     * Stop a queue listener from receiving messages.
     * Call is ignored if the listener is already stopped.
     *
     * @param listener The listener
     */
    void stop(QueueSubscriber listener);

    /**
     * Resume the listening of message for a queue listener.
     * Call is ignored if the listener is already active.
     *
     * @param listener The listener
     */
    void resume(QueueSubscriber listener);

    /**
     * Set the current thread priority level
     *
     * @param priority the current priority, if null, it will remove the priority information if any.
     */
    public void setThreadPriority(Integer priority);

    /**
     * Get the current thread priority level
     *
     * @return the priority level (can be null)
     */
    public Integer getThreadPriority();

    /**
     * Get all internal listeners
     *
     * @return The list of listeners
     */
    List<QueueSubscriber> getSubscribers();
}
