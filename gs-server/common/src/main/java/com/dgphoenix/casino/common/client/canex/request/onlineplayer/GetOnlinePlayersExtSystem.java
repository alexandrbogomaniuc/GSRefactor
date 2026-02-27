package com.abs.casino.common.client.canex.request.onlineplayer;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;

public class GetOnlinePlayersExtSystem {
    @JsonProperty("REQUEST")
    @SerializedName("REQUEST")
    private GetOnlinePlayersRequest request;

    @JsonProperty("RESPONSE")
    @SerializedName("RESPONSE")
    private GetOnlinePlayersResponse response;

    @JsonProperty("TIME")
    @SerializedName("TIME")
    private String time;

    public GetOnlinePlayersExtSystem() {
    }

    public GetOnlinePlayersRequest getRequest() {
        return request;
    }

    public void setRequest(GetOnlinePlayersRequest request) {
        this.request = request;
    }

    public GetOnlinePlayersResponse getResponse() {
        return response;
    }

    public void setResponse(GetOnlinePlayersResponse response) {
        this.response = response;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
