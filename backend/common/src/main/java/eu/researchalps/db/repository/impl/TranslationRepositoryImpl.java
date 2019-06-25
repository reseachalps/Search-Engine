package eu.researchalps.db.repository.impl;

import eu.researchalps.db.model.Translation;
import eu.researchalps.db.repository.TranslationRepositoryCustom;
import com.mongodb.WriteResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.Collection;

import static eu.researchalps.db.repository.TranslationRepository.ALL_LANGS;

/**
 * Created by loic on 27/02/2019.
 * Copyright (C) by Data Publica, All Rights Reserved.
 */
public class TranslationRepositoryImpl implements TranslationRepositoryCustom {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public boolean updateEntry(String id, String targetLanguage, String content) {
        final WriteResult result = mongoTemplate.updateFirst(new Query(Criteria.where("_id").is(id)), new Update().set("translations."+targetLanguage, content), Translation.class);
        return result.getN() > 0;
    }

    @Override
    public boolean areTranslationsComplete(Collection<String> ids) {
        Criteria criteria = Criteria.where("_id").in(ids);
        for (String lang : ALL_LANGS) {
            criteria = criteria.and("translations."+lang).exists(true);
        }

        return mongoTemplate.count(new Query(criteria), Translation.class) == ids.size();
    }
}
