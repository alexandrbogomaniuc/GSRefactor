package com.abs.casino.promo.tournaments.messages;

import com.abs.casino.common.transport.TInboundObject;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.util.List;
import java.util.Objects;

public class GetTournamentDetails extends TInboundObject {
    private static final byte VERSION = 0;

    private long tournamentId;
    private List<Long> networkEventIds;

    public GetTournamentDetails() {}

    public GetTournamentDetails(long date, int rid, long tournamentId, List<Long> networkEventIds) {
        super(date, rid);
        this.tournamentId = tournamentId;
        this.networkEventIds = networkEventIds;
    }

    public long getTournamentId() {
        return tournamentId;
    }

    public void setTournamentId(long tournamentId) {
        this.tournamentId = tournamentId;
    }

    public List<Long> getNetworkEventIds() {
        return networkEventIds;
    }

    public void setNetworkEventIds(List<Long> networkEventIds) {
        this.networkEventIds = networkEventIds;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        super.write(kryo, output);
        output.writeLong(tournamentId, true);
        kryo.writeClassAndObject(output, networkEventIds);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void read(Kryo kryo, Input input) {
        super.read(kryo, input);
        tournamentId = input.readLong(true);
        networkEventIds = (List<Long>) kryo.readClassAndObject(input);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        GetTournamentDetails that = (GetTournamentDetails) o;
        return tournamentId == that.tournamentId &&
                Objects.equals(networkEventIds, that.networkEventIds);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), tournamentId, networkEventIds);
    }

    @Override
    protected byte getVersion() {
        return VERSION;
    }

    @Override
    public String toString() {
        return "GetTournamentDetails{" +
                "tournamentId=" + tournamentId +
                "networkEventIds=" + networkEventIds +
                ", date=" + date +
                ", rid=" + rid +
                '}';
    }
}
