package com.betsoft.casino.mp.transport;

import com.betsoft.casino.utils.TInboundObject;

import java.util.Objects;

/**
 * User: flsh
 * Date: 06.06.17.
 */
public class GetStartGameUrl extends TInboundObject {
    private Long roomId;
    private long stake;
    private String gameType;

    public GetStartGameUrl(long date, Long roomId, int rid, long stake) {
        super(date, rid);
        this.roomId = roomId;
        this.stake = stake;
    }

    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    public long getStake() {
        return stake;
    }

    public void setStake(long stake) {
        this.stake = stake;
    }

    public String getGameType() {
        return gameType;
    }

    public void setGameType(String gameType) {
        this.gameType = gameType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        GetStartGameUrl that = (GetStartGameUrl) o;
        return stake == that.stake &&
                Objects.equals(roomId, that.roomId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), roomId, stake);
    }

    @Override
    public String toString() {
        return "GetStartGameUrl[" +
                "roomId=" + roomId +
                ", stake=" + stake +
                ", gameType=" + gameType +
                ", rid=" + rid +
                ", date=" + date +
                ']';
    }
}
