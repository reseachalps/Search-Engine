package eu.researchalps.db.repository;

import eu.researchalps.db.model.full.FullStructure;
import eu.researchalps.db.model.full.FullStructureField;

import java.util.stream.Stream;

/**
 * Created by loic on 29/02/2016.
 * Copyright (C) by Data Publica, All Rights Reserved.
 */
public interface FullStructureRepositoryCustom {
    Stream<String> selectAllIds();
    public FullStructure findOne(String id, FullStructureField... fields);

    public Stream<FullStructure> streamAll(FullStructureField... fields);

    public Stream<String> streamIdsToIndex();

    boolean addDelayedFieldToRefresh(String id, FullStructureField... fields);

    boolean notifyIndexed(String id);
}
