package com.abs.casino.gs.managers.payment.wallet.common.xml;

import com.abs.casino.common.exception.CommonException;
import com.abs.casino.common.util.xml.IXmlElement;
import com.abs.casino.common.util.xml.parser.IXmlElementProcessor;

import java.util.LinkedList;
import java.util.Map;

/**
 * User: flsh
 * Date: 14.06.13
 */
public class ExtraDataProcessor implements IXmlElementProcessor {
    public void process(IXmlElement element, LinkedList<IXmlElement> path, Object result) throws CommonException {
        Map<String, String> res = (Map<String, String>) result;
        res.putAll(element.getAttributes());
    }
}
