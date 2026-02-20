package com.dgphoenix.casino.promo.tournaments.messages;

import com.dgphoenix.casino.common.transport.TInboundObject;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.util.Objects;

public class GetBattlegroundHistory extends TInboundObject {
    public static final byte VERSION = 1;

    private Integer gameId;
    private Long startDate;
    private Long endDate;

    public GetBattlegroundHistory() {}

    public GetBattlegroundHistory(long date, int rid, Integer gameId, Long startDate, Long endDate) {
        super(date, rid);
        this.gameId = gameId;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public Integer getGameId() {
        return gameId;
    }

    public void setGameId(Integer gameId) {
        this.gameId = gameId;
    }

    public Long getStartDate() {
        return startDate;
    }

    public void setStartDate(Long startDate) {
        this.startDate = startDate;
    }

    public Long getEndDate() {
        return endDate;
    }

    public void setEndDate(Long endDate) {
        this.endDate = endDate;
    }

    @Override
    protected byte getVersion() {
        return VERSION;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        super.write(kryo, output);
        kryo.writeClassAndObject(output, gameId);
        kryo.writeClassAndObject(output, startDate);
        kryo.writeClassAndObject(output, endDate);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        super.read(kryo, input);
        gameId = (Integer) kryo.readClassAndObject(input);
        startDate = (Long) kryo.readClassAndObject(input);
        endDate = (Long) kryo.readClassAndObject(input);

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        GetBattlegroundHistory that = (GetBattlegroundHistory) o;
        return Objects.equals(gameId, that.gameId) && Objects.equals(startDate, that.startDate) && Objects.equals(endDate, that.endDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), gameId, startDate, endDate);
    }

    @Override
    public String toString() {
        return "GetBattlegroundHistory{" +
                "gameId=" + gameId +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", date=" + date +
                ", rid=" + rid +
                '}';
    }
}
