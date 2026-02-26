package com.abs.casino.common.rest;

import com.dgphoenix.casino.common.util.support.ExceptionInfo;
import com.dgphoenix.casino.common.util.web.HttpRequestContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.DefaultResponseErrorHandler;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:noragami@dgphoenix.com">Alexander Aldokhin</a>
 * @since 22.06.2020
 */
public class CustomResponseErrorHandler extends DefaultResponseErrorHandler {

    private final HttpRequestContextHolder httpRequestContext = HttpRequestContextHolder.getRequestContext();

    private List<HttpStatus> acceptedStatuses = Collections.singletonList(HttpStatus.INTERNAL_SERVER_ERROR);
    private List<MediaType> acceptedContentTypes = Arrays.asList(MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON_UTF8);

    public CustomResponseErrorHandler() {
    }

    public CustomResponseErrorHandler(MediaType acceptedContentType) {
        acceptedContentTypes = Collections.singletonList(acceptedContentType);
    }

    public CustomResponseErrorHandler(List<HttpStatus> acceptedStatuses) {
        this.acceptedStatuses = acceptedStatuses;
    }

    public CustomResponseErrorHandler(List<HttpStatus> acceptedStatuses, List<MediaType> acceptedContentTypes) {
        this.acceptedStatuses = acceptedStatuses;
        this.acceptedContentTypes = acceptedContentTypes;
    }

    @Override
    public boolean hasError(ClientHttpResponse response) throws IOException {
        MediaType contentType = response.getHeaders().getContentType();
        HttpStatus statusCode;
        try {
            statusCode = response.getStatusCode();
        } catch (IllegalArgumentException e) {
            return false;
        }
        if (acceptedContentTypes.contains(contentType) && acceptedStatuses.contains(statusCode)) {
            return false;
        } else {
            return super.hasError(response);
        }
    }

    @Override
    public void handleError(ClientHttpResponse response) throws IOException {
        try {
            super.handleError(response);
        } catch (Exception e) {
            httpRequestContext.setExceptionInfo(new ExceptionInfo(e, System.currentTimeMillis()));
            throw e;
        }
    }
}
