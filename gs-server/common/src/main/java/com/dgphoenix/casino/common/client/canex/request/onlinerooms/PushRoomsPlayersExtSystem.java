package com.abs.casino.common.client.canex.request.onlinerooms;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;

public class PushRoomsPlayersExtSystem {
    @JsonProperty("REQUEST")
    @SerializedName("REQUEST")
    private PushRoomsPlayersRequest request;

    @JsonProperty("RESPONSE")
    @SerializedName("RESPONSE")
    private PushRoomsPlayersResponse response;

    @JsonProperty("TIME")
    @SerializedName("TIME")
    private String time;

    public PushRoomsPlayersExtSystem() {
    }

    public PushRoomsPlayersRequest getRequest() {
        return request;
    }

    public void setRequest(PushRoomsPlayersRequest request) {
        this.request = request;
    }

    public PushRoomsPlayersResponse getResponse() {
        return response;
    }

    public void setResponse(PushRoomsPlayersResponse response) {
        this.response = response;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
