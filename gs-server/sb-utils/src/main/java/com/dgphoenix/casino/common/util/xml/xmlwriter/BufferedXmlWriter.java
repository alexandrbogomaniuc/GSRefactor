package com.abs.casino.common.util.xml.xmlwriter;

import com.abs.casino.common.util.xml.xmlwriter.XmlWriter;

import java.io.OutputStream;
import java.io.Writer;

/**
 * User: shegan
 * Date: 15.02.17
 */
public class BufferedXmlWriter extends XmlWriter {
    private StringBuilder buf = new StringBuilder(256);

    public BufferedXmlWriter() {
    }

    public BufferedXmlWriter(Writer w) {
        super(w);
    }

    public BufferedXmlWriter(Writer w, String encoding) {
        super(w, encoding);
    }

    public BufferedXmlWriter(OutputStream os) {
        super(os);
    }

    public BufferedXmlWriter(OutputStream os, String encoding) {
        super(os, encoding);
    }

    @Override
    public void print(String text) {
        super.print(text);
        buf.append(text);
    }

    @Override
    public void println(String text) {
        super.println(text);
        buf.append(String.format("%s%n", text));
    }

    @Override
    public String toString() {
        return buf.toString();
    }
}
