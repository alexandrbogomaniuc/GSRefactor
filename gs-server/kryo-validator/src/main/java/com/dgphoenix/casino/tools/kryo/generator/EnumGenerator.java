package com.abs.casino.tools.kryo.generator;

import com.abs.casino.tools.annotations.Preset;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Map;
import java.util.Random;

/**
 * @author <a href="mailto:fateyev@dgphoenix.com">Anton Fateyev</a>
 * @since 15.10.15
 */
public class EnumGenerator implements RandomValueGenerator {

    private final Random random;

    public EnumGenerator(Random random) {
        this.random = random;
    }

    @Override
    public Boolean canGenerate(Class<?> type) {
        return Enum.class.isAssignableFrom(type);
    }

    @Override
    public Object generate(Class<?> type, Map<? extends Class<? extends Annotation>, Annotation> annotations) {
        Object[] enumValues = type.getEnumConstants();
        Annotation preset = annotations.get(Preset.class);
        if (preset != null) {
            String presetValue = ((Preset) preset).value();
            return Arrays.stream(enumValues)
                    .filter(v -> v.toString().equalsIgnoreCase(presetValue))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Unknown value: " + presetValue + " of enum: " + type.getCanonicalName()));
        }
        return enumValues[random.nextInt(enumValues.length)];
    }

    @Override
    public GeneratorPriority getPriority() {
        return GeneratorPriority.HIGH;
    }
}
