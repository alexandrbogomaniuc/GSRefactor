/**
 * User: val
 * Date: Jan 29, 2003
 * Time: 5:19:06 PM
 */
package com.abs.casino.common.util.xml.parser;

import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.util.xml.IXmlMessage;

public interface IXmlHandlerSelector {

    IXmlHandler getXmlHandler();
    IXmlHandler resolveHandler(IXmlMessage message)
            throws CommonException;
}
