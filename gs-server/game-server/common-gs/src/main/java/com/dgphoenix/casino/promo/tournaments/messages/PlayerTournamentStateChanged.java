package com.dgphoenix.casino.promo.tournaments.messages;

import com.dgphoenix.casino.common.transport.TObject;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.util.Objects;

public class PlayerTournamentStateChanged extends TObject {
    private static final byte VERSION = 0;

    private long tournamentId;
    private boolean cannotJoin;
    private boolean joined;

    public PlayerTournamentStateChanged() {}

    public PlayerTournamentStateChanged(long tournamentId, boolean cannotJoin, boolean joined, long date, int rid) {
        super(date, rid);
        this.tournamentId = tournamentId;
        this.cannotJoin = cannotJoin;
        this.joined = joined;
    }

    public PlayerTournamentStateChanged(long tournamentId, boolean cannotJoin, boolean joined) {
        super(System.currentTimeMillis(), -1);
        this.tournamentId = tournamentId;
        this.cannotJoin = cannotJoin;
        this.joined = joined;
    }

    public long getTournamentId() {
        return tournamentId;
    }

    public void setTournamentId(long tournamentId) {
        this.tournamentId = tournamentId;
    }

    public boolean isCannotJoin() {
        return cannotJoin;
    }

    public void setCannotJoin(boolean cannotJoin) {
        this.cannotJoin = cannotJoin;
    }

    public boolean isJoined() {
        return joined;
    }

    public void setJoined(boolean joined) {
        this.joined = joined;
    }

    @Override
    public byte getVersion() {
        return VERSION;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        super.write(kryo, output);
        output.writeLong(tournamentId, true);
        output.writeBoolean(cannotJoin);
        output.writeBoolean(joined);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        super.read(kryo, input);
        tournamentId = input.readLong(true);
        cannotJoin = input.readBoolean();
        joined = input.readBoolean();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        PlayerTournamentStateChanged that = (PlayerTournamentStateChanged) o;
        return tournamentId == that.tournamentId &&
                cannotJoin == that.cannotJoin &&
                joined == that.joined;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), tournamentId, cannotJoin, joined);
    }

    @Override
    public String toString() {
        return "PlayerTournamentStateChanged[" +
                "tournamentId=" + tournamentId +
                ", cannotJoin=" + cannotJoin +
                ", joined=" + joined +
                ", date=" + date +
                ", rid=" + rid +
                ']';
    }
}
