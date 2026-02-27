package com.abs.casino.common.util.web;

import com.dgphoenix.casino.common.util.string.StringUtils;
import org.apache.http.client.CookieStore;

/**
 * User: flsh
 * Date: 11.12.14.
 */
public class HttpResponseWrapper {
    private String body;
    private String cookies;
    private CookieStore cookieStore;

    public HttpResponseWrapper() {
    }

    public HttpResponseWrapper(String body, String cookies) {
        this.body = body;
        this.cookies = cookies;
    }

    public HttpResponseWrapper(String body, String cookies, CookieStore cookieStore) {
        this.body = body;
        this.cookies = cookies;
        this.cookieStore = cookieStore;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getCookies() {
        return cookies;
    }

    public void setCookies(String cookies) {
        this.cookies = cookies;
    }

    public CookieStore getCookieStore() {
        return cookieStore;
    }

    public void setCookieStore(CookieStore cookieStore) {
        this.cookieStore = cookieStore;
    }

    @Override
    public String toString() {
        return "HttpResponseWrapper [" +
                "body='" + body + '\'' +
                ", cookies='" + cookies + '\'' +
                ']';
    }

    public boolean isEmpty() {
        return StringUtils.isTrimmedEmpty(body);
    }
}
