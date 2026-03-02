package com.abs.casino.promo.tournaments.messages;

import com.abs.casino.cassandra.persist.mp.BattlegroundRound;
import com.abs.casino.common.transport.TObject;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.util.List;
import java.util.Objects;

public class GetBattlegroundHistoryResponse extends TObject {
    private static final byte VERSION = 1;
    private List<BattlegroundRound> rounds;

    public GetBattlegroundHistoryResponse() {}

    public GetBattlegroundHistoryResponse(long date, int rid, List<BattlegroundRound> rounds) {
        super(date, rid);
        this.rounds = rounds;
    }

    @Override
    protected byte getVersion() {
        return VERSION;
    }

    public List<BattlegroundRound> getRounds() {
        return rounds;
    }

    public void setRounds(List<BattlegroundRound> rounds) {
        this.rounds = rounds;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        super.write(kryo, output);
        kryo.writeClassAndObject(output, rounds);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void read(Kryo kryo, Input input) {
        super.read(kryo, input);
        rounds = (List<BattlegroundRound>) kryo.readClassAndObject(input);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        GetBattlegroundHistoryResponse that = (GetBattlegroundHistoryResponse) o;
        return Objects.equals(rounds, that.rounds);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), rounds);
    }

    @Override
    public String toString() {
        return "GetBattlegroundHistoryResponse{" +
                "rounds=" + rounds +
                ", date=" + date +
                ", rid=" + rid +
                '}';
    }
}
