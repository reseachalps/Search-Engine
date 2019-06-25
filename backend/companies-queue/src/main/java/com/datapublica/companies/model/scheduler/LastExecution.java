package com.datapublica.companies.model.scheduler;

import java.util.Date;

import org.springframework.scheduling.TriggerContext;
import org.springframework.scheduling.support.SimpleTriggerContext;

/**
 *
 */
public class LastExecution {
    public Date lastActualExecutionTime;
    public Date lastCompletionTime;
    public Date lastScheduledExecutionTime;
    public String status;

    public TriggerContext asContext() {
        SimpleTriggerContext context = new SimpleTriggerContext();
        context.update(lastScheduledExecutionTime, lastActualExecutionTime, lastCompletionTime);
        return context;
    }
}
