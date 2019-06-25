package com.datapublica.companies.model.scheduler;

/**
 *
 */
public enum ExecutionStatus {
    /**
     * Job is planned to be executed eventually
     */
    PLANNED,
    /**
     * The job has been cancelled but it's currently in execution
     * (it will be unscheduled when the job is finished)
     */
    CANCELLED,
    /**
     * If an internal error has occurred in the scheduling process
     */
    ERROR,
    /**
     * The job message has been submitted to the queue hence it should be running
     */
    SUBMITTED
}
