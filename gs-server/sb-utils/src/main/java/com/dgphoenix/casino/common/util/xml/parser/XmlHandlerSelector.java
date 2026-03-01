/**
 * User: val
 * Date: Jan 29, 2003
 * Time: 8:32:21 PM
 */
package com.abs.casino.common.util.xml.parser;

import com.abs.casino.common.exception.CommonException;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import com.abs.casino.common.util.xml.IXmlMessage;
import org.apache.log4j.Logger;

public class XmlHandlerSelector extends DefaultHandler implements IXmlHandlerSelector {
    private static final Logger LOG = Logger.getLogger(XmlHandlerSelector.class);
    private IXmlHandler handler;

    public IXmlHandler getXmlHandler() {
        return handler;
    }

    public XmlHandlerSelector() {}

    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        // Resolve handler
        getHandler(qName.trim());
        if(handler == null) {
            throw new SAXException ("cannot resolve handler for: " + qName.trim());
        }
    }

    public IXmlHandler resolveHandler(IXmlMessage message)
            throws CommonException {

        try {

            Class c = Class.forName("org.apache.xerces.parsers.SAXParser");
            XMLReader parser = (XMLReader) c.newInstance();

            InputSource is = new InputSource(message.getAsReader());
            parser.setContentHandler(this);

            try {
                parser.parse(is);
            } catch(SAXException e) {
                //LOG.error("sax error", e);
                if(handler == null) {  //root handler must be resolved, inner tag handlers may be not defined (ugly code)
                    throw new CommonException("sax error", e);
                }
            }

            if (handler == null) {
                throw new CommonException("resolveHandler  "+"can't resolve handler for this message");
            }

            return handler;

        } catch(CommonException ce) {
            throw ce;
        } catch(Exception e) {
            throw new CommonException("resolveHandler   "+"can't resolve handler for this message", e);
        }

    }

    private void getHandler(String element) throws SAXException {

        try {
/*            if(LOG.isDebugEnabled()) {
                LOG.debug("XmlHandlerSelector :: getHandler, element = " + element);
            }*/
            handler = XmlHandlerRegistry.instance().getXmlHandler(element);
        } catch (Exception e) {
            //LOG.error("getHandler", e);
            throw new SAXException("can't resolve handler for tag: "+ element);
        }
    }


}
