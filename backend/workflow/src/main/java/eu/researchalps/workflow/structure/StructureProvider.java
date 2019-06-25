package eu.researchalps.workflow.structure;

import eu.researchalps.db.model.Structure;
import eu.researchalps.db.model.full.FullStructure;
import eu.researchalps.db.model.full.FullStructureField;
import eu.researchalps.db.repository.StructureRepository;
import eu.researchalps.workflow.full.FullStructureProvider;
import com.google.common.collect.Sets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Created by loic on 16/02/2016.
 * Copyright (C) by Data Publica, All Rights Reserved.
 */
@Component
public class StructureProvider implements FullStructureProvider<Structure> {
    public static final Set<FullStructureField> FIELDS = Sets.newHashSet();

    @Autowired
    private StructureRepository repository;

    @Override
    public Structure computeField(FullStructure structure) {
        return repository.findOne(structure.getId());
    }

    @Override
    public FullStructureField getField() {
        return FullStructureField.STRUCTURE;
    }

    @Override
    public Set<FullStructureField> getDependencies() {
        return FIELDS;
    }
}
