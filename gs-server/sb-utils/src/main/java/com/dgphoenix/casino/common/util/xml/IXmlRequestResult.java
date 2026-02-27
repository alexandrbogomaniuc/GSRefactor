/**
 * User: val
 * Date: Jan 13, 2003
 * Time: 8:39:52 PM
 */
package com.abs.casino.common.util.xml;

import java.util.HashMap;

public interface IXmlRequestResult {

    HashMap <String,Object> getRequestParameters();
    HashMap <String,Object> getResponseParameters();

    String getStatus();
    boolean isSuccessful();
    boolean isSuccessful(String referenceValue);

    String getErrorCode(String errorKey);
    String getErrorText(String errorKey);

    void setStatus(String status);

    void setError(String errorKey,
                  String errorCode,
                  String errorText);

    XmlRequestError getError(String errorKey);

    void putRequestParameter(String key, Object value);
    void putResponseParameter(String key, Object value);

    String getResponseCode();
}
