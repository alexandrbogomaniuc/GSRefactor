package com.abs.casino.promo.tournaments.messages;

import com.abs.casino.common.transport.TObject;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.util.List;
import java.util.Objects;

public class Leaderboard extends TObject {
    private static final byte VERSION = 0;

    private long tournamentId;
    private long endDate;
    private String state;
    private int prizePlaces;
    private List<PlaceInfo> leaderboard;
    private PlaceInfo currentPlayer;

    public Leaderboard() {}

    public Leaderboard(long date, int rid, long tournamentId, long endDate, String state, int prizePlaces,
                       List<PlaceInfo> leaderboard, PlaceInfo currentPlayer) {
        super(date, rid);
        this.tournamentId = tournamentId;
        this.endDate = endDate;
        this.state = state;
        this.prizePlaces = prizePlaces;
        this.leaderboard = leaderboard;
        this.currentPlayer = currentPlayer;
    }

    public long getTournamentId() {
        return tournamentId;
    }

    public void setTournamentId(long tournamentId) {
        this.tournamentId = tournamentId;
    }

    public long getEndDate() {
        return endDate;
    }

    public void setEndDate(long endDate) {
        this.endDate = endDate;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public int getPrizePlaces() {
        return prizePlaces;
    }

    public void setPrizePlaces(int prizePlaces) {
        this.prizePlaces = prizePlaces;
    }

    public List<PlaceInfo> getLeaderboard() {
        return leaderboard;
    }

    public void setLeaderboard(List<PlaceInfo> leaderboard) {
        this.leaderboard = leaderboard;
    }

    public PlaceInfo getCurrentPlayer() {
        return currentPlayer;
    }

    public void setCurrentPlayer(PlaceInfo currentPlayer) {
        this.currentPlayer = currentPlayer;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        super.write(kryo, output);
        output.writeLong(tournamentId, true);
        output.writeLong(endDate, true);
        output.writeString(state);
        output.writeInt(prizePlaces, true);
        kryo.writeClassAndObject(output, leaderboard);
        kryo.writeObjectOrNull(output, currentPlayer, PlaceInfo.class);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void read(Kryo kryo, Input input) {
        super.read(kryo, input);
        tournamentId = input.readLong(true);
        endDate = input.readLong(true);
        state = input.readString();
        prizePlaces = input.readInt(true);
        leaderboard = (List<PlaceInfo>) kryo.readClassAndObject(input);
        currentPlayer = kryo.readObjectOrNull(input, PlaceInfo.class);
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
        Leaderboard that = (Leaderboard) o;
        return tournamentId == that.tournamentId &&
                endDate == that.endDate &&
                prizePlaces == that.prizePlaces &&
                Objects.equals(state, that.state) &&
                Objects.equals(leaderboard, that.leaderboard) &&
                Objects.equals(currentPlayer, that.currentPlayer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), tournamentId, endDate, state, prizePlaces, leaderboard, currentPlayer);
    }

    @Override
    public String toString() {
        return "Leaderboard{" +
                "tournamentId=" + tournamentId +
                ", endDate=" + endDate +
                ", state='" + state + '\'' +
                ", prizePlaces=" + prizePlaces +
                ", leaderboard=" + leaderboard +
                ", currentPlayer=" + currentPlayer +
                ", date=" + date +
                ", rid=" + rid +
                '}';
    }
}
