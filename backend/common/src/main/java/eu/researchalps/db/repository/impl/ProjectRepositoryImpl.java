package eu.researchalps.db.repository.impl;

import com.datapublica.companies.util.MongoTemplateExtended;
import eu.researchalps.db.model.Project;
import eu.researchalps.db.repository.ProjectRepositoryCustom;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;
import java.util.stream.Stream;

/**
 * Created by loic on 08/03/2016.
 * Copyright (C) by Data Publica, All Rights Reserved.
 */
public class ProjectRepositoryImpl implements ProjectRepositoryCustom {

    @Autowired
    private MongoTemplateExtended mongoTemplate;

    @Override
    public Stream<Project> streamEntities() {
        Query query = new Query();
        query.fields().include("name").include("acronym").include("label");
        return mongoTemplate.streamQuery(query, Project.class);
    }

    @Override
    public List<Project> findByAcronymLike(String regex, int limit) {
        Query filter = new Query(Criteria.where("acronym").regex(regex, "i"));
        filter.fields().include("name").include("acronym").include("label");
        filter.limit(limit);
        return mongoTemplate.find(filter, Project.class);
    }
}
