package com.dgphoenix.casino.common.util;

import org.junit.Test;

import static com.dgphoenix.casino.common.util.DigitFormatter.denominateMoney;
import static org.junit.Assert.*;

/**
 * @author <a href="mailto:fateyev@dgphoenix.com">Anton Fateyev</a>
 * @since 16.11.17
 */
public class DigitFormatterTest {

    @Test
    public void testDenominateMoney() {
        double money = denominateMoney(700, 1000);
        assertEquals("0.7", String.valueOf(money));

        money = denominateMoney(699, 1000);
        assertEquals("0.69", String.valueOf(money));

        money = denominateMoney(0.70, 1);
        assertEquals("0.7", String.valueOf(money));

        money = denominateMoney(0.69, 1);
        assertEquals("0.69", String.valueOf(money));

        money = denominateMoney(4800026.9, 1000);
        assertEquals("4800.02", String.valueOf(money));
    }
}