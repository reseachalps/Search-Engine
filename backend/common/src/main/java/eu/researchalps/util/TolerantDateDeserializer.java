package eu.researchalps.util;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonTokenId;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.util.ISO8601Utils;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by loic on 22/04/2016.
 * Copyright (C) by Data Publica, All Rights Reserved.
 */
public class TolerantDateDeserializer extends JsonDeserializer<Date> {

    public static final List<DateFormat> FORMATS =
            Stream.of(
                    "EEE MMM dd HH:mm:ss zzz yyyy", // date toString()
                    "yyyy-MM-dd'T'hh:mm:ss", "yyyy-MM-dd hh:mm:ss", // No timezone specified
                    "yyyy-MM-dd", "yyyy-MM", "yyyy/MM", "yyyy" // Partial formats
            ).map(SimpleDateFormat::new).peek(it -> it.setTimeZone(TimeZone.getTimeZone("UTC"))).collect(Collectors.toList());

    @Override
    public Date deserialize(JsonParser jp, DeserializationContext deserializationContext) throws IOException {
        if (jp.getCurrentTokenId() == JsonTokenId.ID_STRING) {
            // parse string
            String toParse = jp.getText();
            if (toParse.indexOf('T') > 0) {
                // There is a T, try to parse a standard ISO
                try {
                    return ISO8601Utils.parse(toParse, new ParsePosition(0));
                } catch (ParseException ignored) {
                }
            }
            // Else, try the alternative formats
            for (DateFormat format : FORMATS) {
                try {
                    return format.parse(toParse);
                } catch (NumberFormatException | ParseException ignored) {
                }
            }
            throw new JsonParseException("Cannot parse date for string '" + toParse + "'", jp.getCurrentLocation());
        } else if (jp.getCurrentTokenId() == JsonTokenId.ID_NUMBER_INT) {
            // timestamp
            return new Date(jp.getNumberValue().longValue());
        }
        throw new JsonParseException("Cannot parse Date from non string or number", jp.getCurrentLocation());
    }
}
