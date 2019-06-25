package eu.researchalps.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;

/**
 * Created by loic on 2019-05-20.
 * Copyright (C) by Data Publica, All Rights Reserved.
 */
@Service
public class FeatureService {
    private static final Logger log = LoggerFactory.getLogger(FeatureService.class);

    private String[] features;
    private Map<String, Integer> idx = new HashMap<>();
    private int[] df;

    public FeatureService() throws IOException {
        final ObjectMapper om = new ObjectMapper();
        log.info("Loading df file...");
        final DFFile dfFile = om.readValue(new GZIPInputStream(FeatureService.class.getResourceAsStream("/df.json.gz")), DFFile.class);
        df = dfFile.values;
        features = dfFile.keys;
        for (int i = 0; i < features.length; i++) {
            String feature = features[i];
            idx.put(feature, i);
        }
        log.info("Done! "+df.length+" features");
    }

    public double getIdf(int idx) {
        final int dfValue = df[idx];
        return Math.log((126000 - dfValue + 0.5) / (dfValue + 0.5));
    }

    public double getDf(int idx) {
        return df[idx];
    }


    public Map<String, Integer> getIdx() {
        return idx;
    }

    public String getFeature(int idx) {
        return features[idx];
    }

    public static class DFFile {
        public String[] keys;
        public int[] values;
    }
}
