package com.dgphoenix.casino.common.util.web;

import com.dgphoenix.casino.common.exception.TransportException;
import org.apache.http.HttpEntity;

import java.util.Map;

/**
 * User: van0ss
 * Date: 23.11.2016
 */
public interface IHttpClientConnection {
    String doRequest(String url, Map<String, String> params) throws TransportException;

    StringBuilder doRequest(String url, String params, boolean post) throws TransportException;

    StringBuilder doRequest(String url, String params, boolean post, boolean plain) throws TransportException;

    StringBuilder doRequest(String url, String params, boolean post, boolean plain, boolean useProxy)
            throws TransportException;

    StringBuilder doRequest(String url, String params, boolean post, boolean plain, Map<String, String> requestHeaders,
                            boolean useProxy) throws TransportException;

    String doRequest(String url, Map<String, String> params, boolean post, boolean useProxy)
                    throws TransportException;

    String doRequest(String url, Map<String, String> params, boolean post) throws TransportException;

    String doRequest(String url, Map<String, String> params, boolean post, Map<String, String> requestHeaders,
                     boolean useProxy)
                            throws TransportException;

    HttpResponseWrapper doRequest(String url, Map<String, String> params, boolean post, String cookies)
                                    throws TransportException;

    HttpResponseWrapper doRequest(String url, Map<String, String> params, boolean post,
                                  boolean allowedEmptyResponse, String cookies)
                                            throws TransportException;

    String doPostRequest(String url, HttpEntity entity) throws TransportException;

    String doPostRequest(String url, HttpEntity entity, Map<String, String> requestHeaders, boolean useProxy)
                                                    throws TransportException;
}
