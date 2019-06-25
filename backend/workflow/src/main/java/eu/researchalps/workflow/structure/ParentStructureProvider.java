package eu.researchalps.workflow.structure;

import eu.researchalps.db.model.ParentReference;
import eu.researchalps.db.model.Structure;
import eu.researchalps.db.model.full.FullStructure;
import eu.researchalps.db.model.full.FullStructureField;
import eu.researchalps.db.model.full.LightStructure;
import eu.researchalps.db.repository.StructureRepository;
import eu.researchalps.workflow.full.FullStructureProvider;
import com.google.common.collect.Sets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by loic on 16/02/2016.
 * Copyright (C) by Data Publica, All Rights Reserved.
 */
@Component
public class ParentStructureProvider implements FullStructureProvider<List<LightStructure>> {
    public static final Set<FullStructureField> FIELDS = Sets.newHashSet(FullStructureField.STRUCTURE);

    @Autowired
    private StructureRepository repository;

    @Override
    public List<LightStructure> computeField(FullStructure structure) {
        List<ParentReference> parent = structure.getStructure().getParent();
        if (parent == null || parent.isEmpty()) return null;

        List<Structure> parentStructures = repository.findByIdsLight(parent.stream().map(ParentReference::getId).collect(Collectors.toList()));
        return parentStructures.stream().map(LightStructure::new).collect(Collectors.toList());
    }

    @Override
    public FullStructureField getField() {
        return FullStructureField.PARENTS;
    }

    @Override
    public Set<FullStructureField> getDependencies() {
        return FIELDS;
    }
}
