package com.abs.casino.common.cache;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;

public interface JsonDeserializable<T> {
    T deserializeObject(JsonParser p, DeserializationContext ctxt) throws IOException;
}
