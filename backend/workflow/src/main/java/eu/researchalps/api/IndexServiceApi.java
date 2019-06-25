package eu.researchalps.api;
/*
 * Copyright (C) by Data Publica, All Rights Reserved.
 */

import com.datapublica.companies.workflow.service.QueueService;
import eu.researchalps.db.repository.FullStructureRepository;
import eu.researchalps.db.repository.StructureRepository;
import eu.researchalps.workflow.search.IndexStructureProcess;
import eu.researchalps.workflow.search.IndexUpdatedProcess;
import org.elasticsearch.client.Client;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import static eu.researchalps.api.util.ApiConstants.PRODUCES_JSON;

/**
 * Temporary Class (to be removed) to create structure and index them
 */
@Controller
@RequestMapping("/admin/index")
public class IndexServiceApi {
    @Autowired
    private FullStructureRepository fullStructureRepository;
    @Autowired
    private StructureRepository structureRepository;

    @Autowired
    private IndexUpdatedProcess indexUpdatedProcess;
    @Autowired
    private QueueService queueService;


    @Autowired
    private Client elasticSearchClient;


    @ResponseBody
    @RequestMapping(value = "/all", method = RequestMethod.GET, produces = PRODUCES_JSON)
    public long indexAllStructures() {
        return fullStructureRepository.selectAllIds().peek(id -> queueService.push(id, IndexStructureProcess.QUEUE, null)).count();
    }


    @ResponseBody
    @RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = PRODUCES_JSON)
    public void indexOneStructures(@PathVariable String id) {
        queueService.push(id, IndexStructureProcess.QUEUE, null);
    }


}
