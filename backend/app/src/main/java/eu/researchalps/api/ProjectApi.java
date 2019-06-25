package eu.researchalps.api;

import eu.researchalps.api.util.ApiConstants;
import eu.researchalps.db.model.Project;
import eu.researchalps.db.repository.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Created by loic on 25/04/2016.
 * Copyright (C) by Data Publica, All Rights Reserved.
 */


@Controller
@RequestMapping("/projects/")
public class ProjectApi {
    @Autowired
    private ProjectRepository projectRepository;

    @ResponseBody
    @RequestMapping(value = "{id}", method = RequestMethod.GET, produces = ApiConstants.PRODUCES_JSON)
    public Project get(@PathVariable String id) {
        return projectRepository.findOne(id);
    }
}
