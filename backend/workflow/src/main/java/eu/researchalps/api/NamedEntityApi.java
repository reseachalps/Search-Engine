package eu.researchalps.api;

import eu.researchalps.db.repository.ProjectRepository;
import eu.researchalps.db.repository.PublicationRepository;
import eu.researchalps.workflow.website.entity.NamedEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by loic on 08/03/2016.
 * Copyright (C) by Data Publica, All Rights Reserved.
 */
@Controller
@RequestMapping("/entities")
public class NamedEntityApi {
    @Autowired
    private ProjectRepository repository;
    @Autowired
    private PublicationRepository publicationRepository;

    @ResponseBody
    @RequestMapping(method = RequestMethod.GET)
    public List<NamedEntity> get() {
        return Stream.concat(
                repository.streamEntities().map(NamedEntity::new),
                publicationRepository.streamEntities().map(NamedEntity::new)
        ).collect(Collectors.toList());
    }

}
