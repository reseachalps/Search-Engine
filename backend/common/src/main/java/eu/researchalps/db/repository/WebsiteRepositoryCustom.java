package eu.researchalps.db.repository;

import eu.researchalps.db.model.Website;

import java.util.stream.Stream;

/**
 * Created by loic on 31/03/2016.
 * Copyright (C) by Data Publica, All Rights Reserved.
 */
public interface WebsiteRepositoryCustom {
    Stream<Website> streamAll();
}
