package com.dgphoenix.casino.common.client.canex.request.onlinerooms;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class PushRoomsPlayersResponse {

    @JsonProperty("RESULT")
    @SerializedName("RESULT")
    private String result;

    public PushRoomsPlayersResponse() {
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }
}
