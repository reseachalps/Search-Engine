package eu.researchalps.db.repository;/*
 * Copyright (C) by Data Publica, All Rights Reserved.
 */

import eu.researchalps.db.model.Project;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Collection;
import java.util.List;

public interface ProjectRepository extends MongoRepository<Project, String>, ProjectRepositoryCustom {
    @Query(value = "{_id: {'$in': ?0}}", fields = "{structures:1}")
    List<Project> findStructuresByProjectIds(Collection<String> projectIds);

    @Query(value = "{}", fields = "{acronym:1, label:1}")
    List<Project> findAllNames();

    List<Project> findByStructuresId(String id);
}
