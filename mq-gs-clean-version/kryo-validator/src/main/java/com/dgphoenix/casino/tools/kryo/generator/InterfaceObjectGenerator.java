package com.dgphoenix.casino.tools.kryo.generator;

import org.mockito.Mockito;

import java.lang.annotation.Annotation;
import java.util.Map;

/**
 * @author <a href="mailto:fateyev@dgphoenix.com">Anton Fateyev</a>
 * @since 15.10.15
 */
public class InterfaceObjectGenerator implements RandomValueGenerator {

    @Override
    public Boolean canGenerate(Class<?> type) {
        return type.isInterface();
    }

    @Override
    public Object generate(Class<?> type, Map<? extends Class<? extends Annotation>, Annotation> annotations) {
        return Mockito.mock(type);
    }

    @Override
    public GeneratorPriority getPriority() {
        return GeneratorPriority.MEDIUM;
    }
}
