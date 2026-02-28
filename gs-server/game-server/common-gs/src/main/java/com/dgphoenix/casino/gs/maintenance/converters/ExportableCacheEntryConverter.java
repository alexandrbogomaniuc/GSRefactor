package com.abs.casino.gs.maintenance.converters;

import com.abs.casino.common.cache.ExportableCacheEntry;
import com.dgphoenix.casino.common.cache.IDistributedCacheEntry;
import com.abs.casino.common.util.ReflectionUtils;
import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * User: flsh
 * Date: 25.08.14.
 */
public class ExportableCacheEntryConverter implements Converter {
    private static final Logger LOG = LogManager.getLogger(ExportableCacheEntryConverter.class);

    @Override
    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        ExportableCacheEntry entry = (ExportableCacheEntry) source;
        writer.startNode("key");
        writer.setValue(entry.getKey());
        writer.endNode();

        if (entry.getValue() != null) {
            writer.startNode("value");
            writer.addAttribute("class", entry.getValue().getClass().getCanonicalName());
            context.convertAnother(entry.getValue());
            writer.endNode();
        }
    }

    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        ExportableCacheEntry entry = new ExportableCacheEntry();
        while (reader.hasMoreChildren()) {
            reader.moveDown();
            String nodeName = reader.getNodeName();
            if ("key".equals(nodeName)) {
                String value = reader.getValue();
                entry.setKey(value);
            } else if ("value".equals(nodeName)) {
                String klazzName = reader.getAttribute("class");
                IDistributedCacheEntry entryValue;
                try {
                    entryValue = (IDistributedCacheEntry) context.convertAnother(entry,
                            ReflectionUtils.forNameWithCompatibilityAliases(klazzName));
                } catch (Exception e) {
                    LOG.error("Cannot convert entry, class=" + klazzName, e);
                    throw new ConversionException("Cannot convert entry, class=" + klazzName, e);
                }
                entry.setValue(entryValue);
            }
            reader.moveUp();
        }
        return entry;
    }

    @Override
    public boolean canConvert(Class type) {
        return type.equals(ExportableCacheEntry.class);
    }
}
