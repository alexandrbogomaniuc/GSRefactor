package com.dgphoenix.casino.gs.managers.payment.wallet.common.xml;

import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.util.xml.IXmlElement;
import com.dgphoenix.casino.common.util.xml.XmlRequestResult;
import com.abs.casino.common.util.xml.parser.IXmlElementProcessor;

import java.util.Iterator;
import java.util.LinkedList;

public class CWResponseProcessor implements IXmlElementProcessor {
    public static final String RESULT_TAG = "result".toUpperCase();

    public void process(IXmlElement element, LinkedList<IXmlElement> path, Object result) throws CommonException {
        XmlRequestResult res = (XmlRequestResult) result;
        Iterator it = element.getChilds().iterator();
        while (it.hasNext()) {
            IXmlElement el = (IXmlElement) it.next();
            String value = el.getValue() == null ? null : el.getValue().trim();
            if (el.getName().equals(RESULT_TAG)) res.setStatus(value);
            res.getResponseParameters().put(el.getName(), value);
        }
    }

}
