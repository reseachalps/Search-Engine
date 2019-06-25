package eu.researchalps.workflow.project;

import eu.researchalps.db.model.Project;
import eu.researchalps.db.model.Website;
import eu.researchalps.db.model.full.FSProject;
import eu.researchalps.db.model.full.FullStructure;
import eu.researchalps.db.model.full.FullStructureField;
import eu.researchalps.db.repository.ProjectRepository;
import eu.researchalps.db.repository.StructureRepository;
import eu.researchalps.workflow.full.FullStructureProvider;
import com.google.common.collect.Sets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Created by loic on 16/02/2016.
 * Copyright (C) by Data Publica, All Rights Reserved.
 */
@Component
public class DetectedProjectProvider implements FullStructureProvider<List<FSProject>> {
    public static final Set<FullStructureField> FIELDS = Sets.newHashSet(FullStructureField.WEBSITES, FullStructureField.PROJECTS);

    @Autowired
    private ProjectRepository projectRepository;


    @Autowired
    private StructureRepository structureRepository;

    @Override
    public List<FSProject> computeField(FullStructure structure) {
        // get projects from website info
        if (structure.getWebsites() != null) {
            Set<String> alreadyPresent = new HashSet<>();
            if (structure.getProjects() != null) {
                structure.getProjects().stream().map(FSProject::getId).forEach(alreadyPresent::add);
            }
            List<String> extractedIds = structure.getWebsites().stream()
                    .map(Website::getExtractedProjects)
                    .filter(it -> it != null)
                    .flatMap(Collection::stream)
                    .filter(it -> !alreadyPresent.contains(it))
                    .collect(Collectors.toList());
            Iterable<Project> projects = projectRepository.findAll(extractedIds);
            // Transform project to FSProject and associate light structures to projects
            return StreamSupport.stream(projects.spliterator(), false)
                    .map(p -> ProjectProvider.toFSProject(p, structureRepository, true))
                    .collect(Collectors.toList());
        }
        return null;
    }

    @Override
    public FullStructureField getField() {
        return FullStructureField.DETECTED_PROJECTS;
    }

    @Override
    public Set<FullStructureField> getDependencies() {
        return FIELDS;
    }
}
