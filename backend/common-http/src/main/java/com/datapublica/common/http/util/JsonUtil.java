package com.datapublica.common.http.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;

/**
 *
 */
public class JsonUtil {

    public static final ObjectMapper JSON_MAPPER = new ObjectMapper();
    public static final ObjectMapper JSON_MAPPER_INDENTED = new ObjectMapper();

    static {
        JSON_MAPPER_INDENTED.enable(SerializationFeature.INDENT_OUTPUT);
    }

    public static String serialize(Object o, boolean indented) {
        try {
            return (indented ? JSON_MAPPER_INDENTED : JSON_MAPPER).writeValueAsString(o);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

    public static String serialize(Object o) {
        return serialize(o, false);
    }

    public static JsonNode read(String s) {
        try {
            return JSON_MAPPER.readTree(s);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public static <E> E read(String s, Class<E> clazz) {
        try {
            return JSON_MAPPER.readValue(s, clazz);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
