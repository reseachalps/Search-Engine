package eu.researchalps.api;

import eu.researchalps.config.CaptchaConfiguration;
import eu.researchalps.db.model.UserFeedback;
import eu.researchalps.db.repository.UserFeedbackRepository;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import springfox.documentation.annotations.ApiIgnore;

import java.io.IOException;

/**
 * Copyright (C) by Data Publica, All Rights Reserved.
 */
@ApiIgnore
@Controller
@RequestMapping("/feedback")
public class UserFeedbackApi {

    @Autowired
    private UserFeedbackRepository userFeedbackRepository;

    @Autowired
    private CaptchaConfiguration captchaConfiguration;

    @ResponseBody
    @ApiOperation(value = "Create a userFeedback", notes = "Don't provide any id to create one")
    @RequestMapping(method = RequestMethod.PUT, produces = "application/json;charset=UTF-8")
    public UserFeedback addUserFeedback(@RequestBody UserFeedbackDTO feedbackDTO) throws IOException {
        // check the code
        if (captchaConfiguration.checkCaptcha(feedbackDTO.verificationCode)) {
            // if code correct save the feedback
            return userFeedbackRepository.save(feedbackDTO.feedback);
        } else throw new IllegalArgumentException("Invalid Captcha verification");
    }


    private static class UserFeedbackDTO {
        public UserFeedback feedback;
        public String verificationCode;
    }
}
