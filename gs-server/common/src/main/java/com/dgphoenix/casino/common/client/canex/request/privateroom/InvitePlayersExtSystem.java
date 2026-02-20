package com.dgphoenix.casino.common.client.canex.request.privateroom;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;

public class InvitePlayersExtSystem {
    @JsonProperty("REQUEST")
    @SerializedName("REQUEST")
    private InvitePlayersRequest request;

    @JsonProperty("RESPONSE")
    @SerializedName("RESPONSE")
    private InvitePlayersResponse response;

    @JsonProperty("TIME")
    @SerializedName("TIME")
    private String time;

    public InvitePlayersExtSystem() {
    }

    public InvitePlayersRequest getRequest() {
        return request;
    }

    public void setRequest(InvitePlayersRequest request) {
        this.request = request;
    }

    public InvitePlayersResponse getResponse() {
        return response;
    }

    public void setResponse(InvitePlayersResponse response) {
        this.response = response;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
