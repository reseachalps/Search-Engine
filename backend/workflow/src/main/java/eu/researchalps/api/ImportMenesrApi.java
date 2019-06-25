package eu.researchalps.api;
/*
 * Copyright (C) by Data Publica, All Rights Reserved.
 */

import com.datapublica.companies.workflow.service.scheduler.QueueScheduler;
import eu.researchalps.api.util.ApiConstants;
import eu.researchalps.db.model.full.FullStructureField;
import eu.researchalps.db.repository.FullStructureRepository;
import eu.researchalps.db.repository.StructureRepository;
import eu.researchalps.workflow.full.FullStructureService;
import eu.researchalps.workflow.publication.MenesrPublicationFetcher;
import eu.researchalps.workflow.structure.menesr.MenesrImportService;
import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

import static eu.researchalps.api.util.ApiConstants.PRODUCES_JSON;

/**
 * Temporary Class (to be removed) to create structure and index them
 */
@Controller
@RequestMapping("/admin/import")
public class ImportMenesrApi {
    @Autowired
    private MenesrImportService service;
    @Autowired
    private FullStructureRepository repository;
    @Autowired
    private FullStructureService fsservice;
    @Autowired
    private MenesrPublicationFetcher publicationFetcher;

    @Autowired
    private StructureRepository structureRepository;

    @Autowired
    private QueueScheduler queueScheduler;


    @ResponseBody
    @RequestMapping(value = "/structures", method = RequestMethod.GET, produces = PRODUCES_JSON)
    public int fetchStructures() {
        return service.fetchStructures();
    }


    @RequestMapping(value = "/refresh", method = RequestMethod.POST, produces = ApiConstants.PRODUCES_JSON)
    @ResponseBody
    public long refresh(@RequestParam FullStructureField field) {
        return repository.selectAllIds().peek(id -> fsservice.refresh(id, field)).count();
    }

    @RequestMapping(value = "/ensureCreated", method = RequestMethod.POST, produces = ApiConstants.PRODUCES_JSON)
    @ResponseBody
    public long ensureCreated() {
        return structureRepository.streamAllIds().peek(id -> fsservice.ensureCreated(id)).count();
    }

    @ResponseBody
    @RequestMapping(value = "/projects", method = RequestMethod.GET, produces = PRODUCES_JSON)
    public int fetchProjects() {
        return service.fetchProjects();
    }

    @ResponseBody
    @RequestMapping(value = "/publications", method = RequestMethod.GET, produces = PRODUCES_JSON)
    public long fetchPublications() {
        return service.fetchPublications();
    }

    @ResponseBody
    @RequestMapping(value = "/publications-doi", method = RequestMethod.GET, produces = PRODUCES_JSON)
    public long fetchPublicationsDOI() {
        return publicationFetcher.execute();
    }

    @ResponseBody
    @RequestMapping(value = "/all", method = RequestMethod.GET, produces = PRODUCES_JSON)
    public Map<String, Long> all() {
        return ImmutableMap.of("structures", (long) fetchStructures(), "projects", (long) fetchProjects(), "publications", fetchPublications());
    }

}
