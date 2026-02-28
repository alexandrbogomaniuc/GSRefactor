package com.abs.casino.promo.tournaments.messages;

import com.abs.casino.common.transport.TInboundObject;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.util.Objects;

public class GetLeaderboard extends TInboundObject {
    private static final byte VERSION = 0;

    private long tournamentId;

    public GetLeaderboard() {}

    public GetLeaderboard(long date, int rid, long tournamentId) {
        super(date, rid);
        this.tournamentId = tournamentId;
    }

    public long getTournamentId() {
        return tournamentId;
    }

    public void setTournamentId(long tournamentId) {
        this.tournamentId = tournamentId;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        super.write(kryo, output);
        output.writeLong(tournamentId, true);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        super.read(kryo, input);
        tournamentId = input.readLong(true);
    }

    @Override
    protected byte getVersion() {
        return VERSION;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        GetLeaderboard that = (GetLeaderboard) o;
        return tournamentId == that.tournamentId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), tournamentId);
    }

    @Override
    public String toString() {
        return "GetLeaderboard{" +
                "tournamentId=" + tournamentId +
                ", date=" + date +
                ", rid=" + rid +
                '}';
    }
}
