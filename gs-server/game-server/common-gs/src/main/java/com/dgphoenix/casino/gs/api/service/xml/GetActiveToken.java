package com.dgphoenix.casino.gs.api.service.xml;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import java.io.Serializable;

@XStreamAlias("EXTSYSTEM")
public class GetActiveToken implements Serializable {
    @XStreamAlias("REQUEST")
    private TokenRequest request;

    @XStreamAlias("TIME")
    private String time;

    @XStreamAlias("RESPONSE")
    private TokenResponse response;

    public GetActiveToken() {
    }

    public TokenRequest getRequest() {
        return request;
    }

    public void setRequest(TokenRequest request) {
        this.request = request;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public TokenResponse getResponse() {
        return response;
    }

    public void setResponse(TokenResponse response) {
        this.response = response;
    }

    @Override
    public String toString() {
        return "GetActiveToken{" +
                "request=" + request +
                ", time='" + time + '\'' +
                ", response=" + response +
                '}';
    }
}
