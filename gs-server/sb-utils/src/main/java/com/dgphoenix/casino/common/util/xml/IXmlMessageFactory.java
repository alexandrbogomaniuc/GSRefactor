/**
 * User: val
 * Date: Jan 29, 2003
 * Time: 8:00:13 PM
 */
package com.abs.casino.common.util.xml;

import com.dgphoenix.casino.common.exception.CommonException;

import java.io.InputStream;
import java.io.Reader;

public interface IXmlMessageFactory {

    IXmlMessage getMessage(String data) throws CommonException;
    IXmlMessage getMessage(StringBuilder data) throws CommonException;
    IXmlMessage getMessage(Reader data) throws CommonException;
    IXmlMessage getMessage(InputStream data) throws CommonException;

}
