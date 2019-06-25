package eu.researchalps.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.Arrays;

/**
 * Created by loic on 27/02/2019.
 * Copyright (C) by Data Publica, All Rights Reserved.
 */
@Configuration
public class HostConfiguration {
    @Value("${http.base:http://localhost:8080}")
    private String baseURL;

    public String getBaseURL() {
        return baseURL;
    }

    @Bean
    @Primary
    public ObjectMapper springBootJackson() {
        return new ObjectMapper();
    }
}
