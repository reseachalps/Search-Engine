package eu.researchalps.workflow.errors;

import com.datapublica.companies.model.error.ErrorMessage;
import com.datapublica.companies.repository.mongo.ErrorRepository;
import com.datapublica.companies.workflow.MessageQueue;
import com.datapublica.companies.workflow.service.ErrorHandler;
import com.datapublica.companies.workflow.service.PluginService;
import com.datapublica.companies.workflow.service.QueueComponent;
import com.datapublica.companies.workflow.service.scheduler.ScheduledJobString;
import eu.researchalps.config.ErrorPatternConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.regex.Pattern;

/**
 * Created by loic on 09/06/2016.
 * Copyright (C) by Data Publica, All Rights Reserved.
 */
@Component
public class ErrorRecoverProcess extends QueueComponent implements PluginService<ScheduledJobString, ScheduledJobString> {
    public static final MessageQueue<ScheduledJobString> QUEUE = MessageQueue.get("ERROR_RECOVER", ScheduledJobString.class);
    public static final String PROVIDER = "errors";
    public static final String ID = "recover";

    @Autowired
    private ErrorPatternConfig patternConfig;

    @Autowired
    private ErrorRepository repository;

    @Autowired
    private ErrorHandler handler;

    @Override
    public ScheduledJobString receiveAndReply(ScheduledJobString message) {
        int[] stats = new int[]{0,0};
        repository.streamAll().forEach(errorMessage -> {
            if (match(errorMessage, patternConfig.getRecover())) {
                handler.recover(errorMessage);
                stats[0]++;
            } else if (match(errorMessage, patternConfig.getIgnore())) {
                repository.delete(errorMessage);
                stats[1]++;
            }
        });
        message.status = String.format("Recovered %d messages. Ignored %d messages.", stats[0], stats[1]);
        return message;
    }

    private boolean match(ErrorMessage error, Set<Pattern> patterns) {
        if (patterns.isEmpty()) return false;
        String stackTrace = error.getStackTrace();
        if (stackTrace == null) {
            return false;
        }
        stackTrace = stackTrace.toLowerCase();
        for (Pattern pattern : patterns) {
            if (pattern.matcher(stackTrace).find()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public MessageQueue<ScheduledJobString> getQueue() {
        return QUEUE;
    }
}
