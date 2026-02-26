package com.abs.casino.tools.kryo.generator;

import com.abs.casino.tools.kryo.InstanceCreator;
import org.apache.commons.math3.random.RandomDataGenerator;
import org.reflections.Reflections;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Generates data for different types
 *
 * @author <a href="mailto:fateyev@dgphoenix.com">Anton Fateyev</a>
 * @since 15.10.2015
 */
public class DataGenerator {

    private final Set<RandomValueGenerator> generators;
    private final Random random;
    private final RandomDataGenerator randomDataGenerator;

    public DataGenerator(List<RandomValueGenerator> customValueGenerators) {
        random = new Random();
        randomDataGenerator = new RandomDataGenerator();
        generators = new TreeSet<>(new ValueGeneratorComparator());
        createGenerators();
        generators.addAll(customValueGenerators);
    }

    public Object generateValue(Class<?> type, Annotation[] annotations) {
        Map<? extends Class<? extends Annotation>, Annotation> annotationMap = Arrays.stream(annotations)
                .collect(Collectors.toMap(Annotation::annotationType, Function.identity()));
        for (RandomValueGenerator generator : generators) {
            if (generator.canGenerate(type)) {
                return generator.generate(type, annotationMap);
            }
        }
        throw new IllegalArgumentException("Cannot generate value. Unsupported data type: " + type.toString());
    }

    private void createGenerators() {
        Reflections reflections = new Reflections(this.getClass().getPackage().getName());
        InstanceCreator<RandomValueGenerator> creator = new InstanceCreator<>(this);
        Set<Class<? extends RandomValueGenerator>> valueGenerators = reflections.getSubTypesOf(RandomValueGenerator.class);
        for (Class<? extends RandomValueGenerator> valueGenerator : valueGenerators) {
            if (!Modifier.isAbstract(valueGenerator.getModifiers())) {
                RandomValueGenerator instance = createGeneratorInstance(valueGenerator, creator);
                generators.add(instance);
            }
        }
    }

    private RandomValueGenerator createGeneratorInstance(Class<? extends RandomValueGenerator> valueGenerator, InstanceCreator<RandomValueGenerator> creator) {
        Constructor<?>[] constructors = valueGenerator.getDeclaredConstructors();
        try {
            if (InstanceCreator.hasDefaultConstructor(valueGenerator)) {
                Constructor<RandomValueGenerator> defaultConstructor = creator.getDefaultConstructor(constructors);
                return defaultConstructor.newInstance();
            } else {
                return createGeneratorWithCustomConstructor(constructors);
            }
        } catch (InstantiationException | InvocationTargetException | IllegalAccessException ex) {
            throw new IllegalStateException("Unable to create generator: " + valueGenerator.getName(), ex);
        }
    }

    private RandomValueGenerator createGeneratorWithCustomConstructor(Constructor<?>[] constructors)
            throws InstantiationException, IllegalAccessException, InvocationTargetException {
        for (Constructor<?> constructor : constructors) {
            Class<?>[] parameterTypes = constructor.getParameterTypes();
            if (parameterTypes.length == 1) {
                Class<?> parameterType = parameterTypes[0];
                if (parameterType.equals(Random.class)) {
                    return (RandomValueGenerator) constructor.newInstance(random);
                } else if (parameterType.equals(RandomDataGenerator.class)) {
                    return (RandomValueGenerator) constructor.newInstance(randomDataGenerator);
                } else if (parameterType.equals(DataGenerator.class)) {
                    return (RandomValueGenerator) constructor.newInstance(this);
                }
            }
        }
        throw new InstantiationException("No suitable constructor for generator");
    }

}
