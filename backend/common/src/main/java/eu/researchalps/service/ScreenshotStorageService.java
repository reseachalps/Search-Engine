package eu.researchalps.service;

import eu.researchalps.api.exception.NotFoundException;
import eu.researchalps.config.ScreenshotConfiguration;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by loic on 08/03/2016.
 * Copyright (C) by Data Publica, All Rights Reserved.
 */
@Service
public class ScreenshotStorageService {
    @Autowired
    private ScreenshotConfiguration configuration;

    public void store(String websiteId, byte[] screenshot) {
        File f = asFile(websiteId);

        try (FileOutputStream fos = new FileOutputStream(f, false)) {
            fos.write(screenshot);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public byte[] get(String websiteId) throws NotFoundException {
        File f = asFile(websiteId);
        if (!f.exists()) throw new NotFoundException("screenshot", websiteId);

        try (FileInputStream fos = new FileInputStream(f)) {
            return IOUtils.toByteArray(fos);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public boolean exists(String websiteId) {
        return asFile(websiteId).exists();
    }

    protected File asFile(String websiteId) {
        if (websiteId.length() > 150) {
            websiteId = websiteId.substring(0, 150);
        }
        String filename = Base64Utils.encodeToUrlSafeString(websiteId.getBytes(Charsets.UTF_8));
        return new File(configuration.getStoragePath(), filename);
    }
}
