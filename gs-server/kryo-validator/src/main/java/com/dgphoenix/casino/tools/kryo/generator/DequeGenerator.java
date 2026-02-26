package com.abs.casino.tools.kryo.generator;

import java.lang.annotation.Annotation;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;

public class DequeGenerator implements RandomValueGenerator {

    @Override
    public Boolean canGenerate(Class<?> type) {
        return Deque.class.isAssignableFrom(type);
    }

    @Override
    public Object generate(Class<?> type, Map<? extends Class<? extends Annotation>, Annotation> annotations) {
        return new ArrayDeque();
    }

    @Override
    public GeneratorPriority getPriority() {
        return GeneratorPriority.HIGH;
    }
}
