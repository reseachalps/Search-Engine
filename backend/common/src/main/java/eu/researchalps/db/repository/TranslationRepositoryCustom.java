package eu.researchalps.db.repository;

import java.util.Collection;

/**
 * Created by loic on 27/02/2019.
 * Copyright (C) by Data Publica, All Rights Reserved.
 */
public interface TranslationRepositoryCustom {
    boolean updateEntry(String id, String targetLanguage, String content);
    boolean areTranslationsComplete(Collection<String> ids);
}
