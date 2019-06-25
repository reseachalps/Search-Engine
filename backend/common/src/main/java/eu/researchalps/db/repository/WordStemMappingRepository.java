package eu.researchalps.db.repository;/*
 * Copyright (C) by Data Publica, All Rights Reserved.
 */

import eu.researchalps.db.model.WordStemMapping;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface WordStemMappingRepository extends MongoRepository<WordStemMapping, String> {
}
