/**
 * User: val
 * Date: Jan 29, 2003
 * Time: 8:01:23 PM
 */
package com.dgphoenix.casino.common.util.xml;

import com.dgphoenix.casino.common.exception.CommonException;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

public class XmlMessageFactory
        implements IXmlMessageFactory {

    protected final static IXmlMessageFactory instance=new XmlMessageFactory();

    protected XmlMessageFactory() { }

    public static IXmlMessageFactory instance() {
        return instance;
    }

    public IXmlMessage getMessage(String data)
            throws CommonException {
        try {
            return new XmlMessage(data);
        } catch(Exception e) {
            throw new CommonException("getMessage  "+"unable to get message", e);
        }
    }

    public IXmlMessage getMessage(StringBuilder data)
            throws CommonException{
        try {
            return new XmlMessage(data.toString());
        } catch(Exception e) {
            throw new CommonException("getMessage  "+"unable to get message", e);
        }
    }

    public IXmlMessage getMessage(Reader data)
            throws CommonException {
        try {
            StringBuilder sb = new StringBuilder();
            readFromReader(sb, data);
            return new XmlMessage(sb.toString());
        } catch(Exception e) {
            throw new CommonException("getMessage  "+"unable to get message", e);
        }
    }

    public IXmlMessage getMessage(InputStream data)
            throws CommonException{
        try {
            StringBuilder sb = new StringBuilder();
            InputStreamReader isr = new InputStreamReader(data);
            readFromReader(sb, isr);

            return new XmlMessage(sb.toString());
        } catch(Exception e) {
            throw new CommonException("getMessage  "+"unable to get message", e);
        }
    }

    private void readFromReader(StringBuilder sb, Reader reader)
            throws Exception {
        char[] buff = new char[1024];
        int len;

        while((len = reader.read(buff)) != -1) {
            sb.append(buff,0,len);
        }
    }
}
