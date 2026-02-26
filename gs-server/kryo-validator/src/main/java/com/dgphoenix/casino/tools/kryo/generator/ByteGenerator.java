package com.abs.casino.tools.kryo.generator;

import org.apache.commons.math3.random.RandomDataGenerator;

import java.lang.annotation.Annotation;
import java.util.Map;

/**
 * @author <a href="mailto:fateyev@dgphoenix.com">Anton Fateyev</a>
 * @since 15.10.2015
 */
public class ByteGenerator implements RandomValueGenerator {

    private final RandomDataGenerator randomDataGenerator;
    private final boolean onlyPositive;

    public ByteGenerator(RandomDataGenerator randomDataGenerator) {
        this(randomDataGenerator, true);
    }

    public ByteGenerator(RandomDataGenerator randomDataGenerator, boolean onlyPositive) {
        this.randomDataGenerator = randomDataGenerator;
        this.onlyPositive = onlyPositive;
    }

    @Override
    public Boolean canGenerate(Class<?> type) {
        return Byte.class.isAssignableFrom(type) || byte.class.isAssignableFrom(type);
    }

    @Override
    public Object generate(Class<?> type, Map<? extends Class<? extends Annotation>, Annotation> annotations) {
        return (byte) randomDataGenerator.nextInt(onlyPositive ? 0 : Byte.MIN_VALUE, Byte.MAX_VALUE);
    }

    @Override
    public GeneratorPriority getPriority() {
        return GeneratorPriority.HIGH;
    }

}
