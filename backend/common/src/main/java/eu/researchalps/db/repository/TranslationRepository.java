package eu.researchalps.db.repository;

import eu.researchalps.db.model.Translation;
import com.google.common.collect.Sets;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Set;

public interface TranslationRepository extends MongoRepository<Translation, String>, TranslationRepositoryCustom {
    Set<String> ALL_LANGS = Sets.newHashSet("fr", "it", "en", "de", "sl");

}
