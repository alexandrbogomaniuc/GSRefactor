package com.dgphoenix.casino.promo.tournaments.messages;

import com.dgphoenix.casino.common.transport.TObject;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.util.List;
import java.util.Objects;

public class TournamentDetails extends TObject {
    private static final byte VERSION = 0;

    private long tournamentId;
    private String title;
    private String description;
    private long startDate;
    private long endDate;
    private long buyInPrice;
    private long buyInAmount;
    private long reBuyPrice;
    private long reBuyAmount;
    private boolean reBuyAllowed;
    private int reBuyLimit;
    private long prizePool;
    private String state;
    private List<PrizeInfo> prizes;
    private List<Long> games;

    public TournamentDetails() {}

    public TournamentDetails(long date, int rid, long tournamentId, String title, String description, long startDate,
                             long endDate, long buyInPrice, long buyInAmount, long reBuyPrice, long reBuyAmount,
                             boolean reBuyAllowed, int reBuyLimit, long prizePool, String state,
                             List<PrizeInfo> prizes, List<Long> games) {
        super(date, rid);
        this.tournamentId = tournamentId;
        this.title = title;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.buyInPrice = buyInPrice;
        this.buyInAmount = buyInAmount;
        this.reBuyPrice = reBuyPrice;
        this.reBuyAmount = reBuyAmount;
        this.reBuyAllowed = reBuyAllowed;
        this.reBuyLimit = reBuyLimit;
        this.prizePool = prizePool;
        this.state = state;
        this.prizes = prizes;
        this.games = games;
    }

    public long getTournamentId() {
        return tournamentId;
    }

    public void setTournamentId(long tournamentId) {
        this.tournamentId = tournamentId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public long getBuyInAmount() {
        return buyInAmount;
    }

    public void setBuyInAmount(long buyInAmount) {
        this.buyInAmount = buyInAmount;
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

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public List<PrizeInfo> getPrizes() {
        return prizes;
    }

    public void setPrizes(List<PrizeInfo> prizes) {
        this.prizes = prizes;
    }

    public List<Long> getGames() {
        return games;
    }

    public void setGames(List<Long> games) {
        this.games = games;
    }

    public long getBuyInPrice() {
        return buyInPrice;
    }

    public void setBuyInPrice(long buyInPrice) {
        this.buyInPrice = buyInPrice;
    }

    public long getReBuyPrice() {
        return reBuyPrice;
    }

    public void setReBuyPrice(long reBuyPrice) {
        this.reBuyPrice = reBuyPrice;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        super.write(kryo, output);
        output.writeLong(tournamentId, true);
        output.writeString(title);
        output.writeString(description);
        output.writeLong(startDate, true);
        output.writeLong(endDate, true);
        output.writeLong(buyInPrice, true);
        output.writeLong(buyInAmount, true);
        output.writeLong(reBuyPrice, true);
        output.writeLong(reBuyAmount, true);
        output.writeBoolean(reBuyAllowed);
        output.writeInt(reBuyLimit);
        output.writeLong(prizePool, true);
        output.writeString(state);
        kryo.writeClassAndObject(output, prizes);
        kryo.writeClassAndObject(output, games);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void read(Kryo kryo, Input input) {
        super.read(kryo, input);
        tournamentId = input.readLong(true);
        title = input.readString();
        description = input.readString();
        startDate = input.readLong(true);
        endDate = input.readLong(true);
        buyInPrice = input.readLong(true);
        buyInAmount = input.readLong(true);
        reBuyPrice = input.readLong(true);
        reBuyAmount = input.readLong(true);
        reBuyAllowed = input.readBoolean();
        reBuyLimit = input.readInt();
        prizePool = input.readLong(true);
        state = input.readString();
        prizes = (List<PrizeInfo>) kryo.readClassAndObject(input);
        games = (List<Long>) kryo.readClassAndObject(input);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        TournamentDetails that = (TournamentDetails) o;
        return tournamentId == that.tournamentId &&
                startDate == that.startDate &&
                endDate == that.endDate &&
                buyInAmount == that.buyInAmount &&
                reBuyAmount == that.reBuyAmount &&
                reBuyAllowed == that.reBuyAllowed &&
                prizePool == that.prizePool &&
                Objects.equals(title, that.title) &&
                Objects.equals(description, that.description) &&
                Objects.equals(state, that.state) &&
                Objects.equals(prizes, that.prizes) &&
                Objects.equals(games, that.games);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), tournamentId, title, description, startDate, endDate,
                buyInAmount, reBuyAmount, reBuyAllowed, prizePool, state, prizes, games);
    }

    @Override
    protected byte getVersion() {
        return VERSION;
    }

    @Override
    public String toString() {
        return "TournamentDetails{" +
                "tournamentId=" + tournamentId +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", buyInPrice=" + buyInPrice +
                ", buyInAmount=" + buyInAmount +
                ", reBuyPrice=" + reBuyPrice +
                ", reBuyAmount=" + reBuyAmount +
                ", reBuyAllowed=" + reBuyAllowed +
                ", reBuyLimit=" + reBuyLimit +
                ", prizePool=" + prizePool +
                ", state='" + state + '\'' +
                ", prizes=" + prizes +
                ", games=" + games +
                ", date=" + date +
                ", rid=" + rid +
                '}';
    }
}
