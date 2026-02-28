package com.betsoft.casino.utils;

import com.abs.casino.common.util.ReflectionUtils;
import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * User: flsh
 * Date: 03.06.17.
 */
public class GsonClassSerializer implements JsonSerializer<TObject>, JsonDeserializer<TObject> {
    private static final String CLASS_PROPERTY_NAME = "class";
    private final Gson gson;
    private final Map<String, Class> nameToClassMap = new HashMap<>();

    public GsonClassSerializer() {
        gson = new Gson();
    }

    public GsonClassSerializer(Gson gson) {
        this.gson = gson;
    }

    public synchronized void register(Class klass) {
        String alias = klass.getSimpleName();
        Class registered = nameToClassMap.get(alias);
        if(registered != null) {
            throw new IllegalArgumentException("Already registered alias: " + alias + ", class=" + registered);
        }
        nameToClassMap.put(alias, klass);
    }

    public synchronized void register(String alias, Class klass) {
        Class registered = nameToClassMap.get(alias);
        if(registered != null) {
            throw new IllegalArgumentException("Already registered alias: " + alias + ", class=" + registered);
        }
        nameToClassMap.put(alias, klass);
    }

    @Override
    public JsonElement serialize(TObject src, Type typeOfSrc, JsonSerializationContext context) {
        JsonElement retValue = gson.toJsonTree(src);
        if (retValue.isJsonObject()) {
            retValue.getAsJsonObject().addProperty(CLASS_PROPERTY_NAME, src.getClassName());
        }
        return retValue;
    }

    @Override
    public TObject deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        Class actualClass;
        if (json.isJsonObject()) {
            JsonObject jsonObject = json.getAsJsonObject();
            String className = jsonObject.get(CLASS_PROPERTY_NAME).getAsString();
            try {
                Class registeredClass = nameToClassMap.get(className);
                actualClass = registeredClass != null ? registeredClass
                        : ReflectionUtils.forNameWithCompatibilityAliases(className);
            } catch (ClassNotFoundException e) {
                throw new JsonParseException(e);
            }
        } else {
            actualClass = typeOfT.getClass();
        }

        TObject tObject = (TObject) gson.fromJson(json, actualClass);
        //need fix serialization bug
        if(tObject instanceof TInboundObject && ((TInboundObject) tObject).getInboundDate() <= 0) {
            ((TInboundObject) tObject).setInboundDate(System.currentTimeMillis());
        }
        return tObject;
    }
}
