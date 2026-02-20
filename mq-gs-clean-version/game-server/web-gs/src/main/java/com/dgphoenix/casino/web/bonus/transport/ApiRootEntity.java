package com.dgphoenix.casino.web.bonus.transport;

/**
 * User: flsh
 * Date: 06.02.13
 */
public class ApiRootEntity {
    private Request request;
    private Response response;

    public ApiRootEntity() {
    }

    public ApiRootEntity(Request request, Response response) {
        this.request = request;
        this.response = response;
    }

    public Request getRequest() {
        return request;
    }

    public void setRequest(Request request) {
        this.request = request;
    }

    public Response getResponse() {
        return response;
    }

    public void setResponse(Response response) {
        this.response = response;
    }


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("ApiRootEntity");
        sb.append("[request=").append(request);
        sb.append(", response=").append(response);
        sb.append(']');
        return sb.toString();
    }
}
