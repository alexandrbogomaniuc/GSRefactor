package com.dgphoenix.casino.common.client.canex.request.friends;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class GetFriendsResponse {
    @JsonProperty("RESULT")
    @SerializedName("RESULT")
    private List<Friend> friends;

    public GetFriendsResponse() {
    }

    public List<Friend> getResult() {
        return friends;
    }

    public void setResult(List<Friend> friends) {
        this.friends = friends;
    }
}
