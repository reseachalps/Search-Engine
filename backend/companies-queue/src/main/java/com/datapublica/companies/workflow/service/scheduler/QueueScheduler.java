package com.datapublica.companies.workflow.service.scheduler;

import java.util.Date;
import java.util.stream.Stream;

import com.datapublica.companies.model.scheduler.ScheduledMessage;
import com.datapublica.companies.model.scheduler.TriggerInfo;
import com.datapublica.companies.workflow.MessageQueue;
import com.datapublica.companies.workflow.dto.ScheduledJobResponse;

/**
 *
 */
public interface QueueScheduler {

    /**
     * Schedule or update a new job.
     * 
     * Its behaviour against the different status goes as follows:
     * <ul>
     * <li>If the job did not exist, its status is set to PLANNED and its next execution is computed.</li>
     * <li>If the job is PLANNED, the next execution is not updated.</li>
     * <li>If the job is SUBMITTED, the next execution is not updated. Note that the routing is updated.</li>
     * <li>If the job is CANCELLED, like SUBMITTED but the status goes back to PLANNED.</li>
     * <li>If the job is ERROR, the next execution re-computed and its status updated to PLANNED.</li>
     * </ul>
     *
     * @param provider "signal"
     * @param id The unique id (for this provider)
     * @param message The message content
     * @param trigger The scheduling info
     * @param queue The queue to submit to
     * @param replyTo The queue to route to after the job is complete. Can be null (will ignore the result)
     * @param priority The priority to use when executing the task
     * @return The schedule description
     */
    <E> ScheduledMessage scheduleJob(String provider, String id, E message, TriggerInfo trigger, MessageQueue queue,
                                     MessageQueue replyTo, Integer priority);

    /**
     * Cancel a scheduled job.
     * 
     * Its behaviour against the different status goes as follows:
     * <ul>
     * <li>If the job is SUBMITTED or CANCELLED, the job is kept in the schedule table but its status is switched to
     * CANCELLED.</li>
     * <li>Else, the job is deleted from the schedule table.</li>
     * </ul>
     * 
     * When a submitted job answers during a cancellation, the scheduled entry is deleted and the return message is
     * not transferred to the reply to queue.
     * 
     * @param id The job id
     * @return true if the job existed in the database
     */
    boolean cancelJob(String id);

    /**
     * Manually adjust a scheduled job. This operation is valid whatever the execution status. The only difference arise
     * if the status is ERROR, then its status is updated to PLANNED
     * 
     * @param id The job id
     */
    void adjustNextExecution(String id, Date nextDate);

    /**
     * Returns the stream of ids for a provider
     * @param provider
     * @return
     */
    Stream<String> listJobs(String provider);
}
