package com.dgphoenix.casino.tools.kryo.generator;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.util.Map;

/**
 * @author <a href="mailto:fateyev@dgphoenix.com">Anton Fateyev</a>
 * @since 15.10.15
 */
public class ArrayGenerator implements RandomValueGenerator {

    private static final int DEFAULT_ARRAY_LENGTH = 20;

    private final DataGenerator generator;
    private final int length;

    public ArrayGenerator(DataGenerator generator) {
        this(generator, DEFAULT_ARRAY_LENGTH);
    }

    public ArrayGenerator(DataGenerator generator, int length) {
        this.generator = generator;
        this.length = length;
    }

    @Override
    public Boolean canGenerate(Class<?> type) {
        return type.isArray();
    }

    @Override
    public Object generate(Class<?> type, Map<? extends Class<? extends Annotation>, Annotation> annotations) {
        Class<?> componentType = type.getComponentType();
        Annotation[] annotationsArray = annotations.values().toArray(new Annotation[annotations.size()]);
        Object array = Array.newInstance(componentType, length);
        for (int i = 0; i < length; i++) {
            Array.set(array, i, generator.generateValue(componentType, annotationsArray));
        }
        return array;
    }

    @Override
    public GeneratorPriority getPriority() {
        return GeneratorPriority.HIGH;
    }

}
