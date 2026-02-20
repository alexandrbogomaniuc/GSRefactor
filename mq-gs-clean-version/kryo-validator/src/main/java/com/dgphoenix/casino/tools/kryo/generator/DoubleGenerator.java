package com.dgphoenix.casino.tools.kryo.generator;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Random;

/**
 * @author <a href="mailto:fateyev@dgphoenix.com">Anton Fateyev</a>
 * @since 15.10.15
 */
public class DoubleGenerator implements RandomValueGenerator {

    private final Random random;

    public DoubleGenerator(Random random) {
        this.random = random;
    }

    @Override
    public Boolean canGenerate(Class<?> type) {
        return Double.class.isAssignableFrom(type) || double.class.isAssignableFrom(type);
    }

    @Override
    public Object generate(Class<?> type, Map<? extends Class<? extends Annotation>, Annotation> annotations) {
        return random.nextDouble();
    }

    @Override
    public GeneratorPriority getPriority() {
        return GeneratorPriority.HIGH;
    }
}
