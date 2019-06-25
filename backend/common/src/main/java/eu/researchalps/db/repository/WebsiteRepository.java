package eu.researchalps.db.repository;

import eu.researchalps.db.model.Website;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Created by loic on 25/02/2016.
 * Copyright (C) by Data Publica, All Rights Reserved.
 */
public interface WebsiteRepository extends MongoRepository<Website, String>, WebsiteRepositoryCustom {
}
