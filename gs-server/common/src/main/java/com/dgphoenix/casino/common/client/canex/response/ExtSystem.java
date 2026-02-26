package com.abs.casino.common.client.canex.response;

import com.abs.casino.common.client.canex.request.CanexRequest;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;

public class ExtSystem {
    @JsonProperty("REQUEST")
    @SerializedName("REQUEST")
    private CanexRequest request;

    @JsonProperty("RESPONSE")
    @SerializedName("RESPONSE")
    private CanexResponse response;

    @JsonProperty("TIME")
    @SerializedName("TIME")
    private String time;

    public ExtSystem() {
    }

    public CanexRequest getRequest() {
        return request;
    }

    public void setRequest(CanexRequest request) {
        this.request = request;
    }

    public CanexResponse getResponse() {
        return response;
    }

    public void setResponse(CanexResponse response) {
        this.response = response;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
