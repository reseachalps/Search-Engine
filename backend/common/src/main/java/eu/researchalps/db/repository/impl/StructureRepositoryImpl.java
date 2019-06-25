package eu.researchalps.db.repository.impl;

import com.datapublica.companies.util.MongoTemplateExtended;
import eu.researchalps.db.model.Structure;
import eu.researchalps.db.repository.StructureRepositoryCustom;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.stream.Stream;

/**
 * Created by loic on 16/02/2016.
 * Copyright (C) by Data Publica, All Rights Reserved.
 */
public class StructureRepositoryImpl implements StructureRepositoryCustom {
    @Autowired
    private MongoTemplateExtended mongo;

    @Override
    public Stream<String> streamAllIds() {
        return streamIds(new Query());
    }

    @Override
    public Stream<String> streamAllIdsByLinkId(String websiteId) {
        return streamIds(new Query(Criteria.where("links._id").is(websiteId)));
    }

    protected Stream<String> streamIds(Query query) {
        query.fields().include("_id");
        return mongo.streamQuery(query, Structure.class).map(Structure::getId);
    }
}
