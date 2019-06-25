package eu.researchalps.search.repository;/*
 * Copyright (C) by Data Publica, All Rights Reserved.
 */

import eu.researchalps.search.model.FullStructureIndex;
import org.springframework.data.elasticsearch.repository.ElasticsearchCrudRepository;

public interface FullStructureSearchRepository extends ElasticsearchCrudRepository<FullStructureIndex, String>, FullStructureSearchRepositoryCustom {
}
