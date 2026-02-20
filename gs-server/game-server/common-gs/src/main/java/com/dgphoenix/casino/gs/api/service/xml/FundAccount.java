package com.dgphoenix.casino.gs.api.service.xml;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import java.io.Serializable;

@XStreamAlias("EXTSYSTEM")
public class FundAccount implements Serializable {
    @XStreamAlias("REQUEST")
    private FundRequest request;

    @XStreamAlias("TIME")
    private String time;

    @XStreamAlias("RESPONSE")
    private FundResponse response;

    public FundAccount() {
    }

    public FundRequest getRequest() {
        return request;
    }

    public void setRequest(FundRequest request) {
        this.request = request;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public FundResponse getResponse() {
        return response;
    }

    public void setResponse(FundResponse response) {
        this.response = response;
    }

    @Override
    public String toString() {
        return "FundAccount{" +
                "request=" + request +
                ", time='" + time + '\'' +
                ", response=" + response +
                '}';
    }
}
