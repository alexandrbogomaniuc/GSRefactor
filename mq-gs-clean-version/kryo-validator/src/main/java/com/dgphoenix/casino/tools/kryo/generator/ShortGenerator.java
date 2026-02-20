package com.dgphoenix.casino.tools.kryo.generator;

import com.google.common.primitives.Shorts;
import org.apache.commons.math3.random.RandomDataGenerator;

import java.lang.annotation.Annotation;
import java.util.Map;

/**
 * @author <a href="mailto:fateyev@dgphoenix.com">Anton Fateyev</a>
 * @since 15.10.2015
 */
public class ShortGenerator extends NumberGenerator {

    private final RandomDataGenerator randomDataGenerator;

    public ShortGenerator(RandomDataGenerator randomDataGenerator) {
        this.randomDataGenerator = randomDataGenerator;
    }

    @Override
    public Boolean canGenerate(Class<?> type) {
        return Short.class.isAssignableFrom(type) || short.class.isAssignableFrom(type);
    }

    @Override
    public Object generate(Class<?> type, Map<? extends Class<? extends Annotation>, Annotation> annotations) {
        short minValue = Shorts.saturatedCast(calculateMinValue(annotations));
        short maxValue = Shorts.saturatedCast(calculateMaxValue(annotations));
        return (short) randomDataGenerator.nextInt(minValue, maxValue);
    }

    @Override
    public GeneratorPriority getPriority() {
        return GeneratorPriority.HIGH;
    }
}
