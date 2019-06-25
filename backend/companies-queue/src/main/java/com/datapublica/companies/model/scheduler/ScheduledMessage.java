package com.datapublica.companies.model.scheduler;

import com.datapublica.companies.workflow.MessageQueue;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document
public class ScheduledMessage {
    @Id
    private String id;
    @Indexed
    private String provider;

    private String queue;
    private String replyTo;
    private Integer priority;

    private String message;

    private TriggerInfo triggerInfo;

    private LastExecution lastExecution = new LastExecution();

    @Indexed
    private Date nextExecution;

    private ExecutionStatus status = null;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getQueue() {
        return queue;
    }

    public void setQueue(MessageQueue queue) {
        this.queue = queue.getName();
    }

    public String getReplyTo() {
        return replyTo;
    }

    public void setReplyTo(MessageQueue replyTo) {
        if (replyTo != null) {
            this.replyTo = replyTo.getName();
        } else {
            this.replyTo = null;
        }
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public TriggerInfo getTriggerInfo() {
        return triggerInfo;
    }

    public void setTriggerInfo(TriggerInfo triggerInfo) {
        this.triggerInfo = triggerInfo;
    }

    public LastExecution getLastExecution() {
        return lastExecution;
    }

    public void setLastExecution(LastExecution lastExecution) {
        this.lastExecution = lastExecution;
    }

    public Date getNextExecution() {
        return nextExecution;
    }

    public void setNextExecution(Date nextExecution) {
        this.nextExecution = nextExecution;
    }

    public ExecutionStatus getStatus() {
        return status;
    }

    public void setStatus(ExecutionStatus status) {
        this.status = status;
    }

    public static String idFromProvider(String provider, String localId) {
        return provider + ":" + localId;
    }

    public static String localIdFromId(String id) {
        return id.substring(id.indexOf(":") + 1);
    }

    public static String providerFromId(String id) {
        return id.substring(0, id.indexOf(":"));
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }
}
