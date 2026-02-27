package com.abs.casino.common.util.xml;

import com.thoughtworks.xstream.io.xml.PrettyPrintWriter;
import com.thoughtworks.xstream.io.xml.XmlFriendlyReplacer;

import java.io.Writer;

/**
 * User: oleg
 * Date: 26.04.12
 * Time: 17:53
 */
public class XMLUppercaseNodePrintWriter extends PrettyPrintWriter {


    public XMLUppercaseNodePrintWriter(Writer writer, int mode, char[] lineIndenter, XmlFriendlyReplacer replacer) {
        super(writer, mode, lineIndenter, replacer);
    }

    public XMLUppercaseNodePrintWriter(Writer writer, int mode, char[] lineIndenter) {
        super(writer, mode, lineIndenter);
    }

    public XMLUppercaseNodePrintWriter(Writer writer, char[] lineIndenter) {
        super(writer, lineIndenter);
    }

    public XMLUppercaseNodePrintWriter(Writer writer, int mode, String lineIndenter) {
        super(writer, mode, lineIndenter);
    }

    public XMLUppercaseNodePrintWriter(Writer writer, String lineIndenter) {
        super(writer, lineIndenter);
    }

    public XMLUppercaseNodePrintWriter(Writer writer, int mode, XmlFriendlyReplacer replacer) {
        super(writer, mode, replacer);
    }

    public XMLUppercaseNodePrintWriter(Writer writer, XmlFriendlyReplacer replacer) {
        super(writer, replacer);
    }

    public XMLUppercaseNodePrintWriter(Writer writer, int mode) {
        super(writer, mode);
    }

    public XMLUppercaseNodePrintWriter(Writer writer) {
        super(writer);
    }

    public void startNode(String name) {
        super.startNode(name.toUpperCase());
    }
    
}
