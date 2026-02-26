package com.abs.casino.actions.api;

import com.dgphoenix.casino.common.exception.CommonException;

/**
 * User: oleg
 * Date: 10.05.12
 * Time: 17:29
 */
public class ValidateException extends CommonException {

    private String xmlResult;

    public ValidateException(String aXmlResult) {
        super();
        xmlResult = aXmlResult;
    }

    public String getXmlResult() {
        return xmlResult;
    }

    public void setXmlResult(String xmlResult) {
        this.xmlResult = xmlResult;
    }
}
