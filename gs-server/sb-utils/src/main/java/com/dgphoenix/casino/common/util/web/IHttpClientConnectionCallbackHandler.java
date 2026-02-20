package com.dgphoenix.casino.common.util.web;

import java.io.IOException;

/**
 * User: flsh
 * Date: 9/30/11
 */
public interface IHttpClientConnectionCallbackHandler {
    void timeout(String url);
    void emptyResponse(String url);
    void unclassifiedError(String url);
    void httpError503(String url, IOException e);
    void httpError500(String url, IOException e);
    void httpErrorUnclassified(String url, IOException e);
    void success(String url);
    void loginErrorByGameSessionsLimit();
    void longRequest(String url);
}
