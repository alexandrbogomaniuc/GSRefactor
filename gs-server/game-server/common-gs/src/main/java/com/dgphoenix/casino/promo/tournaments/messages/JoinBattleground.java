package com.abs.casino.promo.tournaments.messages;

import com.dgphoenix.casino.common.transport.TInboundObject;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.util.Objects;

public class JoinBattleground extends TInboundObject {
    public static final byte VERSION = 1;
    private int gameId;
    private long buyIn;

    public JoinBattleground() {}

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
    protected byte getVersion() {
        return VERSION;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        super.write(kryo, output);
        output.writeInt(gameId, true);
        output.writeLong(buyIn, true);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        super.read(kryo, input);
        gameId = input.readInt(true);
        buyIn = input.readLong(true);
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
                ", version=" + version +
                ", gameId=" + gameId +
                ", buyIn=" + buyIn +
                '}';
    }
}
