/**
 * User: val
 * Date: Jan 15, 2003
 * Time: 6:44:52 PM
 */
package com.abs.casino.common.util.xml.parser;

import com.dgphoenix.casino.common.exception.ParserException;
import com.abs.casino.common.util.xml.IXmlMessage;

public interface IXmlParser {

    void parse(IXmlMessage message,
               Object result) throws ParserException;

    void setXmlHandler(IXmlHandler handler);

}
