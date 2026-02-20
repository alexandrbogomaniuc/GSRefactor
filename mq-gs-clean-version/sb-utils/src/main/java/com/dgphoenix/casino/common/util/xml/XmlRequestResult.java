/**
 * User: val
 * Date: Jan 13, 2003
 * Time: 8:37:22 PM
 */
package com.dgphoenix.casino.common.util.xml;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;

public class XmlRequestResult implements IXmlRequestResult {

    public final static String RESULT_OK = "OK";
    public final static String RESPONSE_CODE_PARAM = "CODE";

    private HashMap <String,Object> requestParameters;
    private HashMap <String,Object> responseParameters;

    private String status;
    private HashMap <String,XmlRequestError> errors;

    public XmlRequestResult() {
        requestParameters = new HashMap <String,Object> ();
        responseParameters = new HashMap <String,Object> ();
        errors = new HashMap <String,XmlRequestError> ();
    }

    public boolean isSuccessful() {
        return getStatus() != null && getStatus().equalsIgnoreCase(RESULT_OK);
    }

    public boolean isSuccessful(String referenceValue) {
        return getStatus() != null && getStatus().equalsIgnoreCase(referenceValue);
    }

    public HashMap <String,Object> getRequestParameters() {
        return requestParameters;
    }

    public HashMap <String,Object> getResponseParameters() {
        return responseParameters;
    }

    @Override
    public String getResponseCode() {
        return (String) responseParameters.get(RESPONSE_CODE_PARAM);
    }

    public String getStatus() {
        return status;
    }

    public String getErrorCode(String errorKey) {
        if (getError(errorKey) != null)
            return getError(errorKey).getErrorCode();

        return null;
    }

    public String getErrorText(String errorKey) {
        if (getError(errorKey) != null)
            return getError(errorKey).getErrorText();

        return null;
    }

    public XmlRequestError getError(String errorKey) {
        if (errors.get(errorKey) != null)
            return errors.get(errorKey);

        return null;
    }

    public void setError(String errorKey, String errorCode, String errorText) {
        XmlRequestError result = new XmlRequestError(errorCode, errorText);
        errors.put(errorKey, result);
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void putRequestParameter(String key, Object value) {
        requestParameters.put(key, value);
    }
    public void putResponseParameter(String key, Object value) {
        responseParameters.put(key, value);
    }

    public String toString() {

        String request = "";
        Enumeration <String> keys = Collections.enumeration(requestParameters.keySet());
        while(keys.hasMoreElements()) {
            String key = keys.nextElement();
            request = request + key + " = " +  requestParameters.get(key) +"\n";
        }

        String response = "";
        keys = Collections.enumeration(responseParameters.keySet());
        while(keys.hasMoreElements()) {
            String key = keys.nextElement();
            response = response + key + " = " +  responseParameters.get(key) +"\n";
        }

        return "\nSTATUS=[" + getStatus() + "], \n" +
                "REQUEST: \n" +
                request +
                "RESPONSE: \n" +
                response;
    }
}
