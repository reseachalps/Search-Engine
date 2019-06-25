package com.datapublica.companies.config;

import com.datapublica.companies.util.MongoTemplateExtended;
import com.github.fakemongo.Fongo;
import com.mongodb.MongoClient;
import com.mongodb.WriteConcern;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

/**
 *
 */
@EnableMongoRepositories(basePackages = {"com.datapublica.companies.repository.mongo"}, mongoTemplateRef = "mongoTemplate")
@EnableMongoAuditing
@Configuration
public class FongoConfiguration extends AbstractMongoConfiguration {

    @Override
    protected String getDatabaseName() {
        return "mongo";
    }

    @Override
    public MongoClient mongo() {
        return new Fongo("mongo").getMongo();
    }

    @Override
    public MongoTemplateExtended mongoTemplate() throws Exception {
        MongoTemplateExtended mongoTemplate = new MongoTemplateExtended(mongoDbFactory(), mappingMongoConverter());
        mongoTemplate.setWriteConcern(WriteConcern.SAFE);
        return mongoTemplate;
    }
}
