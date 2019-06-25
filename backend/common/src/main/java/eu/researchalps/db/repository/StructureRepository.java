package eu.researchalps.db.repository;/*
 * Copyright (C) by Data Publica, All Rights Reserved.
 */

import eu.researchalps.db.model.Structure;
import org.springframework.data.domain.Pageable;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Point;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Collection;
import java.util.List;

public interface StructureRepository extends MongoRepository<Structure, String>, StructureRepositoryCustom {
    @Query(value = "{_id: {'$in': ?0}}", fields = "{_id: 1, kind: 1, label: 1, logo: 1, acronym: 1, type.publicEntity: 1}")
    List<Structure> findByIdsLight(Collection<String> id);

    @Query(value = "{_id: ?0}", fields = "{_id: 1, kind: 1, label: 1, logo: 1, acronym: 1, type.publicEntity: 1}")
    Structure findByIdLight(String id);

    @Query(value = "{'parent._id': ?0}", fields = "{_id: 1, kind: 1, label: 1, logo: 1, acronym: 1, type.publicEntity: 1}")
    List<Structure> findByParentIdLight(String id);

    @Query(value = "{'relations._id': {'$in': ?0}}", fields = "{_id: 1}")
    List<Structure> findIdsByRelationId(Collection<String> relationIds);

    @Query(value = "{'institutions.code.normalized': {'$in': ?0}}", fields = "{_id: 1}")
    List<Structure> findIdsByInstitutionCode(Collection<String> normalizedCodes);

    @Query(value = "{_id: {'$in': ?0}}", delete = true)
    void deleteByIds(Collection<String> id);

    List<Structure> findByAddressGpsNear(Point point, Distance distance, Pageable request);
}
