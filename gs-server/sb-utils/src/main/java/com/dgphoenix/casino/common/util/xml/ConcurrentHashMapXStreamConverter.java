package com.dgphoenix.casino.common.util.xml;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.collections.MapConverter;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.mapper.Mapper;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * User: flsh
 * Date: 3/15/11
 */
public class ConcurrentHashMapXStreamConverter extends MapConverter {

    public ConcurrentHashMapXStreamConverter(Mapper mapper) {
        super(mapper);
    }

    public boolean canConvert(Class type) {
        return /*super.canConvert(type) ||*/ type.equals(ConcurrentHashMap.class);
    }

    protected Object createCollection(Class type) {
        return new ConcurrentHashMap();
        //return new HashMap();  //temp hack
    }

    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        //LOG.debug("ConcurrentHashMapXStreamConverter: marshal: " + source);
        super.marshal(source, writer, context);
    }
}
