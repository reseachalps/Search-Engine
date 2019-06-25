package eu.researchalps.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.junit.Test;

import java.io.IOException;
import java.time.Instant;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Created by loic on 22/04/2016.
 * Copyright (C) by Data Publica, All Rights Reserved.
 */
public class TolerantDateDeserializerTest {

    public static class A {
        public Date a;
        @JsonDeserialize(using=TolerantDateDeserializer.class)
        public Date b;
    }

    private final ObjectMapper om;

    public TolerantDateDeserializerTest() {
        om = new ObjectMapper();

        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addDeserializer(Date.class, new TolerantDateDeserializer());
        om.registerModule(simpleModule);
    }

    @Test
    public void testDateParse() throws IOException {
        assertNull(om.readValue("null", Date.class));
        assertEquals("2012-01-01T00:00:00Z", om.readValue("\"2012\"", Date.class).toInstant().toString());
        assertEquals("2012-07-01T00:00:00Z", om.readValue("\"2012-07\"", Date.class).toInstant().toString());
        assertEquals("2012-07-12T00:00:00Z", om.readValue("\"2012-07-12\"", Date.class).toInstant().toString());
        assertEquals("2012-07-12T14:42:02Z", om.readValue("\"2012-07-12T14:42:02\"", Date.class).toInstant().toString());
        assertEquals("2012-07-12T14:42:02Z", om.readValue("\"2012-07-12 14:42:02\"", Date.class).toInstant().toString());
        assertEquals("2012-07-12T14:42:02Z", om.readValue("\"2012-07-12T14:42:02Z\"", Date.class).toInstant().toString());
        assertEquals("2012-07-12T14:42:02Z", om.readValue("\"2012-07-12 14:42:02Z\"", Date.class).toInstant().toString());
        assertEquals("2012-07-12T13:42:02Z", om.readValue("\"2012-07-12T14:42:02+0100\"", Date.class).toInstant().toString());
        assertEquals("2012-07-12T13:42:02Z", om.readValue("\"2012-07-12T14:42:02+01:00\"", Date.class).toInstant().toString());
        assertEquals("2012-01-01T00:00:00Z", om.readValue("{\"a\":\"2012\"}", A.class).a.toInstant().toString());
        assertEquals("2012-01-01T00:00:00Z", new ObjectMapper().readValue("{\"b\":\"2012\"}", A.class).b.toInstant().toString());
        assertEquals("2012-07-12T13:42:02Z", om.readValue(String.valueOf(Instant.parse("2012-07-12T13:42:02Z").toEpochMilli()), Date.class).toInstant().toString());
    }
}