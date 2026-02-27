/**
 * User: val
 * Date: Jan 15, 2003
 * Time: 7:12:32 PM
 */
package com.abs.casino.common.util.xml.parser;

import com.dgphoenix.casino.common.exception.ParserException;
import com.abs.casino.common.util.xml.IXmlElement;
import com.abs.casino.common.util.xml.IXmlMessage;
import com.abs.casino.common.util.xml.XmlElement;
import org.apache.xerces.parsers.SAXParser;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import java.util.LinkedList;

public class XmlParser extends DefaultHandler implements IXmlParser {

    private LinkedList<IXmlElement> elements;
    private XMLReader parser;
    private IXmlHandler handler;
    private Object result;

    public XmlParser() throws ParserException {
        try {
            parser = new SAXParser();
            parser.setFeature("http://xml.org/sax/features/validation", false);
        } catch (Exception e) {
            throw new ParserException("XmlParser   " + " can't create XmlParser", e);
        }
    }

    public void startElement(String uri, String localName,
                             String qName, Attributes attributes)
            throws SAXException {

        IXmlElement element = new XmlElement(qName.trim());
        for (int i = 0; i < attributes.getLength(); i++) {
            element.addAttribute(attributes.getQName(i).trim().toUpperCase(), attributes.getValue(i));
        }
        elements.add(element);
    }

    public void endElement(String uri, String localName, String qName)
            throws SAXException {

        if (elements.size() > 0) {
            IXmlElement element = elements.removeLast();
            boolean isAtom = handler.isAtom(element.getName());
            if (elements.size() > 0) {
                if (!isAtom) {
                    IXmlElement el = (IXmlElement) elements.getLast();
                    el.addChild(element);
                }
            } else {
                elements.add(element);
            }
            // processed element
            if (isAtom) {
                try {
                    handler.process(element, elements, result);
                } catch (Exception e) {
                    throw new SAXException(e);
                }
            }
        }

    }

    public void characters(char ch[], int start, int length)
            throws SAXException {
        IXmlElement element = elements.getLast();
        String value = new String(ch, start, length);
        if (element.getValue() != null) {
            value = element.getValue() + value;
        }
        element.setValue(value);

    }

    public void parse(IXmlMessage message, Object result) throws ParserException {
        if (handler == null) {
            throw new ParserException(this.getClass().getName() + "  handle is undefined");
        }
        this.result = result;
        // Parse Document
        elements = new LinkedList<IXmlElement>();
        InputSource is = new InputSource(message.getAsReader());
        parser.setContentHandler(this);

        try {
            parser.parse(is);
        } catch (Exception e) {
            throw new ParserException(getClass().getName() + "   parse", e);
        }

    }

    public void setXmlHandler(IXmlHandler handler) {
        this.handler = handler;
    }


}
