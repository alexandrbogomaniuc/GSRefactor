package com.dgphoenix.casino.common.util;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class CollectionUtilsTest {
    private Map<Integer, Integer> data;

    @Before
    public void setUp() throws Exception {
        data = new HashMap<Integer, Integer>();
        data.put(1, 10);
        data.put(2, 20);
        data.put(3, 30);
        data.put(4, 40);
        data.put(5, 40);
        data.put(6, 30);
        data.put(7, 20);
        data.put(0, 0);
        data.put(-1, 10);
        data.put(-2, 5);
        data.put(-3, -10);
    }

    @Test
    public void testSortReversed() throws Exception {
        Map<Integer, Integer> sorted = CollectionUtils.sortByValue(data, true);
        int firstValue = sorted.values().iterator().next();
        Assert.assertEquals(40, firstValue);
    }

    @Test
    public void testSort() throws Exception {
        Map<Integer, Integer> sorted = CollectionUtils.sortByValue(data);
        int firstValue = sorted.values().iterator().next();
        Assert.assertEquals(-10, firstValue);
    }
}