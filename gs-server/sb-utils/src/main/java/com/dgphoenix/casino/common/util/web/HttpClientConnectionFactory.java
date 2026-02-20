package com.dgphoenix.casino.common.util.web;

/**
 * User: van0ss
 * Date: 17.11.2016
 */
public class HttpClientConnectionFactory {
    public IHttpClientConnection create() {
        return HttpClientConnection.newInstance();
    }

    public IHttpClientConnection create(long timeout) {
        return HttpClientConnection.newInstance(timeout);
    }

    public IHttpClientConnection create(IHttpClientConnectionCallbackHandler handler) {
        return HttpClientConnection.newInstance(handler);
    }

    public IHttpClientConnection create(IHttpClientConnectionCallbackHandler handler,
                                       IHttpClientConnectionNotOkCallbackHandler notOkHandler) {
        return HttpClientConnection.newInstance(handler, notOkHandler);
    }

    public IHttpClientConnection create(IHttpClientConnectionCallbackHandler callbackHandler,
                                       IHttpClientConnectionNotOkCallbackHandler notOkHandler,
                                       long timeout) {
        return HttpClientConnection.newInstance(callbackHandler, notOkHandler, timeout);
    }

    public IHttpClientConnection createSilent() {
        return HttpClientConnection.newInstanceSilent();
    }
}
