package eu.researchalps.db.repository;

import eu.researchalps.db.model.SearchEvent;
import org.bson.types.ObjectId;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * @author maxime (mma)
 */
public interface SearchEventRepository extends PagingAndSortingRepository<SearchEvent, ObjectId> {
}