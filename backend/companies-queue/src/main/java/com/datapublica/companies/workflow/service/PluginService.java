package com.datapublica.companies.workflow.service;

/**
 * Declares a #QueueListener that has the particularity to route automatically the output DTO to the replyTo parameter
 */
public interface PluginService<INPUT_DTO, OUTPUT_DTO> extends QueueSubscriber<INPUT_DTO> {
    /**
     * Message callback. The DTO is the json equivalent of what's coming in the queue (may be a list on bulk)
     *
     * @param message A DTO instance coming from the queue
     */
    OUTPUT_DTO receiveAndReply(INPUT_DTO message);
}
