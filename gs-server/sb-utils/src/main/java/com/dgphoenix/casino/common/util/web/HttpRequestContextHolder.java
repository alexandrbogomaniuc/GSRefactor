package com.abs.casino.common.util.web;

import com.abs.casino.common.util.support.*;

/**
 * @author <a href="mailto:noragami@dgphoenix.com">Alexander Aldokhin</a>
 * @since 19.06.2020
 */
public class HttpRequestContextHolder {

    private static final HttpRequestContextHolder httpRequestContextHolder = new HttpRequestContextHolder();
    private static final ThreadLocal<HttpCallInfo> httpCallInfo = new ThreadLocal<HttpCallInfo>();

    private HttpRequestContextHolder() {
    }

    public static HttpRequestContextHolder getRequestContext() {
        return httpRequestContextHolder;
    }

    public void create() {
        httpCallInfo.set(new HttpCallInfo(Thread.currentThread().getName()));
    }

    public void clear() {
        httpCallInfo.remove();
    }

    public boolean isInitialized() {
        return httpCallInfo.get() != null;
    }

    public HttpCallInfo getHttpCallInfo() {
        return httpCallInfo.get();
    }

    public void setRequest(String url, Request request) {
        if (isInitialized()) {
            httpCallInfo.get().setRequest(url, request);
        }
    }

    public void setResponse(Response response) {
        if (isInitialized()) {
            httpCallInfo.get().setResponse(response);
        }
    }

    public void setExceptionInfo(ExceptionInfo exceptionInfo) {
        if (isInitialized()) {
            httpCallInfo.get().setExceptionInfo(exceptionInfo);
        }
    }

    public void addAdditionalInfo(AdditionalInfoAttribute attribute, String value) {
        if (isInitialized()) {
            httpCallInfo.get().addAdditionalInfo(attribute, value);
        }
    }
}
