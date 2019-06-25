package eu.researchalps.db.repository;/*
 * Copyright (C) by Data Publica, All Rights Reserved.
 */

import eu.researchalps.db.model.full.FullStructure;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface FullStructureRepository extends MongoRepository<FullStructure, String>, FullStructureRepositoryCustom {
    @Query(value = "{_id: {'$in': ?0}}", fields = "{_id: 1, 'websites.twitter.profilePictureUrl':1}")
    List<FullStructure> findByIdsLightWithTwitterLogo(List<String> twitterLogoToFetchStructures);
}
