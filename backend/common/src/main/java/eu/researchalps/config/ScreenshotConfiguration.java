package eu.researchalps.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.File;

/**
 * Created by loic on 08/03/2016.
 * Copyright (C) by Data Publica, All Rights Reserved.
 */
@Configuration
public class ScreenshotConfiguration {

    @Value("${screenshot.storage:/tmp/screenshots}")
    private String storagePath;

    @PostConstruct
    private void init() {
        File dir = new File(storagePath);
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                throw new RuntimeException("Cannot storage directory "+storagePath);
            }
        }
    }

    public String getStoragePath() {
        return storagePath;
    }
}
