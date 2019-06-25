package com.datapublica.companies.workflow.dto;

import com.datapublica.companies.workflow.MessageQueue;

/**
*
*/
public class PluginError {
    public String error;
    public MessageQueue reply_to;
    public String original_message;
    public MessageQueue queue;
}
