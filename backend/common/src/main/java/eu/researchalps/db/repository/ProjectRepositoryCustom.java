package eu.researchalps.db.repository;/*
 * Copyright (C) by Data Publica, All Rights Reserved.
 */

import eu.researchalps.db.model.Project;

import java.util.List;
import java.util.stream.Stream;

public interface ProjectRepositoryCustom {
    Stream<Project> streamEntities();

    /**
     * Return 'limit' projects matching the given regex
     *
     * @param regex
     * @return
     */
    List<Project> findByAcronymLike(String regex, int limit);
}
