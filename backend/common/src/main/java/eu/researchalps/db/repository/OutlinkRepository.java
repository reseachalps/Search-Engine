package eu.researchalps.db.repository;

import eu.researchalps.db.model.Outlink;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 *
 */

public interface OutlinkRepository extends MongoRepository<Outlink, String> {
}
