package com.abs.casino.gs.managers.payment.wallet;

/**
 * @author <a href="mailto:fateyev@dgphoenix.com">Anton Fateyev</a>
 * @since 21.09.16
 */
public interface ILoggableContainer {

    void logUrl(String url);

    void logRequest(String request);

    void logResponse(String response);

    String getUrl();

    String getRequest();

    String getResponse();
}
