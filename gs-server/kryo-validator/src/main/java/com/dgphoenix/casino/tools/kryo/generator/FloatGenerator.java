package com.dgphoenix.casino.tools.kryo.generator;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Random;

/**
 * @author <a href="mailto:fateyev@dgphoenix.com">Anton Fateyev</a>
 * @since 15.10.15
 */
public class FloatGenerator implements RandomValueGenerator {

    private final Random random;

    public FloatGenerator(Random random) {
        this.random = random;
    }

    @Override
    public Boolean canGenerate(Class<?> type) {
        return Float.class.isAssignableFrom(type) || float.class.isAssignableFrom(type);
    }

    @Override
    public Object generate(Class<?> type, Map<? extends Class<? extends Annotation>, Annotation> annotations) {
        return random.nextFloat();
    }

    @Override
    public GeneratorPriority getPriority() {
        return GeneratorPriority.HIGH;
    }
}
