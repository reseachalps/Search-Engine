package com.datapublica.companies.workflow.service;

/**
 * Created by loic on 18/01/2016.
 */
public class QueueComponent {
    protected QueueService queueService;

    public void ready(QueueService service) {
        this.queueService = service;
    }
}
