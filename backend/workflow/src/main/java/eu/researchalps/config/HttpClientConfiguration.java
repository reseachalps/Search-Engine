package eu.researchalps.config;

import com.datapublica.common.http.DPHttpClient;
import com.datapublica.common.http.impl.DPHttpClientImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by loic on 16/02/2016.
 * Copyright (C) by Data Publica, All Rights Reserved.
 */
@Configuration
public class HttpClientConfiguration {
    @Bean
    public DPHttpClient dpHttpClient() {
        return new DPHttpClientImpl();
    }
}
