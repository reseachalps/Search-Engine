package eu.researchalps.api;

import eu.researchalps.api.exception.NotFoundException;
import eu.researchalps.api.util.ApiConstants;
import eu.researchalps.api.util.ApiUtil;
import eu.researchalps.db.model.FullStructureCorrection;
import eu.researchalps.db.model.full.FullStructure;
import eu.researchalps.db.model.full.FullStructureField;
import eu.researchalps.db.repository.FullStructureCorrectionRepository;
import eu.researchalps.workflow.full.FullStructureService;
import eu.researchalps.workflow.full.FullStructureTransaction;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

import static eu.researchalps.api.util.ApiConstants.OK_MESSAGE;
import static eu.researchalps.api.util.ApiConstants.PRODUCES_JSON;

/**
 * Created by loic on 12/05/2016.
 * Copyright (C) by Data Publica, All Rights Reserved.
 */
@Controller
@RequestMapping("/admin/corrections")
public class CorrectionAdminApi {

    @Autowired
    private FullStructureCorrectionRepository correctionRepository;

    @Autowired
    private FullStructureService service;

    @ResponseBody
    @RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = PRODUCES_JSON)
    public FullStructureCorrection getCorrection(@PathVariable String id) {
        if (!service.getRepository().exists(id)) {
            throw new NotFoundException("structure", id);
        }
        FullStructureCorrection correction = this.correctionRepository.findOne(id);
        return correction == null ? new FullStructureCorrection(id) : correction;
    }

    @ResponseBody
    @ApiOperation(value = "Mark a userFeedback as§ processed")
    @RequestMapping(value = "", method = RequestMethod.PUT, produces = PRODUCES_JSON)
    public ApiConstants.OK saveCorrection(@RequestBody FullStructureCorrection correction) {
        String id = correction.getId();
        if (id == null) {
            throw new IllegalArgumentException("Empty id given");
        }
        if (!service.getRepository().exists(id)) {
            throw new NotFoundException("structure", id);
        }
        correctionRepository.save(correction);

        // Ensure that all corrected fields are up to date (to avoid double shallow correction)
        try (FullStructureTransaction tx = service.tx(id, true)) {
            tx.refresh(FullStructureField.WEBSITES);
            tx.refresh(FullStructureField.DETECTED_PROJECTS);
            tx.refresh(FullStructureField.DETECTED_PUBLICATIONS);
            tx.save(true, false);
        }
        return OK_MESSAGE;
    }

    @ResponseBody
    @RequestMapping(value = "{id}/structure", method = RequestMethod.GET, produces = PRODUCES_JSON)
    public FullStructure getStructure(@PathVariable String id) throws IOException {
        return ApiUtil.fetchOrThrow(service.getRepository(), "structure", id);
    }
}
