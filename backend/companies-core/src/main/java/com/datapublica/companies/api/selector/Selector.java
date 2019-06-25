package com.datapublica.companies.api.selector;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;

public interface Selector {
    ObjectMapper OM = new ObjectMapper();

    static Query fromCriteriaList(List<Criteria> criterias) {
        // Return query
        Criteria criteria = new Criteria();
        if (!criterias.isEmpty())
            criteria = criteria.andOperator(criterias.toArray(new Criteria[criterias.size()]));
        return new Query(criteria);
    }

    Query toQuery();

    default String toJson() {
        try {
            return OM.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }
}
