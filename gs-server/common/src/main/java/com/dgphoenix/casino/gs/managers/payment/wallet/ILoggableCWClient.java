package com.abs.casino.gs.managers.payment.wallet;

import java.util.Map;

/**
 * User: flsh
 * Date: 16.05.13
 */
public interface ILoggableCWClient {
    void logUrl(String url);

    void logRequest(Map<String, String> params);

    void logResponse(String response);

    String getUrl();

    String getRequest();

    String getResponse();

    void setLoggableContainer(ILoggableContainer loggableContainer);
}
