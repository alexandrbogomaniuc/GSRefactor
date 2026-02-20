package com.dgphoenix.casino.promo.tournaments.messages;

import com.dgphoenix.casino.common.transport.TObject;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.util.Objects;

public class JoinTournamentResponse extends TObject {
    private static final byte VERSION = 0;

    private long tournamentId;
    private long balance;
    private String link;
    private boolean renamed;
    private String assignedPlayerAlias;

    public JoinTournamentResponse() {}

    public JoinTournamentResponse(long date, int rid, long tournamentId, long balance, String link, boolean renamed,
                                  String assignedPlayerAlias) {
        super(date, rid);
        this.tournamentId = tournamentId;
        this.balance = balance;
        this.link = link;
        this.renamed = renamed;
        this.assignedPlayerAlias = assignedPlayerAlias;
    }

    public long getTournamentId() {
        return tournamentId;
    }

    public void setTournamentId(long tournamentId) {
        this.tournamentId = tournamentId;
    }

    public long getBalance() {
        return balance;
    }

    public void setBalance(long balance) {
        this.balance = balance;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public boolean isRenamed() {
        return renamed;
    }

    public void setRenamed(boolean renamed) {
        this.renamed = renamed;
    }

    public String getAssignedPlayerAlias() {
        return assignedPlayerAlias;
    }

    public void setAssignedPlayerAlias(String assignedPlayerAlias) {
        this.assignedPlayerAlias = assignedPlayerAlias;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        super.write(kryo, output);
        output.writeLong(tournamentId, true);
        output.writeLong(balance, true);
        output.writeString(link);
        output.writeBoolean(renamed);
        output.writeString(assignedPlayerAlias);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        super.read(kryo, input);
        tournamentId = input.readLong(true);
        balance = input.readLong(true);
        link = input.readString();
        renamed = input.readBoolean();
        assignedPlayerAlias = input.readString();
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
        JoinTournamentResponse that = (JoinTournamentResponse) o;
        return tournamentId == that.tournamentId &&
                balance == that.balance &&
                Objects.equals(link, that.link) &&
                renamed == that.renamed &&
                Objects.equals(assignedPlayerAlias, that.assignedPlayerAlias);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), tournamentId, balance, link, renamed, assignedPlayerAlias);
    }

    @Override
    public String toString() {
        return "JointTournamentResponse[" +
                "tournamentId=" + tournamentId +
                ", balance=" + balance +
                ", link='" + link + '\'' +
                ", date=" + date +
                ", rid=" + rid +
                ", renamed=" + renamed +
                ", assignedPlayerAlias=" + assignedPlayerAlias +
                ']';
    }
}
