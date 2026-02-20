package com.dgphoenix.casino.kafka.dto;

import java.util.Set;

public class RunningRoomDto {

    private long roomId;
    private int gameId;
    private Set<String> observers;
    private boolean isPrivate;

    public RunningRoomDto() {
    }

    public RunningRoomDto(long roomId, int gameId, Set<String> observers, boolean isPrivate) {
        this.roomId = roomId;
        this.gameId = gameId;
        this.observers = observers;
        this.isPrivate = isPrivate;
    }

    public long getRoomId() {
        return roomId;
    }

    public void setRoomId(long roomId) {
        this.roomId = roomId;
    }

    public int getGameId() {
        return gameId;
    }

    public void setGameId(int gameId) {
        this.gameId = gameId;
    }

    public Set<String> getObservers() {
        return observers;
    }

    public void setObservers(Set<String> observers) {
        this.observers = observers;
    }

    public boolean isPrivate() {
        return isPrivate;
    }

    public void setPrivate(boolean aPrivate) {
        isPrivate = aPrivate;
    }

    @Override
    public String toString() {
        return "RunningRoomDto{" +
                "roomId=" + roomId +
                ", gameId=" + gameId +
                ", observers=" + observers +
                ", isPrivate=" + isPrivate +
                '}';
    }
}
