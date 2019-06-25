package com.datapublica.companies.workflow.service.scheduler;

/**
 * Created by loic on 13/06/2016.
 * Copyright (C) by Data Publica, All Rights Reserved.
 */
public class ScheduledJobOutput<E, F> extends ScheduledJobMessage<E, F> {
    public boolean reschedule = false;

    public ScheduledJobOutput(String id, E body, F status, boolean reschedule) {
        this.id = id;
        this.body = body;
        this.status = status;
        this.reschedule = reschedule;
    }

    public ScheduledJobOutput() {
    }

    public ScheduledJobOutput(String id, E body, F status) {
        this(id, body, status, false);
    }
}
