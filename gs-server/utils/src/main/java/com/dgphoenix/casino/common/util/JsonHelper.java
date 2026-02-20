package com.dgphoenix.casino.common.util;

import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;

import com.dgphoenix.casino.common.cache.JsonDeserializableModule;
import com.dgphoenix.casino.common.cache.UniversalCollectionModule;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.StreamReadFeature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class JsonHelper implements InitializingBean {
    private static final String CLASS_FIELD = "_class";

    private static final Logger LOG = LogManager.getLogger(JsonHelper.class);

    private final ObjectMapper jsonMapper;

    public JsonHelper(String packageName) {
        jsonMapper = JsonMapper
            .builder()
            .configure(MapperFeature.REQUIRE_SETTERS_FOR_GETTERS, true)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .activateDefaultTypingAsProperty(
                    LaissezFaireSubTypeValidator.instance,
                    DefaultTyping.OBJECT_AND_NON_CONCRETE, CLASS_FIELD)
            .enable(StreamReadFeature.INCLUDE_SOURCE_IN_LOCATION)
            .addModule(new JavaTimeModule())
            .addModule(new JsonDeserializableModule(packageName))
            .addModule(new UniversalCollectionModule())
            .build();

        jsonMapper.setVisibility(jsonMapper.getSerializationConfig().getDefaultVisibilityChecker()
                .withFieldVisibility(Visibility.ANY)
                .withGetterVisibility(Visibility.NONE)
                .withIsGetterVisibility(Visibility.NONE)
                .withSetterVisibility(Visibility.NONE)
                .withCreatorVisibility(Visibility.NONE));
    }

    private static JsonHelper instance;

    public static JsonHelper getInstance() {
        return instance;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        instance = this;
    }

    public <T> String serializeToJson(T entity) {
        try {
            return jsonMapper.writeValueAsString(entity);
        } catch (JsonProcessingException e) {
            LOG.error("Error converting object to json", e);
        }
        return null;
    }

    public <T> String serializeWithClassToJson(T entity) {
        try {
            jsonMapper.activateDefaultTypingAsProperty(LaissezFaireSubTypeValidator.instance, DefaultTyping.NON_FINAL, CLASS_FIELD);
            return jsonMapper.writeValueAsString(entity);
        } catch (JsonProcessingException e) {
            LOG.error("Error converting object to json", e);
        } finally {
            jsonMapper.activateDefaultTypingAsProperty(LaissezFaireSubTypeValidator.instance, DefaultTyping.OBJECT_AND_NON_CONCRETE, CLASS_FIELD);
        }
        return null;
    }

    public <T> String serializeToListJson(List<T> list, Class<T> clazz) {
        try {
            return jsonMapper
                    .writerFor(jsonMapper.getTypeFactory()
                            .constructCollectionType(List.class, clazz))
                    .writeValueAsString(list);
        } catch (Exception e) {
            LOG.error("Error serializing list to json", e);
        }
        return null;
    }

    public <K, V> String serializeToMapJson(Map<K, V> map, Class<K> keyClass, Class<V> valueClass) {
        try {
            return jsonMapper
                    .writerFor(jsonMapper.getTypeFactory()
                            .constructMapLikeType(Map.class, keyClass, valueClass))
                    .writeValueAsString(map);
        } catch (Exception e) {
            LOG.error("Error serializing map to json", e);
        }
        return null;
    }

    public <T> T deserializeFromJson(String json, Class<T> klazz) {
        if (json == null) {
            return null;
        }
        try {
            return jsonMapper.readValue(json, klazz);
        } catch (Exception e) {
            LOG.error("Error converting json to object of type '" + klazz.getName() + "'", e);
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T deserializeWithClassFromJson(String json) {
        if (json == null) {
            return null;
        }
        try {
            jsonMapper.activateDefaultTypingAsProperty(LaissezFaireSubTypeValidator.instance, DefaultTyping.NON_FINAL, CLASS_FIELD);
            return (T) jsonMapper.readValue(json, Object.class);
        } catch (Exception e) {
            LOG.error("Error converting json to object", e);
            return null;
        } finally {
            jsonMapper.activateDefaultTypingAsProperty(LaissezFaireSubTypeValidator.instance, DefaultTyping.OBJECT_AND_NON_CONCRETE, CLASS_FIELD);
        }
    }

    public <T> List<T> deserializeToListJson(String json, Class<T> klazz) {
        try {
            if (json == null) {
                return null;
            }
            return jsonMapper.readValue(json, jsonMapper.getTypeFactory().constructCollectionType(List.class, klazz));
        } catch (Exception e) {
            LOG.error("Error deserializing list from json", e);
        }
        return null;
    }

    public <K, V> Map<K, V> deserializeToMapJson(String json, Class<K> keyClass, Class<V> valueClass) {
        try {
            if (json == null) {
                return null;
            }
            return jsonMapper.readValue(json, jsonMapper.getTypeFactory().constructMapLikeType(Map.class, keyClass, valueClass));
        } catch (Exception e) {
            LOG.error("Error deserializing list from json", e);
        }
        return null;
    }
}
