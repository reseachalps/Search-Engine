package eu.researchalps.db.repository.impl;

import com.datapublica.companies.util.MongoTemplateExtended;
import eu.researchalps.db.model.Identifier;
import eu.researchalps.db.model.publication.Publication;
import eu.researchalps.db.model.publication.PublicationType;
import eu.researchalps.db.repository.PublicationRepositoryCustom;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Created by loic on 21/03/2016.
 * Copyright (C) by Data Publica, All Rights Reserved.
 */
public class PublicationRepositoryImpl implements PublicationRepositoryCustom {
    @Autowired
    private MongoTemplateExtended mongo;

    @Override
    public List<Publication> findSimilar(String id, Set<Identifier> identifiers) {
        // Conditions list treated as OR
        List<Criteria> conditions = new LinkedList<>();

        // Id can be null if we want to search only by identifiers
        if (id != null) {
            conditions.add(Criteria.where("_id").is(id));
        }

        for (Identifier identifier : identifiers) {
            if (identifier.getType() == null || identifier.getId() == null) {
                continue;
            }
            conditions.add(Criteria.where("identifiers").elemMatch(Criteria.where("_id").is(identifier.getId()).and("type").is(identifier.getType())));
        }

        if (conditions.isEmpty()) {
            return new LinkedList<>();
        }

        // Create the query as a or
        Query query = new Query(new Criteria().orOperator(conditions.toArray(new Criteria[conditions.size()])));
        return mongo.find(query, Publication.class);
    }

    @Override
    public Stream<Publication> streamEntities() {
        Query q = new Query(new Criteria("type").is(PublicationType.PATENT.name()));
        q.fields().include("_id").include("title").include("identifiers.patent");
        return mongo.streamQuery(q, Publication.class);
    }
}
