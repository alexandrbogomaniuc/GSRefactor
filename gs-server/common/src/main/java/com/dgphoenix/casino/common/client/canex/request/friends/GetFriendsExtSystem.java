package com.dgphoenix.casino.common.client.canex.request.friends;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;

public class GetFriendsExtSystem {
    @JsonProperty("REQUEST")
    @SerializedName("REQUEST")
    private GetFriendsRequest request;

    @JsonProperty("RESPONSE")
    @SerializedName("RESPONSE")
    private GetFriendsResponse response;

    @JsonProperty("TIME")
    @SerializedName("TIME")
    private String time;

    public GetFriendsExtSystem() {
    }

    public GetFriendsRequest getRequest() {
        return request;
    }

    public void setRequest(GetFriendsRequest request) {
        this.request = request;
    }

    public GetFriendsResponse getResponse() {
        return response;
    }

    public void setResponse(GetFriendsResponse response) {
        this.response = response;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
