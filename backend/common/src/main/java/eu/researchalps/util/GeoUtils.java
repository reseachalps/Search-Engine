package eu.researchalps.util;/*
 * Copyright (C) by Data Publica, All Rights Reserved.
 */

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class GeoUtils {
    private static final Logger log = LoggerFactory.getLogger(GeoUtils.class);

    public static Map<String, String> DEPARTEMENT_LABELS;

    static {
        DEPARTEMENT_LABELS = new HashMap<>();
        try (InputStreamReader in = new InputStreamReader(GeoUtils.class.getResourceAsStream("dept2016.csv"))) {
            for (CSVRecord record : CSVFormat.DEFAULT.parse(in)) {
                DEPARTEMENT_LABELS.put(record.get(0), record.get(1));
            }
        } catch (IOException e) {
            log.error("Cannot parse dpt labels", e);

        }
    }

    /**
     * Get departement from postal code
     *
     * @param postcode
     * @return
     */
    public static String secondLevelFromPostcode(String postcode) {
        if (postcode == null || !postcode.matches("[0-9]{5}|97[0-9]|98[0-9]")) {
            // Invalid pattern
            return null;
        }
        if (postcode.startsWith("97") || postcode.startsWith("98")) {
            return postcode.substring(0, 3);
        }
        if (postcode.startsWith("20")) {
            // All 200xx and 201xx are '2A'
            if (postcode.charAt(2) < '2')
                return "2A";
            return "2B";
        }
        return postcode.substring(0, 2);
    }
}
