package com.dgphoenix.casino.common.util;

import com.dgphoenix.casino.tools.kryo.KryoSerializationValidator;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * @author <a href="mailto:fateyev@dgphoenix.com">Anton Fateyev</a>
 * @since 29.10.15
 */
public class KryoSerializationTest {

    @Test
    public void testSerialization() throws Exception {
        KryoSerializationValidator validator = new KryoSerializationValidator();
//        KryoSerializationValidator validator = new KryoSerializationValidator("./src/test/resources/kryoTest");
//        KryoSerializationValidator validator = new KryoSerializationValidator("./src/test/resources/kryoTest", true);
        assertTrue(validator.validate("com.dgphoenix.casino.common.util"));
        assertTrue(validator.validate("com.dgphoenix.casino.common.cache"));
        assertTrue(validator.validate("com.dgphoenix.casino.common.engine"));
        assertTrue(validator.validate("com.dgphoenix.casino.common.promo"));
    }
}
