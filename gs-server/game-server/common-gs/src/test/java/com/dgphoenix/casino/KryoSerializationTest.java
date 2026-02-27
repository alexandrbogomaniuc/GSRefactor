package com.dgphoenix.casino;

import com.abs.casino.tools.kryo.KryoSerializationValidator;
import com.abs.casino.tools.kryo.generator.RandomValueGenerator;
import org.apache.commons.math3.random.RandomDataGenerator;
import org.junit.Test;

import java.util.Collections;
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
        List<RandomValueGenerator> customGenerators = Collections.singletonList(
                new CurrencyGenerator(rnd)
        );
        KryoSerializationValidator validator = new KryoSerializationValidator(customGenerators);
        assertTrue(validator.validate("com.dgphoenix.casino.gs"));
        assertTrue(validator.validate("com.abs.casino.configuration"));
        assertTrue(validator.validate("com.dgphoenix.casino.common.promo"));
        assertTrue(validator.validate("com.dgphoenix.casino.promo"));
    }
}
