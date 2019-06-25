package db;/*
 * Copyright (C) by Data Publica, All Rights Reserved.
 */

import eu.researchalps.db.model.StructureFinance;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class StructureFinanceTest {
    @Test
    public void testGeoDeserialization() throws IOException {
        String json = "{\n" +
                "    \"employeesField\": \"Plein d'employés\",\n" +
                "    \"employeesCategory\": 83,\n" +
                "    \"ecRatio\": 0.3735,\n" +
                "    \"researchersPayroll\": [\n" +
                "        {\n" +
                "            \"id\": \"180089013\",\n" +
                "            \"label\": \"Centre national de la recherche scientifique (CNRS)\",\n" +
                "            \"url\": \"scanr/structure/180089013\",\n" +
                "            \"texte_survol\": null\n" +
                "        },\n" +
                "        {\n" +
                "            \"id\": \"196917751\",\n" +
                "            \"label\": \"Université Lumière - Lyon 2\",\n" +
                "            \"url\": \"scanr/structure/196917751\",\n" +
                "            \"texte_survol\": null\n" +
                "        }\n" +
                "    ],\n" +
                "    \"hdr\": 14,\n" +
                "    \"domainRatios\": [\n" +
                "        {\n" +
                "            \"id\": \"SHS6_1 Histoire\",\n" +
                "            \"label\": \"Histoire\"\n" +
                "        }\n" +
                "    ]\n" +
                "}";
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        StructureFinance structureFiance = objectMapper.readValue(json, StructureFinance.class);
        assertEquals("Plein d'employés", structureFiance.getEmployeesField());
        assertEquals("25 - 50 % de chercheurs et enseignants-chercheurs", structureFiance.getEcField());
        assertEquals("HDR : + de 10 personnes", structureFiance.getHdrField());
    }
}
