package com.abs.casino.tools.kryo;

import com.abs.casino.tools.kryo.custom.CustomClass;
import com.abs.casino.tools.kryo.generator.GeneratorPriority;
import com.abs.casino.tools.kryo.generator.RandomValueGenerator;
import com.esotericsoftware.kryo.Kryo;
import de.javakaffee.kryoserializers.guava.ImmutableSetSerializer;
import junit.framework.AssertionFailedError;
import org.junit.Test;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.Map;
import java.util.function.Consumer;

import static org.junit.Assert.assertTrue;

/**
 * @author <a href="mailto:fateyev@dgphoenix.com">Anton Fateyev</a>
 * @since 13.10.2015
 */
public class KryoSerializationValidatorTest {

    @Test
    public void testSuccessValidation() throws Exception {
        KryoSerializationValidator validator = new KryoSerializationValidator();
        Consumer<Kryo> consumer = ImmutableSetSerializer::registerSerializers;
        validator.configure(consumer);
        assertTrue(validator.validate("com.abs.casino.tools.kryo.correct"));
    }

    @Test
    public void testSuccessValidationWithCustomGenerator() throws Exception {
        KryoSerializationValidator validator = new KryoSerializationValidator(Collections.singletonList(new CustomValueGenerator()));
        assertTrue(validator.validate("com.abs.casino.tools.kryo.custom"));
    }

    private static class CustomValueGenerator implements RandomValueGenerator {

        @Override
        public Boolean canGenerate(Class<?> type) {
            return type == CustomClass.class;
        }

        @Override
        public Object generate(Class<?> type, Map<? extends Class<? extends Annotation>, Annotation> annotations) {
            return new CustomClass("123");
        }

        @Override
        public GeneratorPriority getPriority() {
            return GeneratorPriority.MEDIUM;
        }
    }
}