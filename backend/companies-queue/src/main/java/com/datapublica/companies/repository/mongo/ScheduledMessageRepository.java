package com.datapublica.companies.repository.mongo;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import com.datapublica.companies.model.scheduler.ScheduledMessage;

/**
 *
 */

public interface ScheduledMessageRepository extends PagingAndSortingRepository<ScheduledMessage, String>, ScheduledMessageRepositoryCustom {
}
