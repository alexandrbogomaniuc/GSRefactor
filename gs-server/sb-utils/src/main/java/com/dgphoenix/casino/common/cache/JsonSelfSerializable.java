package com.dgphoenix.casino.common.cache;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.WritableTypeId;
import com.fasterxml.jackson.databind.JsonSerializable;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;

public interface JsonSelfSerializable<T> extends JsonSerializable, JsonDeserializable<T>, JsonAdditionalSerializer {
    public default void serializeWithType(JsonGenerator gen,
                                          SerializerProvider serializers,
                                          TypeSerializer typeSer)
            throws IOException {
        WritableTypeId typeIdDef = typeSer.writeTypePrefix(gen, typeSer.typeId(this, this.getClass(), JsonToken.START_OBJECT));
        serializeObject(gen, serializers);
        typeSer.writeTypeSuffix(gen, typeIdDef);
    }

    public default void serialize(JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();
        serializeObject(gen, serializers);
        gen.writeEndObject();
    }

    public void serializeObject(JsonGenerator gen,
                                SerializerProvider serializers) throws IOException;
}
