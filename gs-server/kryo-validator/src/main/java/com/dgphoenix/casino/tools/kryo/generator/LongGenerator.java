package com.abs.casino.tools.kryo.generator;

import org.apache.commons.math3.random.RandomDataGenerator;

import java.lang.annotation.Annotation;
import java.util.Map;

/**
 * @author <a href="mailto:fateyev@dgphoenix.com">Anton Fateyev</a>
 * @since 15.10.15
 */
public class LongGenerator extends NumberGenerator {

    private final RandomDataGenerator randomDataGenerator;

    public LongGenerator(RandomDataGenerator randomDataGenerator) {
        this.randomDataGenerator = randomDataGenerator;
    }

    @Override
    public Boolean canGenerate(Class<?> type) {
        return Long.class.isAssignableFrom(type) || long.class.isAssignableFrom(type);
    }

    @Override
    public Object generate(Class<?> type, Map<? extends Class<? extends Annotation>, Annotation> annotations) {
        long minValue = calculateMinValue(annotations);
        long maxValue = calculateMaxValue(annotations);
        return randomDataGenerator.nextLong(minValue, maxValue);
    }

    @Override
    public GeneratorPriority getPriority() {
        return GeneratorPriority.HIGH;
    }
}
