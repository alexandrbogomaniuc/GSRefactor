package com.abs.casino.tools.kryo;

import com.abs.casino.tools.annotations.IgnoreValidation;
import com.abs.casino.tools.annotations.Transient;
import com.abs.casino.tools.kryo.generator.DataGenerator;
import com.abs.casino.tools.kryo.generator.RandomValueGenerator;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

/**
 * Contains methods for create and initialize class instance
 *
 * @param <T>
 * @author <a href="mailto:fateyev@dgphoenix.com">Anton Fateyev</a>
 * @since 15.10.15
 */
public class InstanceCreator<T> {

    private final DataGenerator generator;
    private static final String ANNOTATION_NAME_TRANSIENT = "Transient";

    private final Predicate<Field> nonFinal = field -> !Modifier.isFinal(field.getModifiers());
    private final Predicate<Field> nonStatic = field -> !Modifier.isStatic(field.getModifiers());

    public InstanceCreator(List<RandomValueGenerator> customValueGenerators) {
        generator = new DataGenerator(customValueGenerators);
    }

    public InstanceCreator(DataGenerator generator) {
        this.generator = generator;
    }

    public boolean canCreateInstance(Class<? extends T> clazz) {
        int modifiers = clazz.getModifiers();
        return !(Modifier.isAbstract(modifiers) || Modifier.isInterface(modifiers) || isIgnoredClass(clazz));
    }

    public T instantiateClass(Class<? extends T> clazz)
            throws IllegalAccessException, InvocationTargetException, InstantiationException {
        T newInstance = createInstance(clazz);
        initializeInstance(newInstance, clazz);
        return newInstance;
    }

    public T createInstance(Class<? extends T> clazz)
            throws InvocationTargetException, IllegalAccessException, InstantiationException {
        Constructor<?>[] constructors = clazz.getDeclaredConstructors();
        T newInstance = createInstanceWithDefaultConstructor(constructors);
        if (newInstance == null) {
            newInstance = createInstanceWithCustomConstructor(constructors);
            if (newInstance == null) {
                throw new InstantiationException("Cannot create instance of class " + clazz.getSimpleName());
            }
        }
        return newInstance;
    }

    public Constructor<T> getDefaultConstructor(Constructor<?>[] constructors) {
        for (Constructor<?> constructor : constructors) {
            if (constructor.getParameterTypes().length == 0) {
                return (Constructor<T>) constructor;
            }
        }
        return null;
    }

    public static boolean hasDefaultConstructor(Class<?> clazz) {
        Constructor<?>[] constructors = clazz.getConstructors();
        for (Constructor<?> constructor : constructors) {
            if (constructor.getParameterTypes().length == 0) {
                return true;
            }
        }
        return false;
    }

    private void initializeInstance(T instance, Class<? extends T> clazz) throws IllegalAccessException {
        initializeFields(instance, clazz.getDeclaredFields());
        Class<?> superclass = clazz.getSuperclass();
        if (!superclass.equals(Object.class)) {
            initializeFields(instance, superclass.getDeclaredFields());
        }
    }

    private T createInstanceWithDefaultConstructor(Constructor<?>[] constructors) throws InvocationTargetException, IllegalAccessException {
        Constructor<T> defaultConstructor = getDefaultConstructor(constructors);
        if (defaultConstructor != null) {
            try {
                defaultConstructor.setAccessible(true);
                return defaultConstructor.newInstance();
            } catch (InstantiationException ex) {
                ex.printStackTrace();
                return null;
            }
        } else {
            return null;
        }
    }

    private T createInstanceWithCustomConstructor(Constructor<?>[] constructors) throws InvocationTargetException, IllegalAccessException {
        for (Constructor<?> constructor : constructors) {
            Class<?>[] parameterTypes = constructor.getParameterTypes();
            if (parameterTypes.length != 0) {
                List<Object> parameterValues = new ArrayList<>(parameterTypes.length);
                for (int i = 0; i < parameterTypes.length; i++) {
                    Annotation[] annotations = constructor.getParameterAnnotations()[i];
                    Object value = generator.generateValue(parameterTypes[i], annotations);
                    parameterValues.add(value);
                }
                try {
                    constructor.setAccessible(true);
                    return (T) constructor.newInstance(parameterValues.toArray());
                } catch (InstantiationException ex) {
                    ex.printStackTrace();
                }
            }
        }
        return null;
    }

    private void initializeFields(T instance, Field[] fields) throws IllegalAccessException {
        for (Field field : fields) {
            if (nonTransient().and(nonFinal).test(field)) {
                initializeField(instance, field);
            } else {
                clearField(instance, field);
            }
        }
    }

    private void initializeField(T instance, Field field) throws IllegalAccessException {
        field.setAccessible(true);
        Annotation[] annotations = field.getAnnotations();
        Object value = generator.generateValue(field.getType(), annotations);
        field.set(instance, value);
    }

    private void clearField(T instance, Field field) throws IllegalAccessException {
        if (nonStatic.test(field)) {
            field.setAccessible(true);
            Object value = getDefaultValue(field);
            field.set(instance, value);
        }
    }

    private Object getDefaultValue(Field field) {
        Class<?> type = field.getType();
        if (!type.isPrimitive()) {
            return null;
        }
        if (type == Byte.TYPE) {
            return (byte) 0;
        } else if (type == Short.TYPE) {
            return (short) 0;
        } else if (type == Boolean.TYPE) {
            return false;
        } else {
            return 0;
        }
    }

    private Predicate<Field> nonTransient() {
        return field -> !Modifier.isTransient(field.getModifiers())
                && !field.isAnnotationPresent(Transient.class)
                && hasNoTransientAnnotation(field);
    }

    private boolean hasNoTransientAnnotation(Field field) {
        return Arrays.stream(field.getAnnotations())
                .map(annotation -> annotation.annotationType().getSimpleName())
                .noneMatch(name -> name.equals(ANNOTATION_NAME_TRANSIENT));
    }

    private boolean isIgnoredClass(Class<? extends T> clazz) {
        return clazz.isAnnotationPresent(IgnoreValidation.class);
    }
}
