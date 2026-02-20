package com.dgphoenix.casino.common.util;


import com.dgphoenix.casino.common.util.test.Repeat;
import com.dgphoenix.casino.common.util.test.RepeatRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class RNGTest {

    @Rule
    public RepeatRule repeatRule = new RepeatRule();

    @Test
    @Repeat(times = 100)
    public void randExponential() {
        assertTrue(RNG.randExponential(1, 2) >= 1);
        assertTrue(RNG.randExponential(1, 2) <= 2);
    }

    @Test
    @Repeat(times = 100)
    public void nextIntUniform() {
        assertTrue(RNG.nextIntUniform(1, 2) >= 1);
        assertTrue(RNG.nextIntUniform(1, 2) <= 2);
    }

    @Test
    @Repeat(times = 100)
    public void randUniform() {
        assertTrue(RNG.randUniform() <= 1);
        assertTrue(RNG.randUniform() >= 0);
    }
}
