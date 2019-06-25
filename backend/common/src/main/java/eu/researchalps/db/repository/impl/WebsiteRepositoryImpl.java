package eu.researchalps.db.repository.impl;

import com.datapublica.companies.util.MongoTemplateExtended;
import eu.researchalps.db.model.Website;
import eu.researchalps.db.repository.WebsiteRepositoryCustom;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.stream.Stream;

/**
 * Created by loic on 31/03/2016.
 * Copyright (C) by Data Publica, All Rights Reserved.
 */
public class WebsiteRepositoryImpl implements WebsiteRepositoryCustom {
    @Autowired
    private MongoTemplateExtended templateExtended;

    @Override
    public Stream<Website> streamAll() {
        return templateExtended.streamAll(Website.class);
    }
}
