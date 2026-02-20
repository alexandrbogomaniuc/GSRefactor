package com.dgphoenix.casino.controller.frbonus;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

public class LongDateDeserializerTest {
    private final ObjectMapper mapper = new ObjectMapper();
    private final LongDateDeserializer deserializer = new LongDateDeserializer();

    @Test
    public void deserialize() throws IOException {
        String json = "{\"date\":\"2017-04-26 12:24:35\"}";
        LocalDateTime ldt = LocalDateTime.parse("2017-04-26T12:24:35");
        long expectedDate = ldt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

        Long deserializeDate = deserializeDate(json);

        assertThat("result must be long", deserializeDate, instanceOf(Long.class));
        assertThat("value must match the passed string", deserializeDate, is(equalTo(expectedDate)));
    }

    @Test(expected = IOException.class)
    public void throwExceptionWhenInvalidValue() throws IOException {
        String json = "{\"date\":\"2017-14-26 12:24:35\"}";
        deserializeDate(json);
    }

    private Long deserializeDate(String json) throws IOException {
        JsonParser parser = mapper.getFactory().createParser(json);
        DeserializationContext ctxt = mapper.getDeserializationContext();
        parser.nextToken();
        parser.nextToken();
        parser.nextToken();
        return deserializer.deserialize(parser, ctxt);
    }
}