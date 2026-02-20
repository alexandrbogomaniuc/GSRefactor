package com.dgphoenix.casino.common.util.test.api;

/**
 * @author <a href="mailto:fateyev@dgphoenix.com">Anton Fateyev</a>
 * @since 22.10.15
 */
public class RequestLogEntry {

    private final String url;
    private final String request;
    private final String response;
    private final Integer responseHTTPCode;

    public RequestLogEntry(String url, String request, String response) {
        this(url, request, response, null);
    }

    public RequestLogEntry(String url, String request, String response, Integer responseHTTPCode) {
        this.url = url;
        this.request = request;
        this.response = response;
        this.responseHTTPCode = responseHTTPCode;
    }

    public String getUrl() {
        return url;
    }

    public String getRequest() {
        return request;
    }

    public String getResponse() {
        return response;
    }

    public Integer getResponseHTTPCode() {
        return responseHTTPCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RequestLogEntry that = (RequestLogEntry) o;

        if (url != null ? !url.equals(that.url) : that.url != null) return false;
        if (request != null ? !request.equals(that.request) : that.request != null) return false;
        if (response != null ? !response.equals(that.response) : that.response != null) return false;
        return responseHTTPCode != null ? responseHTTPCode.equals(that.responseHTTPCode) : that.responseHTTPCode == null;
    }

    @Override
    public int hashCode() {
        int result = url != null ? url.hashCode() : 0;
        result = 31 * result + (request != null ? request.hashCode() : 0);
        result = 31 * result + (response != null ? response.hashCode() : 0);
        result = 31 * result + (responseHTTPCode != null ? responseHTTPCode.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "RequestLogEntry [" +
                String.format("url='%s'", url) +
                String.format(", request='%s'", request) +
                String.format(", response='%s'", response) +
                String.format(", responseHTTPCode='%s'", responseHTTPCode) +
                ']';
    }
}
