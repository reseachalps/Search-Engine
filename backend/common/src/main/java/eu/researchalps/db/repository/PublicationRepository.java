package eu.researchalps.db.repository;

import eu.researchalps.db.model.publication.Publication;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Collection;
import java.util.List;

/**
 * Created by loic on 21/03/2016.
 * Copyright (C) by Data Publica, All Rights Reserved.
 */
public interface PublicationRepository extends MongoRepository<Publication, String>, PublicationRepositoryCustom  {
    List<Publication> findByStructures(String structureId);

    @Query(value = "{_id: {'$in': ?0}}", fields = "{structures:1}")
    List<Publication> findStructuresByPublicationIds(Collection<String> publicationIds);

}
