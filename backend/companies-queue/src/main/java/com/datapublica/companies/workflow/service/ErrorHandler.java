package com.datapublica.companies.workflow.service;

import com.datapublica.companies.model.error.ErrorMessage;
import com.datapublica.companies.workflow.dto.PluginError;
import com.datapublica.companies.workflow.MessageQueue;

/**
 * Public interface to the workflow error handler
 */
public interface ErrorHandler {


    boolean recover(ErrorMessage message);

    /**
     * Handle a user-generated error object
     *
     * @param error the object
     */
    void handle(ErrorMessage error);
}
