package com.abs.casino.tools.kryo.generator;

import java.lang.annotation.Annotation;
import java.util.Map;

/**
 * @author <a href="mailto:fateyev@dgphoenix.com">Anton Fateyev</a>
 * @since 15.10.2015
 */
public interface RandomValueGenerator {

    Boolean canGenerate(Class<?> type);

    Object generate(Class<?> type, Map<? extends Class<? extends Annotation>, Annotation> annotations);

    GeneratorPriority getPriority();
}
