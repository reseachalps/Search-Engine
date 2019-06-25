package com.datapublica.companies.util;

import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.MongoConverter;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.util.CloseableIterator;

import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Inspired by
 * http://djk.website.pl/index.php/2014/01/mongodb-java-8-streams-spring-4-
 * spring-data-mongodb-1-3-3-release/
 * <p/>
 * with some extension...
 * <p/>
 * Makes a stream API available in mongo template!
 */
public class MongoTemplateExtended extends MongoTemplate {
    public MongoTemplateExtended(MongoDbFactory mongoDbFactory, MongoConverter mongoConverter) {
        super(mongoDbFactory, mongoConverter);
    }

    public <T> Stream<T> streamAll(Class<T> type) {
        return streamQuery(new Query(), type);
    }

    public <T> Stream<T> streamQuery(Query query, Class<T> type) {
        CloseableIterator<T> resultIt = stream(query, type);
        Spliterator<T> split = Spliterators.spliteratorUnknownSize(resultIt, Spliterator.ORDERED);
        return StreamSupport.stream(split, false).onClose(resultIt::close);
    }
}
