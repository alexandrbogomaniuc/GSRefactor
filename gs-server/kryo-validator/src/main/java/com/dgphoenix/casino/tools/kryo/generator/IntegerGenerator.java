package com.abs.casino.tools.kryo.generator;

import com.google.common.primitives.Ints;
import org.apache.commons.math3.random.RandomDataGenerator;

import java.lang.annotation.Annotation;
import java.util.Map;

/**
 * @author <a href="mailto:fateyev@dgphoenix.com">Anton Fateyev</a>
 * @since 15.10.15
 */
public class IntegerGenerator extends NumberGenerator {

    private final RandomDataGenerator randomDataGenerator;

    public IntegerGenerator(RandomDataGenerator randomDataGenerator) {
        this.randomDataGenerator = randomDataGenerator;
    }

    @Override
    public Boolean canGenerate(Class<?> type) {
        return Integer.class.isAssignableFrom(type) || int.class.isAssignableFrom(type);
    }

    @Override
    public Object generate(Class<?> type, Map<? extends Class<? extends Annotation>, Annotation> annotations) {
        int minValue = Ints.saturatedCast(calculateMinValue(annotations));
        int maxValue = Ints.saturatedCast(calculateMaxValue(annotations));
        return randomDataGenerator.nextInt(minValue, maxValue);
    }

    @Override
    public GeneratorPriority getPriority() {
        return GeneratorPriority.HIGH;
    }
}
