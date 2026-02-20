package com.dgphoenix.casino.gs.api.service.xml;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import java.io.Serializable;

@XStreamAlias("EXTSYSTEM")
public class GetEnvironment implements Serializable {
    @XStreamAlias("REQUEST")
    private EnvironmentRequest request;

    @XStreamAlias("TIME")
    private String time;

    @XStreamAlias("RESPONSE")
    private EnvironmentResponse response;

    public GetEnvironment() {
    }

    public EnvironmentRequest getRequest() {
        return request;
    }

    public void setRequest(EnvironmentRequest request) {
        this.request = request;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public EnvironmentResponse getResponse() {
        return response;
    }

    public void setResponse(EnvironmentResponse response) {
        this.response = response;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("GetEnvironment");
        sb.append("[request=").append(request);
        sb.append(", time=").append(time);
        sb.append(", response=").append(response);
        sb.append(']');
        return sb.toString();
    }
}
