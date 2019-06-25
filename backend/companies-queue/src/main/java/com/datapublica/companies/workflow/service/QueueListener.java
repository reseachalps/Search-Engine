package com.datapublica.companies.workflow.service;

/**
 * Declares a listener plugged in the Queue service. Any service implementing this queue will be automatically
 * registered.
 * <p>
 * This interface is parametrized by two concepts: the DTO class and the merged class.
 * <p>
 * It is important to NOT auto-wire a QueueService AND do a PostConstruct in a QueueListener.
 * If you need to initialize the listener, you should override the service callback #ready.
 */
public interface QueueListener<DTO> extends QueueSubscriber<DTO> {
    /**
     * Message callback. The DTO is the json equivalent of what's coming in the queue (may be a list on bulk)
     *
     * @param message A DTO instance coming from the queue
     */
    void receive(DTO message);

}
