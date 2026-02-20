package com.betsoft.casino.mp.transport;

import com.betsoft.casino.utils.TInboundObject;

import java.util.Objects;

public class JoinBattleground extends TInboundObject {
    private int gameId;
    private long buyIn;

    public JoinBattleground(long date, int rid, int gameId, long buyIn) {
        super(date, rid);
        this.gameId = gameId;
        this.buyIn = buyIn;
    }

    public int getGameId() {
        return gameId;
    }

    public void setGameId(int gameId) {
        this.gameId = gameId;
    }

    public long getBuyIn() {
        return buyIn;
    }

    public void setBuyIn(long buyIn) {
        this.buyIn = buyIn;
    }

    @Override
    public boolean equals(Object joinBattleground) {
        if (this == joinBattleground) return true;
        if (joinBattleground == null || getClass() != joinBattleground.getClass()) return false;
        if (!super.equals(joinBattleground)) return false;
        JoinBattleground that = (JoinBattleground) joinBattleground;
        return gameId == that.gameId && buyIn == that.buyIn;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), gameId, buyIn);
    }

    @Override
    public String toString() {
        return "JoinBattleground{" +
                "inboundDate=" + inboundDate +
                ", date=" + date +
                ", rid=" + rid +
                ", gameId=" + gameId +
                ", buyIn=" + buyIn +
                '}';
    }
}
