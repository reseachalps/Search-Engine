package eu.researchalps.workflow.project;

import eu.researchalps.db.model.Project;
import eu.researchalps.db.model.ProjectStructure;
import eu.researchalps.db.model.full.ExternalStructure;
import eu.researchalps.db.model.full.FSProject;
import eu.researchalps.db.model.full.FullStructure;
import eu.researchalps.db.model.full.FullStructureField;
import eu.researchalps.db.model.full.LightStructure;
import eu.researchalps.db.repository.ProjectRepository;
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
public class ProjectProvider implements FullStructureProvider<List<FSProject>> {
    public static final Set<FullStructureField> FIELDS = Sets.newHashSet();

    @Autowired
    private ProjectRepository projectRepository;


    @Autowired
    private StructureRepository structureRepository;

    @Override
    public List<FSProject> computeField(FullStructure structure) {
        // Transform project to FSProject and associate light structures to projects
        return projectRepository
                .findByStructuresId(structure.getId())
                .stream().map(p -> toFSProject(p, structureRepository, false))
                .collect(Collectors.toList());
    }

    public static FSProject toFSProject(Project p, StructureRepository structureRepository, boolean webDetected) {
        FSProject fsProject = new FSProject(p);
        List<ProjectStructure> structures = p.getStructures();
        // Split structure between external in scanrstructures
        if (structures != null) {
            // scanrstructures structures
            List<String> scanrStructures = structures.stream().filter(s -> !s.isExternal()).map(s -> s.getId()).collect(Collectors.toList());
            List<LightStructure> lightStructures = structureRepository.findByIdsLight(scanrStructures).stream().map(LightStructure::new).collect(Collectors.toList());
            fsProject.setStructures(lightStructures);

            //external structures
            List<ExternalStructure> externalStructures = structures.stream().filter(ProjectStructure::isExternal)
                    .map(s -> new ExternalStructure(s.getLabel(), s.getUrl())).collect(Collectors.toList());
            fsProject.setExternalStructures(externalStructures);
        }
        fsProject.setWebDetected(webDetected);
        return fsProject;
    }

    @Override
    public FullStructureField getField() {
        return FullStructureField.PROJECTS;
    }

    @Override
    public Set<FullStructureField> getDependencies() {
        return FIELDS;
    }
}
