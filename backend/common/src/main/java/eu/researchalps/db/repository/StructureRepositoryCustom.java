package eu.researchalps.db.repository;

import java.util.stream.Stream;

/**
 * Created by loic on 16/02/2016.
 * Copyright (C) by Data Publica, All Rights Reserved.
 */
public interface StructureRepositoryCustom {
    Stream<String> streamAllIds();
    Stream<String> streamAllIdsByLinkId(String websiteId);

}
