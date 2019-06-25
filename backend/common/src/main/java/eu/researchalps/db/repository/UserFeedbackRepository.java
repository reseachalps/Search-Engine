package eu.researchalps.db.repository;

import eu.researchalps.db.model.UserFeedback;
import eu.researchalps.db.model.UserFeedbackStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 *
 */

public interface UserFeedbackRepository extends MongoRepository<UserFeedback, String> {

    Page<UserFeedback> findByStatus(UserFeedbackStatus status, Pageable var1);

}
