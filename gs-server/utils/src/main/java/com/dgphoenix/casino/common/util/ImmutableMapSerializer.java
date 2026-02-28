package com.abs.casino.common.util;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.serializers.MapSerializer;
import com.google.common.collect.ImmutableMap;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by vladislav on 2/20/17.
 */
public class ImmutableMapSerializer extends MapSerializer {
    @Override
    protected Map create(Kryo kryo, Input input, Class<Map> type) {
        if (ImmutableMap.class.isAssignableFrom(type)) { // if true, create HashMap instead of ImmutableMap
            Class hashMapClass = HashMap.class; // intentionally cast to raw type to disable type checking
            return super.create(kryo, input, hashMapClass);
        } else {
            return super.create(kryo, input, type);
        }
    }
}
