package com.datapublica.companies.workflow.service.scheduler;

/**
 *
 */
public class ScheduledJobMessage<E, F> {
    public String id;
    public E body;
    public F status;

    public ScheduledJobMessage(String id, E body) {
        this.id = id;
        this.body = body;
    }

    public ScheduledJobMessage(String id, E body, F status) {
        this.id = id;
        this.body = body;
        this.status = status;
    }

    public ScheduledJobMessage() {
    }
}
