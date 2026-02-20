package com.dgphoenix.casino.bonus;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import java.io.Serializable;

@XStreamAlias("EXTSYSTEM")
public class GetBonusInfoResponse implements Serializable {
    @XStreamAlias("REQUEST")
    private BonusInfoRequest request;

    @XStreamAlias("TIME")
    private String time;

    @XStreamAlias("RESPONSE")
    private BonusInfoResponse response;

    public GetBonusInfoResponse() {
    }

    public BonusInfoRequest getRequest() {
        return request;
    }

    public void setRequest(BonusInfoRequest request) {
        this.request = request;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public BonusInfoResponse getResponse() {
        return response;
    }

    public void setResponse(BonusInfoResponse response) {
        this.response = response;
    }


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("GetBonusInfoResponse");
        sb.append("[request=").append(request);
        sb.append(", time=").append(time);
        sb.append(", response=").append(response);
        sb.append(']');
        return sb.toString();
    }
}
