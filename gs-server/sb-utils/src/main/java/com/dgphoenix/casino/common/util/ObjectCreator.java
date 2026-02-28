package com.abs.casino.common.util;

import com.dgphoenix.casino.common.exception.CommonException;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:fateyev@dgphoenix.com">Anton Fateyev</a>
 * @since 08.12.15
 */
public class ObjectCreator<T> {

    public T createInstance(Class<? extends T> clazz) throws CommonException {
        return createInstance(clazz, Collections.<Class, Object>emptyMap());
    }

    public T createInstance(Class<? extends T> clazz, Map<Class, Object> constructorParameters) throws CommonException {
        Set<Class> parameters = constructorParameters.keySet();
        Class[] parameterTypes = parameters.toArray(new Class[parameters.size()]);
        try {
            Constructor<? extends T> constructor = clazz.getConstructor(parameterTypes);
            constructor.setAccessible(true);
            return constructor.newInstance(constructorParameters.values().toArray());
        } catch (NoSuchMethodException ex) {
            throw new CommonException(
                    String.format("Unable find constructor with %d parameters for class: %s", parameters.size(), clazz.getName()), ex);
        } catch (InvocationTargetException ex) {
            throw new CommonException("Constructor throws exception", ex);
        } catch (InstantiationException ex) {
            throw new CommonException("Unable create class: " + clazz.getName(), ex);
        } catch (IllegalAccessException ex) {
            throw new CommonException("Unable access to constructor of class: " + clazz.getName(), ex);
        }
    }

}
