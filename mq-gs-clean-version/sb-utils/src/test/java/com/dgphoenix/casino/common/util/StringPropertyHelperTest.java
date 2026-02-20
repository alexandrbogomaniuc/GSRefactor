package com.dgphoenix.casino.common.util;

import com.google.common.collect.Maps;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;

public class StringPropertyHelperTest {
    private String property = "a=1;b=2;c=3";
    private String correct = ";a=1;b=2;c=3;";
    private String incorrect = ";a==1;b=;c=2=3;";

    private String ed = ";";
    private String kvd = "=";

    @Test
    public void testAddAll() throws Exception {
        Map<String, String> map = Maps.newHashMap();
        map.put("d", "4");
        map.put("e", "5");
        map.put("a", "10");
        String result = CollectionUtils.modifyStringProperty(property, ed, kvd)
                .addAll(map)
                .getString();
        assertEquals("a=10;b=2;c=3;d=4;e=5", result);
    }

    @Test
    public void testModify() throws Exception {
        String result = CollectionUtils.modifyStringProperty(correct, ed, kvd)
                .add("d", "5")
                .remove("a")
                .getString();
        assertEquals("b=2;c=3;d=5", result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIncorrect() throws Exception {
        CollectionUtils.modifyStringProperty(incorrect, ed, kvd).getString();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIncorrect2() throws Exception {
        CollectionUtils.modifyStringProperty("a=b=c", ed, kvd).getString();
    }

    @Test
    public void testAdd() throws Exception {
        String result = CollectionUtils.modifyStringProperty(property, ed, kvd)
                .add("a", "10")
                .add("c", "20")
                .getString();

        assertEquals("a=10;b=2;c=20", result);
    }

    @Test
    public void testRemove() throws Exception {
        String result = CollectionUtils.modifyStringProperty(property, ed, kvd)
                .remove("a")
                .getString();
        assertEquals("b=2;c=3", result);
    }

    @Test
    public void testGetMap() throws Exception {
        CollectionUtils.StringPropertyHelper helper = CollectionUtils.modifyStringProperty(property, ed, kvd);
        Map<String, String> map = helper.getMap();
        map.put("r", "100");
        map.remove("a");
        String result = helper.getString();
        assertEquals("b=2;c=3;r=100", result);
    }

    @Test
    public void testTrim() throws Exception {
        String result = CollectionUtils.modifyStringProperty(" a = 1 ; b = 2 ; c = 3 ", ";", "=")
                .add("a", "10")
                .remove("b")
                .getString();
        assertEquals("a=10;c=3", result);

    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyTrim() throws Exception {
        CollectionUtils.modifyStringProperty("  =  ; a = 1", ";", "=")
                .add("a", "10")
                .remove("b")
                .getString();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyTrimOneElement() throws Exception {
        CollectionUtils.modifyStringProperty("  =  ", ";", "=")
                .add("a", "10")
                .remove("b")
                .getString();
    }

    @Test
    public void testConvertMapToString() throws Exception {
        Map<String, String> map = Maps.newLinkedHashMap();
        map.put("d", "4");
        map.put("e", "5");
        map.put("a", "10");

        String result = CollectionUtils.convertMapToString(map, ed, kvd)
                .add("b", "2")
                .remove("e")
                .getString();

        assertEquals("d=4;a=10;b=2", result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConvertIncorrectMap() throws Exception {
        Map<String, String> map = Maps.newLinkedHashMap();
        map.put("d", null);
        CollectionUtils.convertMapToString(map, ed, kvd);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConvertIncorrectMap1() throws Exception {
        Map<String, String> map = Maps.newLinkedHashMap();
        map.put("d", "");
        CollectionUtils.convertMapToString(map, ed, kvd);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConvertIncorrectMap2() throws Exception {
        Map<String, String> map = Maps.newLinkedHashMap();
        map.put("", "a");
        CollectionUtils.convertMapToString(map, ed, kvd);
    }
}