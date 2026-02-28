/*
 * User: val
 * Date: 13.10.2002
 * Time: 14:21:07
 */
package com.dgphoenix.casino.common.util.xml.xmlwriter;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;

import com.abs.casino.common.exception.XmlWriterException;

public class FormattedXmlWriter extends XmlWriter {
    private static final String SPACER = "    ";
    private int level = 0;

    private PrintWriter pw;

    public FormattedXmlWriter() {}

    public FormattedXmlWriter(Writer w) {
        this.pw = new PrintWriter(w,true);
    }

    public FormattedXmlWriter(OutputStream os) {
        this.pw = new PrintWriter(os,true);
    }

    public void startNode(String name, Attribute[] attributes)
            throws XmlWriterException {
        super.startNode(name, attributes);
            level++;
    }

    public void endNode(String name)
            throws XmlWriterException {
        level--;
        super.endNode(name);
    }

    public void println(String text) {
        printSpacer();
        pw.println(text);
    }

    private void printSpacer() {
        for (int i = 0; i < level; i++) {
            pw.print(SPACER);
        }
    }
}
