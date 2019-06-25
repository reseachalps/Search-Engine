package com.datapublica.companies.workflow.service;

import java.util.List;

/**
 * Created by loic on 18/01/2016.
 */
public interface QueueBulkListener<DTO> extends QueueSubscriber<DTO> {
    int getBulkSize();

    /**
     * Message callback. The DTO is the json equivalent of what's coming in the queue (may be a list on bulk)
     *
     * @param message A DTO instance coming from the queue
     */
    void receive(List<DTO> message);
}
