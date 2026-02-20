package com.dgphoenix.casino.common.client.canex.request.onlineplayer;

import java.util.List;

public class UpdateOnlinePlayersResponse {

    private int code;
    private String message;
    private List<OnlinePlayer> onlinePlayers;

    public UpdateOnlinePlayersResponse(int code, String message, List<OnlinePlayer> onlinePlayers) {
        this.code = code;
        this.message = message;
        this.onlinePlayers = onlinePlayers;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<OnlinePlayer> getOnlinePlayers() {
        return onlinePlayers;
    }

    public void setOnlinePlayers(List<OnlinePlayer> onlinePlayers) {
        this.onlinePlayers = onlinePlayers;
    }

    @Override
    public String toString() {
        return "UpdateOnlinePlayersResponse{" +
                "code=" + code +
                ", message='" + message + '\'' +
                ", onlinePlayers=" + onlinePlayers +
                '}';
    }
}
