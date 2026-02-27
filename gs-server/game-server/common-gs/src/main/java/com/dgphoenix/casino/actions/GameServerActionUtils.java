package com.abs.casino.actions;

import com.dgphoenix.casino.common.util.support.Request;
import com.dgphoenix.casino.common.util.web.HttpRequestContextHolder;
import com.dgphoenix.casino.gs.GameServer;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;

/**
 * User: flsh
 * Date: 8/9/12
 */
public class GameServerActionUtils {

    private GameServerActionUtils() {
    }

    public static String getServerUrl(HttpServletRequest request) {
        return request.getScheme() + "://" + GameServer.getInstance().getHost();
    }

    public static String getFullUrl(HttpServletRequest servletRequest) {
        String url = servletRequest.getRequestURL().toString();
        return servletRequest.getQueryString() != null ? url + "?" + servletRequest.getQueryString() : url;
    }

    public static void initializeHttpRequestContext(HttpServletRequest servletRequest) {
        HttpRequestContextHolder httpRequestContext = HttpRequestContextHolder.getRequestContext();
        if (!httpRequestContext.isInitialized()) {
            String url = getFullUrl(servletRequest);
            Request request = new Request(url, Collections.emptyMap(), "POST".equals(servletRequest.getMethod()), System.currentTimeMillis());
            httpRequestContext.create();
            httpRequestContext.setRequest(url, request);
        }
    }
}
