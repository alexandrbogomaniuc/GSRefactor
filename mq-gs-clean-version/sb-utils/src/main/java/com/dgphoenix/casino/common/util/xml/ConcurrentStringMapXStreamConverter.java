package com.dgphoenix.casino.common.util.xml;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.collections.MapConverter;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.mapper.Mapper;

import java.util.concurrent.ConcurrentHashMap;

/**
 * User: flsh
 * Date: 3/15/11
 */
public class ConcurrentStringMapXStreamConverter extends MapConverter {

    public ConcurrentStringMapXStreamConverter(Mapper mapper) {
        super(mapper);
    }

    public boolean canConvert(Class type) {
        return super.canConvert(type) || type.getCanonicalName().equals("org.terracotta.collections.ClusteredMap");
    }

    protected Object createCollection(Class type) {
        return new ConcurrentHashMap();
    }

    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        //LOG.debug("ConcurrentStringMapXStreamConverter: marshal: " + source);
        super.marshal(source, writer, context);
    }

}
