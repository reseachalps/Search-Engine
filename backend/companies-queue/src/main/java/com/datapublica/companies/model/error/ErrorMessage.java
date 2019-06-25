package com.datapublica.companies.model.error;

import com.datapublica.companies.workflow.MessageQueue;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

/**
 *
 */
@CompoundIndexes({@CompoundIndex(name = "queueIdx", def = "{\"queue\":1}}")})
@Document
public class ErrorMessage {

    @Id
    private ObjectId id;
    private String queue;
    private String replyTo;
    private Date timestamp;
    private String message;
    private String stackTrace;
    private boolean recoverable;
    private Integer priority;

    public ErrorMessage() {
    }

    public ErrorMessage(String queue, String message, String replyTo, String stackTrace, boolean recoverable, Integer priority) {
        this.queue = queue;
        this.message = message;
        this.replyTo = replyTo;
        this.priority = priority;
        timestamp = new Date();
        this.stackTrace = stackTrace;
        this.recoverable = recoverable;
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

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStackTrace() {
        return stackTrace;
    }

    public void setStackTrace(String stackTrace) {
        this.stackTrace = stackTrace;
    }

    public boolean isRecoverable() {
        return recoverable;
    }

    public void setRecoverable(boolean recoverable) {
        this.recoverable = recoverable;
    }

    public String getId() {
        return id.toString();
    }

    public void setId(String id) {
        this.id = new ObjectId(id);
    }

    public void setIf(ObjectId id) {
        this.id = id;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }
}
