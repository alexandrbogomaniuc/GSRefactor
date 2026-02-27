package com.abs.casino.common.util.xml.parser;

import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.util.xml.IXmlElement;

import java.util.LinkedList;

public interface IXmlElementProcessor {

    void process(IXmlElement element, LinkedList <IXmlElement> path, Object result)
            throws CommonException;


}
