/**
 * User: val
 * Date: Jun 27, 2003
 * Time: 2:21:46 PM
 */
package com.abs.casino.common.util.xml;

public class XmlRequestError {

    protected String errorCode;
    protected String errorText;

    XmlRequestError() {}

    public XmlRequestError(String errorCode, String errorText) {
        this.errorCode = errorCode;
        this.errorText = errorText;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorText() {
        return errorText;
    }

}
