package com.abs.casino.tools.kryo.generator;

import org.mockito.Mockito;

import java.lang.annotation.Annotation;
import java.util.EnumMap;
import java.util.Map;

/**
 * @author <a href="mailto:fateyev@dgphoenix.com">Anton Fateyev</a>
 * @since 01.10.18
 */
public class EnumMapGenerator implements RandomValueGenerator {
    @Override
    public Boolean canGenerate(Class<?> type) {
        return EnumMap.class.isAssignableFrom(type);
    }

    @Override
    public Object generate(Class<?> type, Map<? extends Class<? extends Annotation>, Annotation> annotations) {
        return Mockito.mock(type);
    }

    @Override
    public GeneratorPriority getPriority() {
        return GeneratorPriority.HIGH;
    }
}
