package com.dgphoenix.casino.tools.kryo.generator;

import com.dgphoenix.casino.tools.annotations.Preset;
import org.apache.commons.math3.random.RandomDataGenerator;

import java.lang.annotation.Annotation;
import java.util.Map;

/**
 * @author <a href="mailto:fateyev@dgphoenix.com">Anton Fateyev</a>
 * @since 15.10.15
 */
public class StringGenerator implements RandomValueGenerator {

    private static final int DEFAULT_STRING_LENGTH = 20;

    private final RandomDataGenerator randomDataGenerator;
    private final int length;

    public StringGenerator(RandomDataGenerator randomDataGenerator) {
        this(randomDataGenerator, DEFAULT_STRING_LENGTH);
    }

    public StringGenerator(RandomDataGenerator randomDataGenerator, int length) {
        this.randomDataGenerator = randomDataGenerator;
        this.length = length;
    }

    @Override
    public Boolean canGenerate(Class<?> type) {
        return String.class.isAssignableFrom(type);
    }

    @Override
    public Object generate(Class<?> type, Map<? extends Class<? extends Annotation>, Annotation> annotations) {
        Annotation preset = annotations.get(Preset.class);
        if (preset != null) {
            return ((Preset) preset).value();
        }
        return randomDataGenerator.nextHexString(length);
    }

    @Override
    public GeneratorPriority getPriority() {
        return GeneratorPriority.HIGH;
    }
}
