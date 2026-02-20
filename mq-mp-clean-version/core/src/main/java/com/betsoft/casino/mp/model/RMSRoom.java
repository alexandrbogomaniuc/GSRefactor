package com.betsoft.casino.mp.model;

import java.util.List;

public class RMSRoom implements IRMSRoom {
    private long roomId;
    private int serverId;
    private boolean isActive;
    private boolean isBattleground;
    private boolean isPrivate;
    private long buyInStake;
    private String currency;
    private long gameId;
    private String gameName;
    private List<IRMSPlayer> players;

    public RMSRoom() {}

    public RMSRoom(long roomId,
            int serverId,
            boolean isActive,
            boolean isBattleground,
            boolean isPrivate,
            long buyInStake,
            String currency,
            long gameId,
            String gameName,
            List<IRMSPlayer> players) {
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

    @Override
    public long getRoomId() {
        return roomId;
    }

    @Override
    public int getServerId() {
        return serverId;
    }

    @Override
    public boolean isIsActive() {
        return isActive;
    }

    @Override
    public boolean isIsBattleground() {
        return isBattleground;
    }

    @Override
    public boolean isIsPrivate() {
        return isPrivate;
    }

    @Override
    public long getBuyInStake() {
        return buyInStake;
    }

    @Override
    public String getCurrency() {
        return currency;
    }

    @Override
    public long getGameId() {
        return gameId;
    }

    @Override
    public String getGameName() {
        return gameName;
    }

    @Override
    public List<IRMSPlayer> getPlayers() {
        return players;
    }

    public void setRoomId(long roomId) {
        this.roomId = roomId;
    }

    public void setServerId(int serverId) {
        this.serverId = serverId;
    }

    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
    }

    public void setIsBattleground(boolean isBattleground) {
        this.isBattleground = isBattleground;
    }

    public void setIsPrivate(boolean isPrivate) {
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

    public void setPlayers(List<IRMSPlayer> players) {
        this.players = players;
    }
}
