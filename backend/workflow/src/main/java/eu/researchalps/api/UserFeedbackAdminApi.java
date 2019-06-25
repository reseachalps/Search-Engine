package eu.researchalps.api;

import eu.researchalps.api.util.ApiUtil;
import eu.researchalps.db.model.UserFeedback;
import eu.researchalps.db.model.UserFeedbackStatus;
import eu.researchalps.db.repository.UserFeedbackRepository;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import static eu.researchalps.api.util.ApiConstants.PRODUCES_JSON;

/**
 * Copyright (C) by Data Publica, All Rights Reserved.
 */
@Controller
@RequestMapping("/admin/feedback")
public class UserFeedbackAdminApi {

    @Autowired
    private UserFeedbackRepository userFeedbackRepository;

    @ResponseBody
    @RequestMapping(method = RequestMethod.GET, produces = PRODUCES_JSON)
    @ApiOperation(value = "Get list of user feedbacks", notes = "Retrive the user feedbacks (active=true to retrieve only unprocessed feedbacks)")
    public Page<UserFeedback> getUserFeedbacks(@RequestParam(required = false, defaultValue = "1") int page,
                                               @RequestParam(required = false, defaultValue = "1000") int size,
                                               @RequestParam(required = false, defaultValue = "1") boolean active) {
        if (active)
            return userFeedbackRepository.findByStatus(UserFeedbackStatus.SUBMITTED, new PageRequest(page - 1, size, Sort.Direction.DESC, "creationDate"));
        else
            return userFeedbackRepository.findAll(new PageRequest(page - 1, size, Sort.Direction.DESC, "creationDate"));
    }

    @ResponseBody
    @RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = PRODUCES_JSON)
    public UserFeedback getFeedback(@PathVariable String id) {
        return ApiUtil.fetchOrThrow(this.userFeedbackRepository, "userFeedback", id);
    }

    @ResponseBody
    @ApiOperation(value = "Mark a userFeedback as processed")
    @RequestMapping(value = "/{id}/markProcessed", method = RequestMethod.POST, produces = PRODUCES_JSON)
    public UserFeedback markFeedbackAsProcessed(@PathVariable String id) {
        UserFeedback userFeedback = ApiUtil.fetchOrThrow(this.userFeedbackRepository, "userFeedback", id);
        userFeedback.markProcessed();
        return userFeedbackRepository.save(userFeedback);
    }

    @ResponseBody
    @ApiOperation(value = "Create a userFeedback", notes = "Don't provide any id to create one")
    @RequestMapping(method = RequestMethod.PUT, produces = PRODUCES_JSON)
    public UserFeedback addUserFeedback(@RequestBody UserFeedback feedback) {
        return userFeedbackRepository.save(feedback);
    }
}
