package com.betsoft.casino.mp.model.movement;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class MathUtilsTest {

    @Test
    public void shouldOverlaps() {
        assertTrue(MathUtils.isOverlaps(20, 50, 30, 60));
        assertTrue(MathUtils.isOverlaps(20, 50, 10, 40));
        assertTrue(MathUtils.isOverlaps(30, 60, 20, 50));
        assertTrue(MathUtils.isOverlaps(10, 40, 20, 50));
        assertTrue(MathUtils.isOverlaps(30, 40, 20, 50));
        assertTrue(MathUtils.isOverlaps(20, 50, 30, 40));
        assertTrue(MathUtils.isOverlaps(20, 50, 20, 50));
        assertTrue(MathUtils.isOverlaps(20, 50, 30, 50));
        assertTrue(MathUtils.isOverlaps(30, 50, 20, 50));
        assertTrue(MathUtils.isOverlaps(20, 40, 20, 50));
        assertTrue(MathUtils.isOverlaps(20, 50, 20, 40));
        assertTrue(MathUtils.isOverlaps(20, 30, 30, 50));
        assertTrue(MathUtils.isOverlaps(30, 50, 20, 30));
    }

    @Test
    public void shouldNotOverlaps() {
        assertFalse(MathUtils.isOverlaps(20, 30, 40, 50));
        assertFalse(MathUtils.isOverlaps(40, 50, 20, 30));
    }

    @Test
    public void shouldOverlapsDouble() {
        assertTrue(MathUtils.isOverlaps(20.0, 50.0, 30.0, 60.0));
        assertTrue(MathUtils.isOverlaps(20.0, 50.0, 10.0, 40.0));
        assertTrue(MathUtils.isOverlaps(30.0, 60.0, 20.0, 50.0));
        assertTrue(MathUtils.isOverlaps(10.0, 40.0, 20.0, 50.0));
        assertTrue(MathUtils.isOverlaps(30.0, 40.0, 20.0, 50.0));
        assertTrue(MathUtils.isOverlaps(20.0, 50.0, 30.0, 40.0));
        assertTrue(MathUtils.isOverlaps(20.0, 50.0, 20.0, 50.0));
        assertTrue(MathUtils.isOverlaps(20.0, 50.0, 30.0, 50.0));
        assertTrue(MathUtils.isOverlaps(30.0, 50.0, 20.0, 50.0));
        assertTrue(MathUtils.isOverlaps(20.0, 40.0, 20.0, 50.0));
        assertTrue(MathUtils.isOverlaps(20.0, 50.0, 20.0, 40.0));
        assertTrue(MathUtils.isOverlaps(20.0, 30.0, 30.0, 50.0));
        assertTrue(MathUtils.isOverlaps(30.0, 50.0, 20.0, 30.0));
    }

    @Test
    public void shouldNotOverlapsDouble() {
        assertFalse(MathUtils.isOverlaps(20.0, 30.0, 40.0, 50.0));
        assertFalse(MathUtils.isOverlaps(40.0, 50.0, 20.0, 30.0));
    }

    @Test
    public void shouldCalculateSumProduct() {
        Map<Integer, Double> map = new HashMap<>();
        map.put(2, 0.025);
        map.put(3, 0.01);
        map.put(5, 0.0075);
        map.put(7, 0.005);
        map.put(10, 0.0025);
        assertEquals(0.1775, MathUtils.sumProduct(map), 0);
    }

    @Test
    public void atan2() {
        assertEquals(0, MathUtils.atan2(1, 0));
        assertEquals(45, MathUtils.atan2(1, 1));
        assertEquals(135, MathUtils.atan2(-1, 1));
        assertEquals(225, MathUtils.atan2(-1, -1));
        assertEquals(315, MathUtils.atan2(1, -1));
    }
}
