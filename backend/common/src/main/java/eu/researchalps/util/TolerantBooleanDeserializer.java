package eu.researchalps.util;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonTokenId;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

/**
 * Created by loic on 22/04/2016.
 * Copyright (C) by Data Publica, All Rights Reserved.
 */
public class TolerantBooleanDeserializer extends JsonDeserializer<Boolean> {

    @Override
    public Boolean deserialize(JsonParser jp, DeserializationContext deserializationContext) throws IOException {
        if (jp.getCurrentTokenId() == JsonTokenId.ID_STRING) {
            // parse string
            String toParse = jp.getText();
            if ("true".equals(toParse)) {
                return true;
            } else if ("false".equals(toParse)) {
                return false;
            } else {
                return null;
            }
        } else if (jp.getCurrentTokenId() == JsonTokenId.ID_TRUE) {
            return true;
        } else if (jp.getCurrentTokenId() == JsonTokenId.ID_FALSE) {
            return false;
        } else if (jp.getCurrentTokenId() == JsonTokenId.ID_NUMBER_INT) {
            return jp.getIntValue() == 1;
        } else if (jp.getCurrentTokenId() == JsonTokenId.ID_NULL) {
            return null;
        }
        throw new JsonParseException("Cannot parse boolean from non string", jp.getCurrentLocation());
    }
}
