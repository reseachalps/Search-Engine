package eu.researchalps.util;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonTokenId;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by loic on 22/04/2016.
 * Copyright (C) by Data Publica, All Rights Reserved.
 */
public class YearFromDateDeserializer extends JsonDeserializer<Integer> {

    public static final Pattern FIND_YEAR = Pattern.compile("(?:.*\\D|^)(\\d{4})(?:\\D.*|$)");
    private TolerantDateDeserializer dateDeserializer = new TolerantDateDeserializer();

    @Override
    public Integer deserialize(JsonParser jp, DeserializationContext deserializationContext) throws IOException {
        if (jp.getCurrentTokenId() == JsonTokenId.ID_STRING) {
            try {
                Date date = dateDeserializer.deserialize(jp, deserializationContext);
                return date.toInstant().atZone(deserializationContext.getTimeZone().toZoneId()).getYear();
            } catch (JsonParseException jpe) {
                String toParse = jp.getText();
                Matcher matcher = FIND_YEAR.matcher(toParse);
                if (matcher.find()) {
                    return Integer.parseInt(matcher.group(1));
                }
                return null;
            }
        } else if (jp.getCurrentTokenId() == JsonTokenId.ID_NUMBER_INT) {
            // timestamp
            return jp.getNumberValue().intValue();
        }
        throw new JsonParseException("Cannot parse year from non string or number", jp.getCurrentLocation());
    }
}
