/**
 * User: val
 * Date: Jan 29, 2003
 * Time: 4:10:12 PM
 */
package com.dgphoenix.casino.common.util.xml;

import java.io.Reader;
import java.io.StringReader;

public class XmlMessage
        implements IXmlMessage {

    private String data;

    @SuppressWarnings("unused")
	private XmlMessage() {}

    public XmlMessage(String data) {
        this.data = data;
    }

    public Reader getAsReader() {
        return new StringReader(data);
    }

    public String toString() {
        return data;
    }
}
