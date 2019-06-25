package eu.researchalps.config;/*
 * Copyright (C) by Data Publica, All Rights Reserved.
 */

import com.datapublica.common.http.DPHttpClient;
import com.datapublica.common.http.DPHttpResponse;
import com.datapublica.common.http.impl.DPHttpClientImpl;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Class to verify captcha code.
 * See https://developers.google.com/recaptcha/docs/verify#api-request
 */
@Configuration
public class CaptchaConfiguration {

    @Value("${captcha.key:}")
    private String captchaKey;

    @Value("${captcha.verif.url:https://www.google.com/recaptcha/api/siteverify}")
    private String captchaVerificationUrl;

    private final ObjectMapper om;
    private final DPHttpClient client;

    public CaptchaConfiguration() {
        om = new ObjectMapper();
        om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        client = new DPHttpClientImpl();
    }


    public boolean checkCaptcha(String value) throws IOException {
        HttpPost post = new HttpPost(captchaVerificationUrl);

        // TODO: add ip of the user
        List<NameValuePair> postParameters = Arrays.asList(
                new BasicNameValuePair("secret", captchaKey),
                new BasicNameValuePair("response", value));
        post.setEntity(new UrlEncodedFormEntity(postParameters));
        DPHttpResponse response = client.execute(post);

        CaptchaVerificationResponse verificationResponse = om.readValue(response.text(), CaptchaVerificationResponse.class);
        return verificationResponse.success;
    }

    private static class CaptchaVerificationResponse {
        public boolean success;
        private String hostname;
        @JsonProperty("error-codes")
        private List<String> errorCodes;
    }

}
