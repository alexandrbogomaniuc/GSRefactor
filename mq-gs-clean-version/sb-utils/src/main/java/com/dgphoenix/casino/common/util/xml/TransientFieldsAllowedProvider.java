package com.dgphoenix.casino.common.util.xml;

import com.thoughtworks.xstream.converters.reflection.FieldDictionary;
import com.thoughtworks.xstream.converters.reflection.ImmutableFieldKeySorter;
import com.thoughtworks.xstream.converters.reflection.SunUnsafeReflectionProvider;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * User: flsh
 * Date: 04.04.19.
 */
public class TransientFieldsAllowedProvider extends SunUnsafeReflectionProvider {
    public TransientFieldsAllowedProvider() {
        this(new FieldDictionary(new ImmutableFieldKeySorter()));
    }

    public TransientFieldsAllowedProvider(FieldDictionary fieldDictionary) {
        super(fieldDictionary);
    }

    @Override
    protected boolean fieldModifiersSupported(Field field) {
        int modifiers = field.getModifiers();
        return !Modifier.isStatic(modifiers);
    }
}
