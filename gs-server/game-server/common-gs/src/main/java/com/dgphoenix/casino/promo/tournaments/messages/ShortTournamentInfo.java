package com.dgphoenix.casino.promo.tournaments.messages;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

public class ShortTournamentInfo implements KryoSerializable, Serializable {
    private static final byte VERSION = 0;

    private long id;
    private String title;
    private long startDate;
    private long endDate;
    private long buyInPrice;
    private long buyInAmount;
    private long reBuyPrice;
    private long reBuyAmount;
    private boolean reBuyAllowed;
    private int reBuyLimit;
    private long prizePool;
    private boolean joined;
    private String state;
    private String icon;
    private List<Long> games;
    private boolean cannotJoin;
    private Long networkTournamentId;

    public ShortTournamentInfo() {}

    public ShortTournamentInfo(long id, String title, long startDate, long endDate, long buyInPrice, long buyInAmount,
                               long reBuyPrice, long reBuyAmount, boolean reBuyAllowed, int reBuyLimit, long prizePool,
                               boolean joined, String state, String icon, List<Long> games, boolean cannotJoin,
                               Long networkTournamentId) {
        this.id = id;
        this.title = title;
        this.startDate = startDate;
        this.endDate = endDate;
        this.buyInPrice = buyInPrice;
        this.buyInAmount = buyInAmount;
        this.reBuyPrice = reBuyPrice;
        this.reBuyAmount = reBuyAmount;
        this.reBuyAllowed = reBuyAllowed;
        this.reBuyLimit = reBuyLimit;
        this.prizePool = prizePool;
        this.joined = joined;
        this.state = state;
        this.icon = icon;
        this.games = games;
        this.cannotJoin = cannotJoin;
        this.networkTournamentId = networkTournamentId;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getStartDate() {
        return startDate;
    }

    public void setStartDate(long startDate) {
        this.startDate = startDate;
    }

    public long getEndDate() {
        return endDate;
    }

    public void setEndDate(long endDate) {
        this.endDate = endDate;
    }

    public long getBuyInPrice() {
        return buyInPrice;
    }

    public void setBuyInPrice(long buyInPrice) {
        this.buyInPrice = buyInPrice;
    }

    public long getBuyInAmount() {
        return buyInAmount;
    }

    public void setBuyInAmount(long buyInAmount) {
        this.buyInAmount = buyInAmount;
    }

    public long getReBuyPrice() {
        return reBuyPrice;
    }

    public void setReBuyPrice(long reBuyPrice) {
        this.reBuyPrice = reBuyPrice;
    }

    public long getReBuyAmount() {
        return reBuyAmount;
    }

    public void setReBuyAmount(long reBuyAmount) {
        this.reBuyAmount = reBuyAmount;
    }

    public boolean isReBuyAllowed() {
        return reBuyAllowed;
    }

    public void setReBuyAllowed(boolean reBuyAllowed) {
        this.reBuyAllowed = reBuyAllowed;
    }

    public int getReBuyLimit() {
        return reBuyLimit;
    }

    public void setReBuyLimit(int reBuyLimit) {
        this.reBuyLimit = reBuyLimit;
    }

    public long getPrizePool() {
        return prizePool;
    }

    public void setPrizePool(long prizePool) {
        this.prizePool = prizePool;
    }

    public boolean isJoined() {
        return joined;
    }

    public void setJoined(boolean joined) {
        this.joined = joined;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public List<Long> getGames() {
        return games;
    }

    public void setGames(List<Long> games) {
        this.games = games;
    }

    public boolean isCannotJoin() {
        return cannotJoin;
    }

    public void setCannotJoin(boolean cannotJoin) {
        this.cannotJoin = cannotJoin;
    }

    public long getNetworkTournamentId() {
        return networkTournamentId;
    }

    public void setNetworkTournamentId(long networkTournamentId) {
        this.networkTournamentId = networkTournamentId;
    }

    public byte getVersion() {
        return VERSION;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(getVersion());
        output.writeLong(id, true);
        output.writeString(title);
        output.writeLong(startDate, true);
        output.writeLong(endDate, true);
        output.writeLong(buyInPrice, true);
        output.writeLong(buyInAmount, true);
        output.writeLong(reBuyPrice, true);
        output.writeLong(reBuyAmount, true);
        output.writeBoolean(reBuyAllowed);
        output.writeInt(reBuyLimit);
        output.writeLong(prizePool, true);
        output.writeBoolean(joined);
        output.writeString(state);
        output.writeString(icon);
        kryo.writeClassAndObject(output, games);
        output.writeBoolean(cannotJoin);
        kryo.writeClassAndObject(output, networkTournamentId);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void read(Kryo kryo, Input input) {
        input.readByte();
        id = input.readLong(true);
        title = input.readString();
        startDate = input.readLong(true);
        endDate = input.readLong(true);
        buyInPrice = input.readLong(true);
        buyInAmount = input.readLong(true);
        reBuyPrice = input.readLong(true);
        reBuyAmount = input.readLong(true);
        reBuyAllowed = input.readBoolean();
        reBuyLimit = input.readInt();
        prizePool = input.readLong(true);
        joined = input.readBoolean();
        state = input.readString();
        icon = input.readString();
        games = (List<Long>) kryo.readClassAndObject(input);
        cannotJoin = input.readBoolean();
        networkTournamentId = (Long) kryo.readClassAndObject(input);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ShortTournamentInfo that = (ShortTournamentInfo) o;
        return id == that.id &&
                startDate == that.startDate &&
                endDate == that.endDate &&
                buyInPrice == that.buyInPrice &&
                buyInAmount == that.buyInAmount &&
                reBuyPrice == that.reBuyPrice &&
                reBuyAmount == that.reBuyAmount &&
                reBuyAllowed == that.reBuyAllowed &&
                prizePool == that.prizePool &&
                joined == that.joined &&
                Objects.equals(title, that.title) &&
                Objects.equals(state, that.state) &&
                Objects.equals(icon, that.icon) &&
                Objects.equals(games, that.games) &&
                cannotJoin == that.cannotJoin &&
                Objects.equals(networkTournamentId, that.networkTournamentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, startDate, endDate, buyInPrice, buyInAmount, reBuyPrice, reBuyAmount,
                reBuyAllowed, prizePool, joined, state, icon, games, cannotJoin, networkTournamentId);
    }

    @Override
    public String toString() {
        return "ShortTournamentInfo{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", buyInPrice=" + buyInPrice +
                ", buyInAmount=" + buyInAmount +
                ", reBuyPrice=" + reBuyPrice +
                ", reBuyAmount=" + reBuyAmount +
                ", reBuyAllowed=" + reBuyAllowed +
                ", reBuyLimit=" + reBuyLimit +
                ", prizePool=" + prizePool +
                ", joined=" + joined +
                ", state='" + state + '\'' +
                ", icon='" + icon + '\'' +
                ", games=" + games +
                ", cannotJoin=" + cannotJoin +
                ", networkTournamentId=" + networkTournamentId +
                '}';
    }
}
