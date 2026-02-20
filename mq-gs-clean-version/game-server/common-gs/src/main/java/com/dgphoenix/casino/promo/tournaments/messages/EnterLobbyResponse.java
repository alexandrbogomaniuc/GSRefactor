package com.dgphoenix.casino.promo.tournaments.messages;

import com.dgphoenix.casino.common.transport.TObject;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.util.List;
import java.util.Objects;
import java.util.Set;

public class EnterLobbyResponse extends TObject {
    private static final byte VERSION = 5;

    private long balance;
    private String currencySymbol;
    private List<ShortTournamentInfo> tournaments;
    private NetworkTournament networkTournament;
    private Set<BattlegroundInfo> battlegrounds;
    private String currencyCode;

    public EnterLobbyResponse() {}

    public EnterLobbyResponse(long date, int rid, long balance, String currencySymbol,
                              List<ShortTournamentInfo> tournaments, NetworkTournament networkTournament,
                              Set<BattlegroundInfo> battlegrounds, String currencyCode) {
        super(date, rid);
        this.balance = balance;
        this.currencySymbol = currencySymbol;
        this.tournaments = tournaments;
        this.networkTournament = networkTournament;
        this.battlegrounds = battlegrounds;
        this.currencyCode = currencyCode;
    }

    public long getBalance() {
        return balance;
    }

    public void setBalance(long balance) {
        this.balance = balance;
    }

    public String getCurrencySymbol() {
        return currencySymbol;
    }

    public void setCurrencySymbol(String currencySymbol) {
        this.currencySymbol = currencySymbol;
    }

    public List<ShortTournamentInfo> getTournaments() {
        return tournaments;
    }

    public void setTournaments(List<ShortTournamentInfo> tournaments) {
        this.tournaments = tournaments;
    }

    public NetworkTournament getNetworkTournamentId() {
        return networkTournament;
    }

    public void setNetworkTournamentId(NetworkTournament networkTournamentId) {
        this.networkTournament = networkTournamentId;
    }

    public Set<BattlegroundInfo> getBattlegrounds() {
        return battlegrounds;
    }

    public void setBattlegrounds(Set<BattlegroundInfo> battlegrounds) {
        this.battlegrounds = battlegrounds;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        super.write(kryo, output);
        output.writeLong(balance, true);
        kryo.writeClassAndObject(output, tournaments);
        output.writeString(currencySymbol);
        kryo.writeClassAndObject(output, networkTournament);
        kryo.writeClassAndObject(output, battlegrounds);
        output.writeString(currencyCode);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void read(Kryo kryo, Input input) {
        super.read(kryo, input);
        balance = input.readLong(true);
        if (version < 2) {
            input.readString();
        }
        tournaments = (List<ShortTournamentInfo>) kryo.readClassAndObject(input);
        currencySymbol = version > 0 ? input.readString() : "";
        if (version >= 3) {
            this.networkTournament = (NetworkTournament) kryo.readClassAndObject(input);
        }
        battlegrounds = (Set<BattlegroundInfo>) kryo.readClassAndObject(input);
        if (version >= 5) {
            currencyCode = input.readString();
        }
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
        EnterLobbyResponse that = (EnterLobbyResponse) o;
        return balance == that.balance &&
                Objects.equals(currencySymbol, that.currencySymbol) &&
                Objects.equals(currencyCode, that.currencyCode) &&
                Objects.equals(tournaments, that.tournaments) &&
                Objects.equals(networkTournament, that.networkTournament);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), balance, currencySymbol, tournaments, networkTournament, battlegrounds, currencyCode);
    }

    @Override
    public String toString() {
        return "EnterLobbyResponse{" +
                "balance=" + balance +
                ", currencySymbol='" + currencySymbol + '\'' +
                ", tournaments=" + tournaments +
                ", networkTournament=" + networkTournament +
                ", battlegrounds=" + battlegrounds +
                ", currencyCode=" + currencyCode +
                '}';
    }
}
