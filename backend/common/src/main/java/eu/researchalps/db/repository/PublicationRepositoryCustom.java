package eu.researchalps.db.repository;

import eu.researchalps.db.model.Identifier;
import eu.researchalps.db.model.publication.Publication;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Created by loic on 21/03/2016.
 * Copyright (C) by Data Publica, All Rights Reserved.
 */
public interface PublicationRepositoryCustom {
    /**
     * Find a publication that matches this one iff:
     *  - their ids are similar
     *  - they share one of their identifiers
     *
     * @param id
     * @param identifiers
     * @return A list of matches (there should be only one?)
     */
    List<Publication> findSimilar(String id, Set<Identifier> identifiers);

    Stream<Publication> streamEntities();
}
