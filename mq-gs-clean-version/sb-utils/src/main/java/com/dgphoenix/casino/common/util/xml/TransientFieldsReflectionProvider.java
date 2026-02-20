package com.dgphoenix.casino.common.util.xml;

import com.thoughtworks.xstream.converters.reflection.ReflectionConverter;
import com.thoughtworks.xstream.converters.reflection.ReflectionProvider;
import com.thoughtworks.xstream.mapper.Mapper;

/**
 * User: flsh
 * Date: 04.04.19.
 */
public class TransientFieldsReflectionProvider extends ReflectionConverter {
    public TransientFieldsReflectionProvider(Mapper mapper,
                                             ReflectionProvider reflectionProvider) {
        super(mapper, reflectionProvider);
    }

    public TransientFieldsReflectionProvider(Mapper mapper,
                                             ReflectionProvider reflectionProvider,
                                             Class type) {
        super(mapper, reflectionProvider, type);
    }

    @Override
    protected boolean shouldUnmarshalTransientFields() {
        return true;
    }
}
