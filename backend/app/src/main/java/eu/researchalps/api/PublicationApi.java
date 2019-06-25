package eu.researchalps.api;

import eu.researchalps.api.util.ApiConstants;
import eu.researchalps.db.model.full.LightStructure;
import eu.researchalps.db.model.publication.Publication;
import eu.researchalps.db.repository.PublicationRepository;
import eu.researchalps.db.repository.StructureRepository;
import eu.researchalps.search.model.response.PublicationDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by loic on 25/04/2016.
 * Copyright (C) by Data Publica, All Rights Reserved.
 */


@Controller
@RequestMapping("/publications/")
public class PublicationApi {
    @Autowired
    private PublicationRepository publicationRepository;

    @Autowired
    private StructureRepository structureRepository;

    @ResponseBody
    @RequestMapping(value = "{id}", method = RequestMethod.GET, produces = ApiConstants.PRODUCES_JSON)
    public PublicationDTO get(@PathVariable String id) {
        Publication pub = publicationRepository.findOne(id);
        List<LightStructure> structures;
        if (pub.getStructures() != null && !pub.getStructures().isEmpty()) {
            structures = structureRepository.findByIdsLight(pub.getStructures()).stream().map(LightStructure::new).collect(Collectors.toList());
        } else {
            structures = Collections.emptyList();
        }

        return new PublicationDTO(pub, structures);
    }
}
