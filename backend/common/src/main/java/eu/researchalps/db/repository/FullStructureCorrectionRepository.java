package eu.researchalps.db.repository;

import eu.researchalps.db.model.FullStructureCorrection;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Created by loic on 12/05/2016.
 * Copyright (C) by Data Publica, All Rights Reserved.
 */
public interface FullStructureCorrectionRepository extends MongoRepository<FullStructureCorrection, String> {
}
