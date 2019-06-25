package com.datapublica.companies.repository.mongo;

import com.datapublica.companies.model.scheduler.ScheduledMessage;

import java.util.Date;
import java.util.stream.Stream;

/**
 *
 */
public interface ScheduledMessageRepositoryCustom {
    Stream<ScheduledMessage> findAllForExecution(Date now);
    void updateNextExecution(String id, Date next);

    Stream<String> findAllFromProvider(String provider);
}
