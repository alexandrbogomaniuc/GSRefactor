package com.abs.casino.promo.tournaments.messages;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.util.List;
import java.util.Objects;

public class NetworkTournament extends ShortTournamentInfo {
    private static final byte VERSION = 0;

    private List<Long> events;
    private String tournamentRules;
    private String prizeAllocation;
    private String howToWin;

    public NetworkTournament() {}

    public NetworkTournament(long id, String title, long startDate, long endDate, long buyInPrice, long buyInAmount,
                             long reBuyPrice, long reBuyAmount, boolean reBuyAllowed, int reBuyLimit, long prizePool,
                             boolean joined, String state, String icon, List<Long> games, boolean cannotJoin,
                             List<Long> events, String tournamentRules, String prizeAllocation, String howToWin) {
        super(id, title, startDate, endDate, buyInPrice, buyInAmount, reBuyPrice, reBuyAmount, reBuyAllowed, reBuyLimit,
                prizePool, joined, state, icon, games, cannotJoin, id);
        this.events = events;
        this.tournamentRules = tournamentRules;
        this.prizeAllocation = prizeAllocation;
        this.howToWin = howToWin;
    }

    public List<Long> getEvents() {
        return events;
    }

    public void setEvents(List<Long> events) {
        this.events = events;
    }

    public String getTournamentRules() {
        return tournamentRules;
    }

    public void setTournamentRules(String tournamentRules) {
        this.tournamentRules = tournamentRules;
    }

    public String getPrizeAllocation() {
        return prizeAllocation;
    }

    public void setPrizeAllocation(String prizeAllocation) {
        this.prizeAllocation = prizeAllocation;
    }

    public String getHowToWin() {
        return howToWin;
    }

    public void setHowToWin(String howToWin) {
        this.howToWin = howToWin;
    }

    @Override
    public byte getVersion() {
        return VERSION;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        super.write(kryo, output);
        kryo.writeClassAndObject(output, events);
        output.writeString(tournamentRules);
        output.writeString(prizeAllocation);
        output.writeString(howToWin);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        super.read(kryo, input);
        events = (List<Long>) kryo.readClassAndObject(input);
        tournamentRules = input.readString();
        prizeAllocation = input.readString();
        howToWin = input.readString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        NetworkTournament that = (NetworkTournament) o;
        return Objects.equals(prizeAllocation, that.prizeAllocation) &&
                events.equals(that.events) &&
                tournamentRules.equals(that.tournamentRules) &&
                howToWin.equals(that.howToWin);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), events, tournamentRules, prizeAllocation, howToWin);
    }

    @Override
    public String toString() {
        return "NetworkTournament[" +
                "id=" + getId() +
                ", title='" + getTitle() + '\'' +
                ", startDate=" + getStartDate() +
                ", endDate=" + getEndDate() +
                ", buyInPrice=" + getBuyInPrice() +
                ", buyInAmount=" + getBuyInAmount() +
                ", reBuyPrice=" + getReBuyPrice() +
                ", reBuyAmount=" + getReBuyAmount() +
                ", reBuyAllowed=" + isReBuyAllowed() +
                ", reBuyLimit=" + getReBuyLimit() +
                ", prizePool=" + getPrizePool() +
                ", joined=" + isJoined() +
                ", state='" + getState() + '\'' +
                ", icon='" + getIcon() + '\'' +
                ", games=" + getGames() +
                ", cannotJoin=" + isCannotJoin() +
                ", events=" + events +
                ", tournamentRules=" + tournamentRules +
                ", prizeAllocation=" + prizeAllocation +
                ", howToWin=" + howToWin +
                ']';
    }
}
