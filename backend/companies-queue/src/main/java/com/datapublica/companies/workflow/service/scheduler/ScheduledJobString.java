package com.datapublica.companies.workflow.service.scheduler;

import java.util.Date;

/**
 *
 */
public class ScheduledJobString extends ScheduledJobInput<String, String> {
    public ScheduledJobString(String id, Date timestamp, Date lastExecution, String body, String status) {
        super(id, timestamp, lastExecution, body, status);
    }

    public ScheduledJobString() {
    }
}
