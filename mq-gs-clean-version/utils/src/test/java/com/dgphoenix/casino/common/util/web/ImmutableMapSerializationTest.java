package com.dgphoenix.casino.common.util.web;

import com.dgphoenix.casino.common.util.FastKryoHelper;
import com.google.common.collect.ImmutableMap;
import org.junit.Assert;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by vladislav on 2/20/17.
 */
public class ImmutableMapSerializationTest {
    @Test
    public void testSerializationAndDeserialization() {
        Map<Integer, Integer> map = new HashMap<>();
        map.put(1, 4);
        map.put(3, 6);
        map.put(2, 5);
        ImmutableMap<Integer, Integer> immutableMap = ImmutableMap.copyOf(map);

        ByteBuffer immutableMapAsBytes = FastKryoHelper.serializeWithClassToBytes(immutableMap);
        Map<Integer, Integer> deserializedImmutableMap = FastKryoHelper.deserializeWithClassFrom(immutableMapAsBytes);

        Assert.assertEquals(immutableMap, deserializedImmutableMap);
    }
}
