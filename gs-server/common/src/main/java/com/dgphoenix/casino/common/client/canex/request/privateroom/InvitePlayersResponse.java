package com.abs.casino.common.client.canex.request.privateroom;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;

public class InvitePlayersResponse {

    @JsonProperty("RESULT")
    @SerializedName("RESULT")
    private String result;

    public InvitePlayersResponse() {
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }
}
