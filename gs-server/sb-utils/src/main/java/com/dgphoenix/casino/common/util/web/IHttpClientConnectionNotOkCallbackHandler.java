package com.abs.casino.common.util.web;

/**
 * User: Grien
 * Date: 27.05.2014 19:34
 */
public interface IHttpClientConnectionNotOkCallbackHandler {
    void onNotOkResult(String url, int returnCode, String response);
}
