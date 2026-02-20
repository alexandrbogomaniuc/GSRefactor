package com.dgphoenix.casino.common.cache;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.KeyDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.deser.Deserializers;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.MapType;

@SuppressWarnings({"unchecked", "rawtypes"})
public class UniversalCollectionModule extends SimpleModule {
    private static final long serialVersionUID = 5165532096082290786L;

    @Override
    public void setupModule(SetupContext context) {
        super.setupModule(context);
        context.addDeserializers(new UniversalCollectionDeserializers());
        context.addDeserializers(new UniversalMapDeserializers());
    }
    
    public static class UniversalCollectionDeserializers extends Deserializers.Base {
        private final JsonDeserializer<?> delegateCol = new UniversalCollectionDeserializer();

        @Override
        public JsonDeserializer<?> findCollectionDeserializer(
                CollectionType type,
                DeserializationConfig config,
                BeanDescription beanDesc,
                TypeDeserializer elementTypeDeserializer,
                JsonDeserializer<?> elementDeserializer
        ) {
            return delegateCol;
        }
    }

    public static class UniversalMapDeserializers extends Deserializers.Base {
        @Override
        public JsonDeserializer<?> findMapDeserializer(MapType type,
                                                       DeserializationConfig config,
                                                       BeanDescription beanDesc,
                                                       KeyDeserializer keyDeserializer,
                                                       TypeDeserializer elementTypeDeserializer,
                                                       JsonDeserializer<?> elementDeserializer)
                throws JsonMappingException {
            return new UniversalMapDeserializer(
                    type.getKeyType(),
                    type.getContentType(),
                    (Class<? extends Map>) type.getRawClass()
                );
        }
    }

    public static class UniversalCollectionDeserializer extends JsonDeserializer<Collection<?>>
            implements ContextualDeserializer {

        private final JavaType valueType;
        private final Class<? extends Collection> collectionImpl;

        public UniversalCollectionDeserializer() {
            this(null, null);
        }

        public UniversalCollectionDeserializer(JavaType valueType,
                Class<? extends Collection> impl) {
            this.valueType = valueType;
            this.collectionImpl = impl;
        }

        @Override
        public JsonDeserializer<?> createContextual(DeserializationContext ctxt,
                                                    BeanProperty property)
                throws JsonMappingException {

            JavaType contextualType = ctxt.getContextualType();
            if (contextualType == null
                    || !Collection.class.isAssignableFrom(contextualType.getRawClass())) {
                return this;
            }

            JavaType elementType = contextualType.getContentType();

            Class<? extends Collection> impl;
            if (contextualType.getRawClass().isInterface()) {
                if (List.class.isAssignableFrom(contextualType.getRawClass()))
                    impl = ArrayList.class;
                else if (Set.class.isAssignableFrom(contextualType.getRawClass()))
                    impl = HashSet.class;
                else
                    impl = ArrayList.class;
            } else {
                impl = (Class<? extends Collection>) contextualType.getRawClass();
            }

            return new UniversalCollectionDeserializer(elementType, impl);
        }

        @Override
        public Collection<?> deserialize(JsonParser p, DeserializationContext ctxt)
                throws IOException {
            JsonNode root = p.readValueAsTree();

            // Detect polymorphic wrapper format: [ "java.util.ArrayList", [ {...}, {...} ] ]
            if (root.isArray() && root.size() == 2 && root.get(0).isTextual()
                    && root.get(1).isArray()) {
                root = root.get(1); // unwrap actual elements
            }

            Collection<Object> result;
            try {
                result = collectionImpl.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new IOException("Failed to instantiate " + collectionImpl.getName(), e);
            }

            ObjectMapper mapper = (ObjectMapper) p.getCodec();
            for (JsonNode elem : root) {
                Object value = mapper.treeToValue(elem, valueType);
                result.add(value);
            }

            return result;
        }

        @Override
        public Object deserializeWithType(JsonParser p,
                                          DeserializationContext ctxt,
                                          TypeDeserializer typeDeserializer)
                throws IOException {
            return deserialize(p, ctxt);
        }
    }
    
    public static class UniversalMapDeserializer extends JsonDeserializer<Map<?, ?>>
            implements ContextualDeserializer {

        private final JavaType keyType;
        private final JavaType valueType;
        private final Class<? extends Map> mapImpl;

        public UniversalMapDeserializer() {
            this(null, null, null);
        }

        public UniversalMapDeserializer(JavaType keyType,
                JavaType valueType,
                Class<? extends Map> mapImpl) {
            this.keyType = keyType;
            this.valueType = valueType;
            this.mapImpl = mapImpl;
        }

        @Override
        public JsonDeserializer<?> createContextual(DeserializationContext ctxt,
                                                    BeanProperty property)
                throws JsonMappingException {

            JavaType contextualType = ctxt.getContextualType();
            if (contextualType == null
                    || !Map.class.isAssignableFrom(contextualType.getRawClass())) {
                return this;
            }

            JavaType kType = contextualType.getKeyType();
            JavaType vType = contextualType.getContentType();

            Class<? extends Map> impl;
            if (contextualType.getRawClass().isInterface()) {
                impl = HashMap.class;
            } else {
                impl = (Class<? extends Map>) contextualType.getRawClass();
            }

            return new UniversalMapDeserializer(kType, vType, impl);
        }

        @Override
        public Map<?, ?> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            JsonNode node = p.readValueAsTree();

            // Handle wrapper forms
            if (node.isArray() && node.size() == 2 && node.get(0).isTextual()
                    && node.get(1).isObject()) {
                node = node.get(1); // unwrap map contents
            } else if (node.has("_class")) {
                ((ObjectNode) node).remove("_class"); // remove class wrapper if present
            }

            ObjectMapper mapper = (ObjectMapper) p.getCodec();
            Map<Object, Object> result;
            if (EnumMap.class.isAssignableFrom(mapImpl)) {
                if (!keyType.isEnumType()) {
                    throw new IllegalStateException("EnumMap requires enum key type");
                }
                result = new EnumMap(keyType.getRawClass());
            } else {
                try {
                    result = (Map<Object, Object>) mapImpl.getDeclaredConstructor().newInstance();
                } catch (Exception e) {
                    throw new IOException("Failed to create map of type: " + mapImpl, e);
                }
            }

            Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                Object key = deserializeKey(entry.getKey(), mapper);
                Object value = mapper.treeToValue(entry.getValue(), valueType);
                result.put(key, value);
            }

            return result;
        }

        public Object deserializeKey(String key, ObjectMapper mapper) throws IOException {
            if (key == null || !key.contains("[") || !key.endsWith("]")) {
                return mapper.convertValue(key, keyType); // fallback to default
            }

            int bracketStart = key.indexOf('[');
            String props = key.substring(bracketStart + 1, key.length() - 1); // strip brackets

            // Parse key string like "shots=10, type=Bomb"
            Map<String, String> fieldMap = new LinkedHashMap<>();
            for (String pair : props.split(",\\s*")) {
                String[] kv = pair.split("=");
                if (kv.length == 2) {
                    fieldMap.put(kv[0], kv[1]);
                }
            }

            // Convert to JSON
            ObjectNode node = mapper.createObjectNode();
            fieldMap.forEach(node::put);

            return mapper.convertValue(node, keyType);
        }

        @Override
        public Object deserializeWithType(JsonParser p,
                                          DeserializationContext ctxt,
                                          TypeDeserializer typeDeserializer)
                throws IOException {
            return deserialize(p, ctxt);
        }
    }
}
