package eu.researchalps.util;/*
 * Copyright (C) by Data Publica, All Rights Reserved.
 */

import eu.researchalps.db.model.Structure;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class StructureDeserializationTest {
    @Test
    public void testGeoDeserialization() throws IOException {
        String json = " {\n" +
                "        \"id\": \"057817314\",\n" +
                "        \"label\": \"OGEC CHEVREUL DE LA BLANCARDE\",\n" +
                "        \"acronym\": null,\n" +
                "        \"creationYear\": 1957,\n" +
                "        \"parent\": null,\n" +
                "        \"links\": null,\n" +
                "        \"type\": {\n" +
                "            \"id\": \"ISBL\",\n" +
                "            \"label\": \"Institution sans but lucratif\",\n" +
                "            \"isPublic\": true\n" +
                "        },\n" +
                "        \"companyType\": {\n" +
                "            \"id\": \"9220\",\n" +
                "            \"label\": \"Association déclarée \"\n" +
                "        },\n" +
                "        \"address\": {\n" +
                "            \"address\": \"1 Rue St Francois de Sales\",\n" +
                "            \"postcode\": \"13004\",\n" +
                "            \"city\": \"Marseille \",\n" +
                "            \"urbanUnitCode\": \"00759\",\n" +
                "            \"urbanUnit\": \"Marseille - Aix-en-Provence\",\n" +
                "            \"gps\": {\n" +
                "                \"lat\": 43.3025,\n" +
                "                \"lon\": 5.4017\n" +
                "            }\n" +
                "        }}";
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        Structure structure = objectMapper.readValue(json, Structure.class);
        assertEquals(5.4017, structure.getAddress().getGps().getX(), 0.1);
        assertEquals(43.3025, structure.getAddress().getGps().getY(), 0.1);
    }

    @Test
    public void testBadgeDeserialization() throws IOException {
        String json = "    {\n" +
                "        \"id\": \"193719314X\",\n" +
                "        \"nature\": \"Structure propre organisme\",\n" +
                "        \"level\": 2,\n" +
                "        \"levelLabel\": \"Unité\",\n" +
                "        \"creationYear\": 1939,\n" +
                "        \"label\": \"Institut de Recherche et d'Histoire des Textes\",\n" +
                "        \"acronym\": \"IRHT\",\n" +
                "        \"type\": {\n" +
                "            \"id\": \"UR\",\n" +
                "            \"label\": \"Unité de recherche\"\n" +
                "        },\n" +
                "        \"address\": {\n" +
                "            \"address\": \"40 avenue d'Iéna\",\n" +
                "            \"postcode\": \"75116\",\n" +
                "            \"city\": \"Paris 16e\",\n" +
                "            \"citycode\": \"75116\",\n" +
                "            \"urbanUnitCode\": \"00851\",\n" +
                "            \"urbanUnit\": \"Paris 16e\",\n" +
                "            \"gps\": {\n" +
                "                \"lat\": 48.8665,\n" +
                "                \"lon\": 2.2953\n" +
                "            }\n" +
                "        },\n" +
                "        \"tags\": [\n" +
                "            \"anr\",\n" +
                "            \"pcrdt\"\n" +
                "        ]\n" +
                "    }";
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        Structure structure = objectMapper.readValue(json, Structure.class);
        assertEquals(2, structure.getBadges().size());
        assertEquals("anr", structure.getBadges().stream().filter(b -> b.getCode().equals("anr")).findFirst().get().getLabel());
    }

    @Test
    public void testBadgeDeserialization2() throws IOException {
        String json = "{\n" +
                "        \"id\": \"193719314X\",\n" +
                "        \"nature\": \"Structure propre organisme\",\n" +
                "        \"level\": 2,\n" +
                "        \"levelLabel\": \"Unité\",\n" +
                "        \"creationYear\": 1939,\n" +
                "        \"label\": \"Institut de Recherche et d'Histoire des Textes\",\n" +
                "        \"acronym\": \"IRHT\",\n" +
                "        \"type\": {\n" +
                "            \"id\": \"UR\",\n" +
                "            \"label\": \"Unité de recherche\"\n" +
                "        },\n" +
                "        \"address\": {\n" +
                "            \"address\": \"40 avenue d'Iéna\",\n" +
                "            \"postcode\": \"75116\",\n" +
                "            \"city\": \"Paris 16e\",\n" +
                "            \"citycode\": \"75116\",\n" +
                "            \"urbanUnitCode\": \"00851\",\n" +
                "            \"urbanUnit\": \"Paris 16e\",\n" +
                "            \"gps\": {\n" +
                "                \"lat\": 48.8665,\n" +
                "                \"lon\": 2.2953\n" +
                "            }\n" +
                "        },\n" +
                "        \"tags\": [\n" +
                "            \"pcrdt\"\n" +
                "        ],\n" +
                "        \"badges\": [\n" +
                "            {\"code\":\"anr\", \"label\":\"ANR\"}\n" +
                "        ]\n" +
                "    }";
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        Structure structure = objectMapper.readValue(json, Structure.class);
        assertEquals(1, structure.getBadges().size());
        assertEquals("ANR", structure.getBadges().stream().filter(b -> b.getCode().equals("anr")).findFirst().get().getLabel());
    }

}
