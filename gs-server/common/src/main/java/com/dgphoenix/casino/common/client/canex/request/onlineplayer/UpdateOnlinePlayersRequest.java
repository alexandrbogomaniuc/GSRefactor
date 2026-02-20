package com.dgphoenix.casino.common.client.canex.request.onlineplayer;

import com.dgphoenix.casino.common.client.canex.request.CanexJsonRequest;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class UpdateOnlinePlayersRequest extends CanexJsonRequest {

    @JsonProperty("onlinePlayers")
    @SerializedName("onlinePlayers")
    private List<OnlinePlayer> onlinePlayers;

    // Default constructor
    public UpdateOnlinePlayersRequest() {
    }

    public UpdateOnlinePlayersRequest(List<OnlinePlayer> onlinePlayers) {
        this.onlinePlayers = onlinePlayers;
    }

    public List<OnlinePlayer> getOnlinePlayers() {
        return onlinePlayers;
    }

    public void setOnlinePlayers(List<OnlinePlayer> onlinePlayers) {
        this.onlinePlayers = onlinePlayers;
    }

    @Override
    public String toString() {
        return "UpdateOnlinePlayersRequest{" +
                ", onlinePlayers=" + onlinePlayers +
                '}';
    }
}
