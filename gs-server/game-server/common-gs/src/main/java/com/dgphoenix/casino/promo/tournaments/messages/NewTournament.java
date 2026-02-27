package com.abs.casino.promo.tournaments.messages;

import com.dgphoenix.casino.common.transport.TObject;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.util.Objects;

public class NewTournament extends TObject {
    private static final byte VERSION = 0;

    private ShortTournamentInfo tournament;

    public NewTournament() {}

    public NewTournament(long date, ShortTournamentInfo tournament) {
        super(date, SERVER_RID);
        this.tournament = tournament;
    }

    public ShortTournamentInfo getTournament() {
        return tournament;
    }

    public void setTournament(ShortTournamentInfo tournament) {
        this.tournament = tournament;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        super.write(kryo, output);
        kryo.writeObject(output, tournament);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        super.read(kryo, input);
        tournament = kryo.readObject(input, ShortTournamentInfo.class);
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
        NewTournament that = (NewTournament) o;
        return Objects.equals(tournament, that.tournament);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), tournament);
    }

    @Override
    public String toString() {
        return "NewTournament{" +
                "tournament=" + tournament +
                ", date=" + date +
                ", rid=" + rid +
                '}';
    }
}
