package com.datapublica.companies.model.scheduler;

/**
 *
 */
public class TriggerInfo {
    public TriggerType type = TriggerType.CUSTOM;
    public String expression;
    /**
     * Periods are in seconds
     */
    public long period;
    public long initialDelay;

    public TriggerInfo(TriggerType type) {
        this.type = type;
    }

    public TriggerInfo(String cronExpression) {
        type = TriggerType.CRON;
        expression = cronExpression;
    }

    public TriggerInfo(long period, long initialDelay) {
        this.period = period;
        this.initialDelay = initialDelay;
        this.type = TriggerType.RATE;
    }

    public TriggerInfo(long period, long initialDelay, boolean fixed) {
        this(period, initialDelay);
        if(fixed)
            this.type = TriggerType.FIXED_RATE;
    }

    public TriggerInfo() {
    }
}
