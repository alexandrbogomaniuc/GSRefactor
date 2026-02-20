package com.dgphoenix.casino.kafka.dto;

import java.util.List;

public class RMSRoomDto {
    private long roomId;
    private int serverId;
    private boolean isActive;
    private boolean isBattleground;
    private boolean isPrivate;
    private long buyInStake;
    private String currency;
    private long gameId;
    private String gameName;
    private List<RMSPlayerDto> players;

    public RMSRoomDto() {}

    public RMSRoomDto(long roomId,
            int serverId,
            boolean isActive,
            boolean isBattleground,
            boolean isPrivate,
            long buyInStake,
            String currency,
            long gameId,
            String gameName,
            List<RMSPlayerDto> players) {
        this.roomId = roomId;
        this.serverId = serverId;
        this.isActive = isActive;
        this.isBattleground = isBattleground;
        this.isPrivate = isPrivate;
        this.buyInStake = buyInStake;
        this.currency = currency;
        this.gameId = gameId;
        this.gameName = gameName;
        this.players = players;
    }

    public long getRoomId() {
        return roomId;
    }

    public int getServerId() {
        return serverId;
    }

    public boolean isActive() {
        return isActive;
    }

    public boolean isBattleground() {
        return isBattleground;
    }

    public boolean isPrivate() {
        return isPrivate;
    }

    public long getBuyInStake() {
        return buyInStake;
    }

    public String getCurrency() {
        return currency;
    }

    public long getGameId() {
        return gameId;
    }

    public String getGameName() {
        return gameName;
    }

    public List<RMSPlayerDto> getPlayers() {
        return players;
    }

    public void setRoomId(long roomId) {
        this.roomId = roomId;
    }

    public void setServerId(int serverId) {
        this.serverId = serverId;
    }

    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }

    public void setBattleground(boolean isBattleground) {
        this.isBattleground = isBattleground;
    }

    public void setPrivate(boolean isPrivate) {
        this.isPrivate = isPrivate;
    }

    public void setBuyInStake(long buyInStake) {
        this.buyInStake = buyInStake;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public void setGameId(long gameId) {
        this.gameId = gameId;
    }

    public void setGameName(String gameName) {
        this.gameName = gameName;
    }

    public void setPlayers(List<RMSPlayerDto> players) {
        this.players = players;
    }
}
