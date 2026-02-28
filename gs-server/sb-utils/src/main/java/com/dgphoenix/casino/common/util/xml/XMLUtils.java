package com.abs.casino.common.util.xml;

import com.abs.casino.common.util.string.StringBuilderWriter;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.HierarchicalStreamDriver;
import com.thoughtworks.xstream.io.xml.CompactWriter;

/**
 * User: plastical
 * Date: 27.04.2010
 */
public final class XMLUtils {
    private static final String XML_HEADER = "<?xml version=\"1.0\" ?>";

    public static <T> T fromXML(String objectXML) {
        XStream parser = new XStream();
        XStream.setupDefaultSecurity(parser);
        parser.allowTypesByWildcard(new String[]{"com.dgphoenix.casino.**"});
        //noinspection unchecked
        return (T) parser.fromXML(objectXML);
    }

    public static <T> T fromXML(String objectXML, Class... aliases) {
        XStream parser = new XStream();
        XStream.setupDefaultSecurity(parser);
        parser.allowTypesByWildcard(new String[]{"com.dgphoenix.casino.**"});
        parser.autodetectAnnotations(true);
        for (Class alias : aliases) {
            parser.alias(alias.getSimpleName(), alias);
        }
        //noinspection unchecked
        return (T) parser.fromXML(objectXML);
    }

    public static String toCompactXML(Object object) {
        XStream parser = new XStream();
        StringBuilderWriter sw = new StringBuilderWriter();
        try {
            parser.marshal(object, new CompactWriter(sw));
        } finally {
            sw.flush();
        }
        return sw.toString();
    }

    public static String toUpperCaseXML(XStream parser, Object object) {
        StringBuilderWriter sw = new StringBuilderWriter();
        try {
            parser.marshal(object, new XMLUppercaseNodePrintWriter(sw));
        } finally {
            sw.flush();
        }
        return sw.toString();
    }

    public static String toCompactXML(XStream parser, Object object) {
        StringBuilderWriter sw = new StringBuilderWriter();
        try {
            parser.marshal(object, new CompactWriter(sw));
        } finally {
            sw.flush();
        }
        return sw.toString();
    }

    public static String toXML(Object object) {
        return toXML(object, false);
    }

    public static String toXMLNoRegister(Object object) {
        XStream parser = new XStream();
        return parser.toXML(object);
    }

    public static String toXML(Object object, boolean removeHeader) {
        return toXML(object, removeHeader, null);
    }

    public static String toXML(Object object, boolean removeHeader, HierarchicalStreamDriver hierarchicalStreamDriver) {
        XStream parser = hierarchicalStreamDriver == null ? new XStream() : new XStream(hierarchicalStreamDriver);
        Class<? extends Object> klass = object.getClass();
        parser.alias(klass.getSimpleName(), klass);
        parser.autodetectAnnotations(true);

        String xml = parser.toXML(object);
        if (removeHeader) {
            xml = xml.replace(XML_HEADER, "");
        }

        return xml;
    }
}
