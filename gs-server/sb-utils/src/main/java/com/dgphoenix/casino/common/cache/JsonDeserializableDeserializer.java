package com.dgphoenix.casino.common.cache;

import java.io.IOException;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonDeserializableDeserializer<T extends JsonDeserializable<T>> extends JsonDeserializer<T> {
    public static final Logger LOG = LogManager.getLogger(JsonDeserializableDeserializer.class);

    private final Class<T> type;

    public JsonDeserializableDeserializer(Class<T> type) {
        this.type = type;
    }

    @Override
    public T deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        try {
            return type.getDeclaredConstructor().newInstance().deserializeObject(p, ctxt);
        } catch (Exception e) {
            LOG.error("Unable to deserialize object. Using default deserialization. ", e);
            return new ObjectMapper()
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                    .readValue(p, type);
        }
    }
}
