package com.dgphoenix.casino.common.util.web;

import com.dgphoenix.casino.services.externallobby.data.AccountInfoWrapper;
import com.google.common.collect.Lists;
import org.junit.Test;

import static com.dgphoenix.casino.common.util.web.WebTools.decodeObject;
import static com.dgphoenix.casino.common.util.web.WebTools.encodeObject;
import static junit.framework.Assert.assertEquals;

public class WebToolsTest {

    @Test
    public void testEncodeString() throws Exception {
        String initial = "a&s?d/a+sd \\\\ /d/s/a +d+sa+";
        String decoded = decodeObject(encodeObject(initial), String.class);
        assertEquals(initial, decoded);
    }

    @Test
    public void testEncodeObject() throws Exception {
        AccountInfoWrapper account = new AccountInfoWrapper(1, "S/D/A/", "-1", 20, 1, 100, -20, false, true,
                1000, -1,
                "ASA", "019s+123/12=", "asd", "ddssd", "0-asd12-=");
        AccountInfoWrapper decoded = decodeObject(encodeObject(account), AccountInfoWrapper.class);
        assertEquals(account, decoded);
    }

    @Test
    public void testEncodeKryo() throws Exception {
        TestEntity entity = new TestEntity("A/+/AS//+as", -100000L, false, Lists.newArrayList(1, 2, 3));
        String encoded = encodeObject(entity);
        TestEntity decoded = decodeObject(encoded, TestEntity.class);
        assertEquals(entity, decoded);
    }

    @Test
    public void testEncodeKryoNull() throws Exception {
        TestEntity entity = new TestEntity(null, null, null, Lists.newArrayList(1L, 1L, null, 2L, null));
        String encoded = encodeObject(entity);
        TestEntity decoded = decodeObject(encoded, TestEntity.class);
        assertEquals(entity, decoded);
    }
}