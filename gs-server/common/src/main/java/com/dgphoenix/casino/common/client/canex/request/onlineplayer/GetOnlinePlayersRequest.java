package com.abs.casino.common.client.canex.request.onlineplayer;

import com.abs.casino.common.client.canex.request.CanexJsonRequest;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class GetOnlinePlayersRequest extends CanexJsonRequest {
    @JsonProperty("externalIds")
    @SerializedName("externalIds")
    private List<String> externalIds;

    public GetOnlinePlayersRequest() {
    }

    public GetOnlinePlayersRequest(List<String> externalIds) {
        this.externalIds = externalIds;
    }

    public List<String> getExternalIds() {
        return externalIds;
    }

    public void setExternalIds(List<String> externalIds) {
        this.externalIds = externalIds;
    }

    @Override
    public String toString() {
        return "GetOnlinePlayersRequest{" +
                "externalIds=" + externalIds +
                '}';
    }
}
