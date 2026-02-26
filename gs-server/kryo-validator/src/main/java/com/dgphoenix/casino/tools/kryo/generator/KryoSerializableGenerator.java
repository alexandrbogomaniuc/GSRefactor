package com.abs.casino.tools.kryo.generator;

import com.abs.casino.tools.kryo.InstanceCreator;
import com.esotericsoftware.kryo.KryoSerializable;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

/**
 * @author <a href="mailto:fateyev@dgphoenix.com">Anton Fateyev</a>
 * @since 15.10.15
 */
public class KryoSerializableGenerator implements RandomValueGenerator {

    private final InstanceCreator<KryoSerializable> creator;

    public KryoSerializableGenerator(DataGenerator generator) {
        creator = new InstanceCreator<>(generator);
    }

    @Override
    public Boolean canGenerate(Class<?> type) {
        return KryoSerializable.class.isAssignableFrom(type);
    }

    @Override
    public Object generate(Class<?> type, Map<? extends Class<? extends Annotation>, Annotation> annotations) {
        Class<? extends KryoSerializable> clazz = (Class<? extends KryoSerializable>) type;
        if (creator.canCreateInstance(clazz)) {
            try {
                return creator.instantiateClass(clazz);
            } catch (IllegalAccessException | InvocationTargetException | InstantiationException ex) {
                throw new  IllegalArgumentException("Cannot create KryoSerializable class: " + clazz, ex);
            }
        } else {
            return null;
        }
    }

    @Override
    public GeneratorPriority getPriority() {
        return GeneratorPriority.HIGH;
    }
}
