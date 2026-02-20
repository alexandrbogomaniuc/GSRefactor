/*
 * User: val
 * Date: 13.10.2002
 * Time: 14:21:07
 */
package com.dgphoenix.casino.common.util.xml.xmlwriter;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;

import com.dgphoenix.casino.common.exception.XmlWriterException;

public class XmlWriter {
    public static final String defaultEncoding = "iso-8859-1";

    private PrintWriter pw;
    private String encoding = defaultEncoding;

    public XmlWriter() {}

    public XmlWriter(Writer w) {
        this.pw = new PrintWriter(w,true);
    }

    public XmlWriter(Writer w, String encoding) {
        this.pw = new PrintWriter(w,true);
        this.encoding = encoding;
    }

    public XmlWriter(OutputStream os) {
        this.pw = new PrintWriter(os,true);
    }

    public XmlWriter(OutputStream os, String encoding) {
        this.pw = new PrintWriter(os,true);
        this.encoding = encoding;
    }

    public void header() {
        println("<?xml version=\"1.0\" encoding=\"" + encoding + "\" ?>");
    }

    public void startDocument(String document)
            throws XmlWriterException {
        header();
        startNode(document);
    }

    public void endDocument(String document)
            throws XmlWriterException {
        endNode(document);
    }

    public void startNode(String name)
            throws XmlWriterException {
        if (name != null && !name.trim().isEmpty()) {
            startNode(name,null);
        }
    }

    public void cdata(String content) {
        if(content != null) quotaPrintln(content, true);
    }

    public void cdata(String content, boolean backspacing) {
        if(content != null) quotaPrintln(content, backspacing);
    }
    public void endNode(String name)
            throws XmlWriterException {
        if (name != null && !name.trim().isEmpty()) {
            println("</" + name.toUpperCase() + ">");
        }
    }

    public void node(String name)
            throws XmlWriterException {
        node(name, null);
    }

    public void node(String name, String content)
            throws XmlWriterException {
            node(name,content,null);
    }

    public void node(String name, String content, boolean skipEmpty)
            throws XmlWriterException {
            node(name,content,null,skipEmpty);
    }

    public void startNode(String name, Attribute[] attributes, boolean backspacing)
            throws XmlWriterException  {
        if (name != null && !name.trim().isEmpty()) {
            String atrs = "";
            if(attributes !=null )
                for(Attribute atr:attributes) {
                    if(atr !=null) {
                        atrs+= " " + atr.getName().toUpperCase() + "=\"" + XmlQuota.quota(atr.getValue()) + "\"";
                    }
                }
            StringBuilder buff = new StringBuilder(name.length() + atrs.length() + 2);
            buff.append("<");
            buff.append(name.toUpperCase());
            buff.append(atrs);
            buff.append(">");

            if (backspacing) {
                println(buff.toString());
            } else {
                print(buff.toString());
            }
        }
    }

    public void startNode(String name, Attribute[] attributes)
            throws XmlWriterException  {
        startNode(name, attributes, true);
    }
    public void node(String name, String content, Attribute[] attributes)
            throws XmlWriterException{
        node(name,content,attributes,true);
    }
    /**
     * @param skipEmpty if true not prints elemetn with empty content
     * @throws XmlWriterException
     */
    public void node(String name, String content, Attribute[] attributes, boolean skipEmpty)
            throws XmlWriterException {
        if(name != null && !name.trim().isEmpty() &&
                content != null && !content.trim().isEmpty() && skipEmpty) {
            startNode(name, attributes, false);
            cdata(content, false);
            endNode(name);
        }
        else if (name != null && !name.trim().isEmpty() && ! skipEmpty){
            startNode(name,attributes);
            if (content != null && !content.trim().isEmpty()){
                cdata(content, false);
            }
            endNode(name);
        }
    }

    public void print(String text) {
        pw.print(text);
    }

    public void println(String text) {
        pw.println(text);
    }

    protected void quotaPrintln(String text, boolean backspacing) {
        if (backspacing) {
            println(XmlQuota.quota(text));
        } else {
            print(XmlQuota.quota(text));
        }
    }


}
