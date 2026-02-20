package com.dgphoenix.casino.controller.mqb.response;

import com.dgphoenix.casino.common.client.canex.request.onlineplayer.OnlinePlayer;

import java.util.List;

public class PostOnlinePlayersResponse extends BaseResult {

    private List<OnlinePlayer> onlinePlayers;

    public PostOnlinePlayersResponse(String result, String message, List<OnlinePlayer> onlinePlayers) {
        super(result, message);
        this.onlinePlayers = onlinePlayers;
    }

    public List<OnlinePlayer> getOnlinePlayers() {
        return onlinePlayers;
    }

    public void setOnlinePlayers(List<OnlinePlayer> onlinePlayers) {
        this.onlinePlayers = onlinePlayers;
    }
}
