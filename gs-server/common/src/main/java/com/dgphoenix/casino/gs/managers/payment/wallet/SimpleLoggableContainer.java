package com.abs.casino.gs.managers.payment.wallet;

import static org.apache.commons.lang.StringUtils.EMPTY;

/**
 * @author <a href="mailto:fateyev@dgphoenix.com">Anton Fateyev</a>
 * @since 21.09.16
 */
public class SimpleLoggableContainer implements ILoggableContainer, ILoggableResponseCode {

    private String url = EMPTY;
    private String request = EMPTY;
    private String response = EMPTY;
    private int responseHTTPCode;

    @Override
    public void logUrl(String url) {
        this.url = url;
        this.request = EMPTY;
        this.response = EMPTY;
        this.responseHTTPCode = 0;
    }

    @Override
    public void logRequest(String request) {
        this.request = request;
    }

    @Override
    public void logResponse(String response) {
        this.response = response;
    }

    @Override
    public void logResponseHTTPCode(Integer code) {
        this.responseHTTPCode = code;
    }

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public String getRequest() {
        return request;
    }

    @Override
    public String getResponse() {
        return response;
    }

    @Override
    public Integer getResponseHTTPCode() {
        return responseHTTPCode;
    }
}
