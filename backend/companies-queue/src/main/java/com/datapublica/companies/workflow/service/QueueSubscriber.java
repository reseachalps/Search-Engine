package com.datapublica.companies.workflow.service;

import com.datapublica.companies.workflow.MessageQueue;

/**
 * Created by loic on 18/01/2016.
 */
public interface QueueSubscriber<DTO> {
    /**
     * Get the number of consumers for the message listener
     *
     * @return the number of consumers
     */
    default int getConcurrentConsumers() {
        return 1;
    }

    /**
     * The queue to listen to
     *
     * @return The queue
     */
    MessageQueue<DTO> getQueue();

    /**
     * Define the consumer timeout
     *
     * @return the receiveTimeout. null is driver's default.
     */
    default Long receiveTimeout() {
        return null;
    }
}
