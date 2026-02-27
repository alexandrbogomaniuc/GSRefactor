/**
 * User: val
 * Date: Jan 31, 2003
 * Time: 1:02:39 PM
 */
package com.abs.casino.common.util.xml.parser;

import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.exception.ParserException;
import com.dgphoenix.casino.common.util.xml.IXmlMessage;
import com.dgphoenix.casino.common.util.xml.XmlMessageFactory;

public class Parser {

    static final protected Parser instance = new Parser();

    static public Parser instance() {
        return instance;
    }

    public void parse(StringBuilder text, Object result) throws CommonException {
        parse(text.toString(), result);
    }

    public void parse(String text, Object result) throws CommonException {
        try {
            IXmlMessage message = XmlMessageFactory.instance().getMessage(text.trim());
            // Get Xml
            IXmlHandlerSelector selector = new XmlHandlerSelector();
            IXmlHandler handler = selector.resolveHandler(message);
            parse(message, handler, result);
        } catch (CommonException ce) {
            throw ce;
        } catch (Exception e) {
            throw new CommonException("parse exception!", e);
        }

    }

    public void parse(String text, IXmlHandler handler, Object result) throws CommonException {
        try {
            IXmlMessage message = XmlMessageFactory.instance().getMessage(text.trim());
            parse(message, handler, result);
        } catch (CommonException ce) {
            throw ce;
        } catch (Exception e) {
            throw new CommonException("parse error", e);
        }
    }

    private void parse(IXmlMessage message,
                       IXmlHandler handler,
                       Object result) throws ParserException {
        XmlParser parser = new XmlParser();
        parser.setXmlHandler(handler);
        parser.parse(message, result);
    }

}
