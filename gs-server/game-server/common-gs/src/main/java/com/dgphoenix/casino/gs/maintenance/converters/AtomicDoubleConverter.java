package com.abs.casino.gs.maintenance.converters;

import com.google.common.util.concurrent.AtomicDouble;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * @author <a href="mailto:fateyev@dgphoenix.com">Anton Fateyev</a>
 * @since 01.07.16
 */
public class AtomicDoubleConverter implements Converter {
    @Override
    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        writer.setValue(source.toString());
    }

    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        return new AtomicDouble(Double.parseDouble(reader.getValue()));
    }

    @Override
    public boolean canConvert(Class aClass) {
        return AtomicDouble.class.equals(aClass);
    }
}
