package com.dgphoenix.casino.common.util;

import com.dgphoenix.casino.tools.kryo.KryoSerializationValidator;
import com.dgphoenix.casino.tools.kryo.generator.RandomValueGenerator;
import org.apache.commons.math3.random.RandomDataGenerator;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * @author <a href="mailto:fateyev@dgphoenix.com">Anton Fateyev</a>
 * @since 16.10.15
 */
public class KryoSerializationTest {

    @Test
    public void testSerialization() throws Exception {
        RandomDataGenerator rnd = new RandomDataGenerator();
        List<RandomValueGenerator> customGenerators = Arrays.asList(
                new CurrencyGenerator(rnd),
                new LimitGenerator(rnd)
        );
        KryoSerializationValidator validator = new KryoSerializationValidator(customGenerators);
        assertTrue(validator.validate("com.dgphoenix.casino.common"));
        assertTrue(validator.validate("com.dgphoenix.casino.gs"));
    }
}
