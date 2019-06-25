package com.datapublica.companies.api.selector;

import com.google.common.collect.Sets;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ErrorSelector implements Selector {

    public String id;
    public Set<String> queues;
    public Boolean recoverable;

    public ErrorSelector withId(String id) {
        this.id = id;
        return this;
    }

    public ErrorSelector withQueues(String... queues) {
        this.queues = Sets.newHashSet(queues);
        return this;
    }

    public ErrorSelector withRecoverable(Boolean recoverable) {
        this.recoverable = recoverable;
        return this;
    }

    @Override
    public Query toQuery() {
        // Init criteria
        final List<Criteria> criterias = new ArrayList<>();
        if(this.id != null) {
            criterias.add(Criteria.where("_id").is(new ObjectId(this.id)));
        }
        if(this.queues != null) {
            criterias.add(Criteria.where("queue").in(this.queues));
        }
        if(this.recoverable != null) {
            criterias.add(Criteria.where("recoverable").is(this.recoverable));
        }
        // Return query
        return Selector.fromCriteriaList(criterias);
    }
}
