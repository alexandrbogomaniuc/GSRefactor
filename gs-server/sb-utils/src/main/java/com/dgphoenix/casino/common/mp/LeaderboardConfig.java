package com.abs.casino.common.mp;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class LeaderboardConfig implements Serializable, KryoSerializable {
    private static final byte VERSION = 1;

    private long id;
    private String currency;
    private String currencySymbol = "€";
    private long startDate;
    private long endDate;
    private double percent;
    private Map<Integer, LeaderboardAward> awards;
    private List<Long> banks;
    private List<Long> games;
    private List<XPAwardLevel> expAwards;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getCurrencySymbol() {
        return currencySymbol;
    }

    public void setCurrencySymbol(String currencySymbol) {
        this.currencySymbol = currencySymbol;
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

    public double getPercent() {
        return percent;
    }

    public void setPercent(double percent) {
        this.percent = percent;
    }

    public Map<Integer, LeaderboardAward> getAwards() {
        return awards;
    }

    public void setAwards(Map<Integer, LeaderboardAward> awards) {
        this.awards = awards;
    }

    public List<Long> getBanks() {
        return banks;
    }

    public void setBanks(List<Long> banks) {
        this.banks = banks;
    }

    public List<Long> getGames() {
        return games;
    }

    public void setGames(List<Long> games) {
        this.games = games;
    }

    public List<XPAwardLevel> getExpAwards() {
        return expAwards;
    }

    public void setExpAwards(List<XPAwardLevel> expAwards) {
        this.expAwards = expAwards;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        output.writeLong(id, true);
        output.writeString(currency);
        output.writeString(currencySymbol);
        output.writeLong(startDate, true);
        output.writeLong(endDate, true);
        output.writeDouble(percent);
        kryo.writeClassAndObject(output, awards);
        kryo.writeClassAndObject(output, banks);
        kryo.writeClassAndObject(output, games);
        kryo.writeClassAndObject(output, expAwards);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void read(Kryo kryo, Input input) {
        byte version = input.readByte();
        id = input.readLong(true);
        currency = input.readString();
        currencySymbol = version > 0 ? input.readString() : "€";
        startDate = input.readLong(true);
        endDate = input.readLong(true);
        percent = input.readDouble();
        awards = (Map<Integer, LeaderboardAward>) kryo.readClassAndObject(input);
        banks = (List<Long>) kryo.readClassAndObject(input);
        games = (List<Long>) kryo.readClassAndObject(input);
        expAwards = (List<XPAwardLevel>) kryo.readClassAndObject(input);
    }

    @Override
    public String toString() {
        return "LeaderboardConfig{" +
                "id=" + id +
                ", currency=" + currency +
                ", currencySymbol=" + currencySymbol +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", percent=" + percent +
                ", awards=" + awards +
                ", banks=" + banks +
                ", games=" + games +
                ", expAwards=" + expAwards +
                '}';
    }
}
