package com.dgphoenix.casino.common.util.test.api;

import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.util.ObjectCreator;

import java.util.HashMap;

/**
 * @author <a href="mailto:fateyev@dgphoenix.com">Anton Fateyev</a>
 * @since 10.11.15
 */
public class ClientFactory {

    public ClientFactory() {
    }

    public <T> T build(String className, Class<T> clientInterface, long bankId) throws CommonException {
        Class<T> clazz = getClientClass(className, clientInterface);
        ObjectCreator<T> objectCreator = new ObjectCreator<T>();
        HashMap<Class, Object> parameters = new HashMap<Class, Object>();
        parameters.put(long.class, bankId);
        return objectCreator.createInstance(clazz, parameters);
    }

    @SuppressWarnings("unchecked")
    private <T> Class<T> getClientClass(String className, Class<T> clientInterface) throws CommonException {
        try {
            Class<?> clazz = Class.forName(className);
            if (clientInterface.isAssignableFrom(clazz)) {
                return (Class<T>) clazz;
            } else {
                throw new CommonException(className + " must implement " + clientInterface.getName());
            }
        } catch (ClassNotFoundException ex) {
            throw new CommonException("Unknown client class: " + className, ex);
        }
    }
}
