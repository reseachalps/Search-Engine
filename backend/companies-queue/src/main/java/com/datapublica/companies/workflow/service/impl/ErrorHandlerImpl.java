package com.datapublica.companies.workflow.service.impl;

import com.datapublica.companies.model.error.ErrorMessage;
import com.datapublica.companies.repository.mongo.ErrorRepository;
import com.datapublica.companies.workflow.MessageQueue;
import com.datapublica.companies.workflow.dto.PluginError;
import com.datapublica.companies.workflow.service.ErrorHandler;
import com.datapublica.companies.workflow.service.QueueComponent;
import com.datapublica.companies.workflow.service.QueueListener;
import com.datapublica.companies.workflow.service.QueueService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 *
 */
@Service
@Profile("!queue-push")
public class ErrorHandlerImpl extends QueueComponent implements QueueListener<PluginError>, ErrorHandler {
    MessageQueue<PluginError> PLUGIN_QUEUE = MessageQueue.get("PLUGIN_ERROR", PluginError.class);

    private static final Logger log = LoggerFactory.getLogger(ErrorHandler.class);

    private QueueService queueService;

    @Value("${errorhandler.plugin.active:true}")
    private boolean pluginHandlingActive;

    @Autowired
    private ErrorRepository errorRepository;

    @Override
    public void ready(QueueService service) {
        this.queueService = service;
    }

    @Override
    public boolean recover(ErrorMessage message) {
        if (message.isRecoverable()) {
            try {
                queueService.pushRaw(message.getMessage(), message.getQueue(), message.getReplyTo(), message.getPriority());
            } catch (Exception e) {
                log.error("Unknown exception found while recovering an error, not deleting the entry...", e);
                return false;
            }
        }
        errorRepository.delete(message);
        return true;
    }

    @Override
    public void handle(ErrorMessage error) {
        log.warn("New error handled from queue " + error.getQueue() + ", putting it into the database");
        errorRepository.save(error);
    }

    @Override
    public void receive(PluginError message) {
        Integer priority = queueService.getThreadPriority();
        ErrorMessage error = new ErrorMessage(message.queue.getName(), message.original_message, message.reply_to != null ? message.reply_to.getName() : null, message.error, true, priority);
        handle(error);
    }

    @Override
    public MessageQueue<PluginError> getQueue() {
        return PLUGIN_QUEUE;
    }

    @Override
    public int getConcurrentConsumers() {
        return pluginHandlingActive ? 1 : 0;
    }
}
