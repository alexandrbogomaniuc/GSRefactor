package com.abs.casino.common.util;

import com.dgphoenix.casino.common.util.ReflectionUtils;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ReflectionUtilsCompatibilityTest {

    @Test
    public void shouldFallbackFromAbsToDgphoenixPrefix() throws Exception {
        Class<?> loaded = ReflectionUtils.forNameWithCompatibilityAliases(
                "com.abs.casino.common.util.compat.AbsToDgProbe");

        assertEquals("com.abs.casino.common.util.compat.AbsToDgProbe", loaded.getName());
    }

    @Test
    public void shouldFallbackFromDgphoenixToAbsPrefix() throws Exception {
        Class<?> loaded = ReflectionUtils.forNameWithCompatibilityAliases(
                "com.dgphoenix.casino.common.util.compat.DgToAbsProbe");

        assertEquals("com.abs.casino.common.util.compat.DgToAbsProbe", loaded.getName());
    }

    @Test(expected = ClassNotFoundException.class)
    public void shouldThrowWhenNoCompatibilityClassExists() throws Exception {
        ReflectionUtils.forNameWithCompatibilityAliases("com.abs.casino.nonexistent.DoesNotExist");
    }
}
