package com.dgphoenix.casino.common.client.canex.request.onlineplayer;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class GetOnlinePlayersResponse {
    @JsonProperty("RESULT")
    @SerializedName("RESULT")
    private List<OnlinePlayer> onlinePlayers;

    public GetOnlinePlayersResponse() {
    }

    public List<OnlinePlayer> getResult() {
        return onlinePlayers;
    }

    public void setResult(List<OnlinePlayer> onlinePlayers) {
        this.onlinePlayers = onlinePlayers;
    }
}
