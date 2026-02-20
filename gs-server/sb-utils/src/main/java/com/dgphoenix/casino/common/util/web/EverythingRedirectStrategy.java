package com.dgphoenix.casino.common.util.web;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.ProtocolException;
import org.apache.http.client.RedirectStrategy;
import org.apache.http.client.methods.*;
import org.apache.http.protocol.HttpContext;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

import static org.apache.http.client.utils.CloneUtils.cloneObject;

public class EverythingRedirectStrategy implements RedirectStrategy {

    private static final List<Integer> REDIRECT_ON = Arrays.asList(
            HttpStatus.SC_MOVED_PERMANENTLY,
            HttpStatus.SC_MOVED_TEMPORARILY,
            HttpStatus.SC_TEMPORARY_REDIRECT
    );

    @Override
    public boolean isRedirected(HttpRequest request, HttpResponse response, HttpContext context) throws ProtocolException {
        return REDIRECT_ON.contains(response.getStatusLine().getStatusCode());
    }

    @Override
    public HttpUriRequest getRedirect(HttpRequest request, HttpResponse response, HttpContext context) throws ProtocolException {
        String query = URI.create(request.getRequestLine().getUri()).normalize().getQuery();
        String location = response.getFirstHeader("Location").getValue();
        HttpUriRequest nRequest;
        if (query != null) {
            nRequest = (location.indexOf('?') == -1) ? getNewRequest(request, location + "?" + query) : getNewRequest(request,
                    location);
        } else {
            nRequest = getNewRequest(request, location);
        }
        nRequest.setHeaders(request.getAllHeaders());
        if (request instanceof HttpEntityEnclosingRequestBase) {
            try {
                HttpEntityEnclosingRequestBase eRequest = (HttpEntityEnclosingRequestBase) request;
                ((HttpEntityEnclosingRequestBase) nRequest).setEntity(cloneObject(eRequest.getEntity()));
            } catch (CloneNotSupportedException e) {
                throw new ProtocolException("Content type clone error", e);
            }
        }
        return nRequest;
    }

    private HttpUriRequest getNewRequest(HttpRequest request, String location) throws ProtocolException {
        HttpUriRequest nRequest;
        String method = request.getRequestLine().getMethod();
        if (HttpGet.METHOD_NAME.equalsIgnoreCase(method)) {
            nRequest = new HttpGet(location);
        } else if (HttpPost.METHOD_NAME.equalsIgnoreCase(method)) {
            nRequest = new HttpPost(location);
        } else if (HttpHead.METHOD_NAME.equalsIgnoreCase(method)) {
            nRequest = new HttpHead(location);
        } else if (HttpPut.METHOD_NAME.equalsIgnoreCase(method)) {
            nRequest = new HttpPut(location);
        } else if (HttpOptions.METHOD_NAME.equalsIgnoreCase(method)) {
            nRequest = new HttpOptions(location);
        } else if (HttpPatch.METHOD_NAME.equalsIgnoreCase(method)) {
            nRequest = new HttpPatch(location);
        } else if (HttpPut.METHOD_NAME.equalsIgnoreCase(method)) {
            nRequest = new HttpPut(location);
        } else if (HttpTrace.METHOD_NAME.equalsIgnoreCase(method)) {
            nRequest = new HttpTrace(location);
        } else if (HttpDelete.METHOD_NAME.equalsIgnoreCase(method)) {
            nRequest = new HttpDelete(location);
        } else {
            throw new ProtocolException("Unknown Http method: " + method);
        }
        return nRequest;
    }
}