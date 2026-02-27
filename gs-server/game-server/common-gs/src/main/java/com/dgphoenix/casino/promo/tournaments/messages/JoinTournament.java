package com.abs.casino.promo.tournaments.messages;

import com.dgphoenix.casino.common.transport.TInboundObject;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.util.Objects;

public class JoinTournament extends TInboundObject {
    private static final byte VERSION = 0;

    private long tournamentId;
    private long gameId;
    private String domainName;
    private String realModeUrl;
    private String playerAlias;
    private Long networkTournamentId;

    public JoinTournament() {}

    public JoinTournament(long date, int rid, long tournamentId, long gameId, String domainName, String realModeUrl,
                          String playerAlias, Long networkTournamentId) {
        super(date, rid);
        this.tournamentId = tournamentId;
        this.gameId = gameId;
        this.domainName = domainName;
        this.realModeUrl = realModeUrl;
        this.playerAlias = playerAlias;
        this.networkTournamentId = networkTournamentId;
    }

    public long getTournamentId() {
        return tournamentId;
    }

    public void setTournamentId(long tournamentId) {
        this.tournamentId = tournamentId;
    }

    public long getGameId() {
        return gameId;
    }

    public void setGameId(long gameId) {
        this.gameId = gameId;
    }

    public String getDomainName() {
        return domainName;
    }

    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }

    public String getRealModeUrl() {
        return realModeUrl;
    }

    public void setRealModeUrl(String realModeUrl) {
        this.realModeUrl = realModeUrl;
    }

    public String getPlayerAlias() {
        return playerAlias;
    }

    public void setPlayerAlias(String playerAlias) {
        this.playerAlias = playerAlias;
    }

    public Long getNetworkTournamentId() {
        return networkTournamentId;
    }

    public void setNetworkTournamentId(Long networkTournamentId) {
        this.networkTournamentId = networkTournamentId;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        super.write(kryo, output);
        output.writeLong(tournamentId, true);
        output.writeLong(gameId, true);
        output.writeString(domainName);
        output.writeString(realModeUrl);
        output.writeString(playerAlias);
        output.writeLong(networkTournamentId, true);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        super.read(kryo, input);
        tournamentId = input.readLong(true);
        gameId = input.readLong(true);
        domainName = input.readString();
        realModeUrl = input.readString();
        playerAlias = input.readString();
        networkTournamentId = input.readLong(true);
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
        JoinTournament that = (JoinTournament) o;
        return tournamentId == that.tournamentId &&
                gameId == that.gameId &&
                domainName.equals(that.domainName) &&
                realModeUrl.equals(that.realModeUrl) &&
                playerAlias.equals(that.playerAlias) &&
                Objects.equals(networkTournamentId, that.networkTournamentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), tournamentId, gameId, domainName, realModeUrl, playerAlias, networkTournamentId);
    }

    @Override
    public String toString() {
        return "JoinTournament{" +
                "tournamentId=" + tournamentId +
                ", gameId=" + gameId +
                ", domainName=" + domainName +
                ", date=" + date +
                ", rid=" + rid +
                ", realModeUrl=" + realModeUrl +
                ", playerAlias=" + playerAlias +
                ", networkTournamentId=" + networkTournamentId +
                '}';
    }
}
