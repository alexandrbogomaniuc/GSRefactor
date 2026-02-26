package com.abs.casino.tools.kryo.generator;

import java.lang.annotation.Annotation;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Random;

/**
 * @author <a href="mailto:fateyev@dgphoenix.com">Anton Fateyev</a>
 * @since 16.10.15
 */
public class BigDecimalGenerator implements RandomValueGenerator {

    private final Random random;

    public BigDecimalGenerator(Random random) {
        this.random = random;
    }

    @Override
    public Boolean canGenerate(Class<?> type) {
        return BigDecimal.class.isAssignableFrom(type);
    }

    @Override
    public Object generate(Class<?> type, Map<? extends Class<? extends Annotation>, Annotation> annotations) {
        return new BigDecimal(random.nextDouble());
    }

    @Override
    public GeneratorPriority getPriority() {
        return GeneratorPriority.HIGH;
    }
}
