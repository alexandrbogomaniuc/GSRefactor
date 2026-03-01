/**
 * User: val
 * Date: Jan 29, 2003
 * Time: 5:36:17 PM
 */
package com.abs.casino.common.util.xml.parser;

import com.abs.casino.common.exception.CommonException;
import com.abs.casino.common.util.xml.IXmlElement;

import java.util.LinkedList;

public interface IXmlHandler {

    boolean isAtom(String name);

    void process(IXmlElement element, LinkedList <IXmlElement> path, Object result)
            throws CommonException;

}
