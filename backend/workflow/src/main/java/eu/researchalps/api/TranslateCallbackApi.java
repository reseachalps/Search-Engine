package eu.researchalps.api;

import eu.researchalps.api.util.ApiConstants;
import eu.researchalps.workflow.translate.TranslateJobService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;

/**
 * Created by loic on 22/02/2019.
 * Copyright (C) by Data Publica, All Rights Reserved.
 */
@Controller
@RequestMapping(TranslateCallbackApi.API_PREFIX)
public class TranslateCallbackApi {
    private static final Logger log = LoggerFactory.getLogger(TranslateCallbackApi.class);
    public static final String API_PREFIX = "/translations/callbacks";
    public static final String SUCCESS_ROUTE = "/success";
    public static final String ERROR_ROUTE = "/error";
    public static final String RECEIVE_ROUTE = "/receive";

    @Autowired
    private TranslateJobService translateJobService;

    @ResponseBody
    @RequestMapping(value = SUCCESS_ROUTE, method = RequestMethod.POST, produces = "application/json; charset=UTF-8")
    public ApiConstants.OK success(@RequestParam("request-id") String requestId, @RequestParam("target-language") String targetLanguage) {
        log.info("Successfully executed the translation request {} in language {}", requestId, targetLanguage);
        return ApiConstants.OK_MESSAGE;
    }

    @ResponseBody
    @RequestMapping(value = ERROR_ROUTE, method = RequestMethod.POST, produces = "application/json; charset=UTF-8")
    public ApiConstants.OK error(@RequestParam("request-id") Long requestId, @RequestParam("target-language") String targetLanguage, @RequestBody byte[] content) {
        log.error("Could not complete the translation request {} in language {}", requestId, targetLanguage);
        translateJobService.handleError(requestId, content);
        return ApiConstants.OK_MESSAGE;
    }

    @ResponseBody
    @RequestMapping(value = RECEIVE_ROUTE, method = RequestMethod.POST, produces = "application/json; charset=UTF-8")
    public ApiConstants.OK receive(@RequestParam("request-id") Long requestId, @RequestParam("target-language") String targetLanguage, @RequestParam("external-reference") String externalId, @RequestBody byte[] content) {
        final byte[] decodedContent = Base64.getDecoder().decode(content);
        log.info("Successfully received the translation request {} in language {}: {} bytes", requestId, targetLanguage, decodedContent.length);
        translateJobService.callback(targetLanguage, externalId, decodedContent, requestId);
        return ApiConstants.OK_MESSAGE;
    }
}
