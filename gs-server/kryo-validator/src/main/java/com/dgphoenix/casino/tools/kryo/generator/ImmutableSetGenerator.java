package com.dgphoenix.casino.tools.kryo.generator;

import com.google.common.collect.ImmutableSet;

import java.lang.annotation.Annotation;
import java.util.Map;

public class ImmutableSetGenerator implements RandomValueGenerator {
    @Override
    public Boolean canGenerate(Class<?> type) {
        return ImmutableSet.class.isAssignableFrom(type);
    }

    @Override
    public Object generate(Class<?> type, Map<? extends Class<? extends Annotation>, Annotation> annotations) {
        return ImmutableSet.of("123", "test");
    }

    @Override
    public GeneratorPriority getPriority() {
        return GeneratorPriority.HIGH;
    }
}
