package com.abs.casino.actions.api.response;

import com.thoughtworks.xstream.io.naming.NameCoder;
import com.thoughtworks.xstream.io.xml.XmlFriendlyNameCoder;

public class UpperCaseNameCoder implements NameCoder {

    private XmlFriendlyNameCoder defaultCoder = new XmlFriendlyNameCoder();

    @Override
    public String encodeNode(String name) {
        String encodedNode = defaultCoder.encodeNode(name);
        return encodedNode.toUpperCase();
    }

    @Override
    public String encodeAttribute(String name) {
        return defaultCoder.encodeAttribute(name);
    }

    @Override
    public String decodeNode(String nodeName) {
        return defaultCoder.decodeNode(nodeName);
    }

    @Override
    public String decodeAttribute(String attributeName) {
        return defaultCoder.decodeNode(attributeName);
    }
}
