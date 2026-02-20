package com.dgphoenix.casino.tools.kryo.generator;

import com.dgphoenix.casino.tools.kryo.InstanceCreator;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

/**
 * @author <a href="mailto:fateyev@dgphoenix.com">Anton Fateyev</a>
 * @since 15.10.15
 */
public class ObjectGenerator implements RandomValueGenerator {

    private final InstanceCreator<Object> creator;

    public ObjectGenerator(DataGenerator generator) {
        this.creator = new InstanceCreator<>(generator);
    }

    @Override
    public Boolean canGenerate(Class<?> type) {
        return Object.class.isAssignableFrom(type);
    }

    @Override
    public Object generate(Class<?> type, Map<? extends Class<? extends Annotation>, Annotation> annotations) {
        try {
            return creator.createInstance(type);
        } catch (InstantiationException | InvocationTargetException | IllegalAccessException ex ) {
            throw new IllegalStateException("Cannot create object for type: " + type, ex);
        }
    }

    @Override
    public GeneratorPriority getPriority() {
        return GeneratorPriority.LOW;
    }
}
