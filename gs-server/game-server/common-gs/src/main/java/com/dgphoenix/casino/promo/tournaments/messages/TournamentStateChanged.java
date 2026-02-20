package com.dgphoenix.casino.promo.tournaments.messages;

import com.dgphoenix.casino.common.transport.TObject;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.util.Objects;

public class TournamentStateChanged extends TObject {
    private static final byte VERSION = 0;

    private long tournamentId;
    private String state;

    public TournamentStateChanged() {}

    public TournamentStateChanged(long date, long tournamentId, String state) {
        super(date, SERVER_RID);
        this.tournamentId = tournamentId;
        this.state = state;
    }

    public long getTournamentId() {
        return tournamentId;
    }

    public void setTournamentId(long tournamentId) {
        this.tournamentId = tournamentId;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        super.write(kryo, output);
        output.writeLong(tournamentId, true);
        output.writeString(state);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        super.read(kryo, input);
        tournamentId = input.readLong(true);
        state = input.readString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        TournamentStateChanged that = (TournamentStateChanged) o;
        return tournamentId == that.tournamentId &&
                Objects.equals(state, that.state);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), tournamentId, state);
    }

    @Override
    protected byte getVersion() {
        return VERSION;
    }

    @Override
    public String toString() {
        return "TournamentStateChanged{" +
                "tournamentId=" + tournamentId +
                ", state='" + state + '\'' +
                ", date=" + date +
                ", rid=" + rid +
                '}';
    }
}
