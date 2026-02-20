package com.betsoft.casino.mp.transport;

import com.betsoft.casino.utils.TInboundObject;

import java.util.Objects;

public class GetBattlegroundStartGameUrl extends TInboundObject {
    private long buyIn;
    private Long roomId;

    public GetBattlegroundStartGameUrl(long date, int rid, long buyIn, Long roomId) {
        super(date, rid);
        this.buyIn = buyIn;
        this.roomId = roomId;
    }

    public long getBuyIn() {
        return buyIn;
    }

    public void setBuyIn(long buyIn) {
        this.buyIn = buyIn;
    }

    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        GetBattlegroundStartGameUrl that = (GetBattlegroundStartGameUrl) o;
        return buyIn == that.buyIn &&
                Objects.equals(roomId, that.roomId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), buyIn, roomId);
    }

    @Override
    public String toString() {
        return "GetBattlegroundStartGameUrl{" +
                "date=" + date +
                ", rid=" + rid +
                ", buyIn=" + buyIn +
                ", roomId=" + roomId +
                '}';
    }
}
