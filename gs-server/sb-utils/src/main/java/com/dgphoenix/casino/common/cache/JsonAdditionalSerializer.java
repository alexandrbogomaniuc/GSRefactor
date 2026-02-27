package com.abs.casino.common.cache;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import static java.util.Objects.isNull;

public interface JsonAdditionalSerializer {
    public default void serializeRawField(JsonGenerator gen, String fieldName, String raw) throws IOException {
        gen.writeFieldName(fieldName);
        gen.writeRawValue(raw);
    }

    public default <K, V> void serializeMapField(JsonGenerator gen, String fieldName, Map<K, V> map, TypeReference<Map<K, V>> tr) throws JsonProcessingException, IOException {
        ObjectMapper om = ((ObjectMapper) gen.getCodec());
        String json = om.writerFor(tr).writeValueAsString(map);
        serializeRawField(gen, fieldName, json);
    }

    public default <LT> void serializeListField(JsonGenerator gen, String fieldName, List<LT> list, TypeReference<List<LT>> tr) throws JsonProcessingException, IOException {
        ObjectMapper om = ((ObjectMapper) gen.getCodec());
        String json = om.writerFor(tr).writeValueAsString(list);
        serializeRawField(gen, fieldName, json);
    }

    public default <ST> void serializeSetField(JsonGenerator gen, String fieldName, Set<ST> list, TypeReference<Set<ST>> tr) throws JsonProcessingException, IOException {
        ObjectMapper om = ((ObjectMapper) gen.getCodec());
        String json = om.writerFor(tr).writeValueAsString(list);
        serializeRawField(gen, fieldName, json);
    }

    public default void serializeNumberOrNull(JsonGenerator gen, String fieldName, Long num) throws IOException {
        if (num == null) {
            gen.writeNullField(fieldName);
        } else {
            gen.writeNumberField(fieldName, num);
        }
    }

    public default void serializeNumberOrNull(JsonGenerator gen, String fieldName, Integer num) throws IOException {
        if (num == null) {
            gen.writeNullField(fieldName);
        } else {
            gen.writeNumberField(fieldName, num);
        }
    }

    public default void serializeNumberOrNull(JsonGenerator gen, String fieldName, Double num) throws IOException {
        if (num == null) {
            gen.writeNullField(fieldName);
        } else {
            gen.writeNumberField(fieldName, num);
        }
    }

    public default <T> T deserializeOrNull(ObjectMapper m, JsonNode node, Class<T> klazz) throws JsonProcessingException, IllegalArgumentException {
        return isNull(node) || node.isNull() ? null : m.treeToValue(node, klazz);
    }

    public default String readNullableText(JsonNode parent, String fieldName) {
        JsonNode node = parent.get(fieldName);
        return isNull(node) || node.isNull() ? null : node.asText();
    }
}
