package com.abs.casino.common.cache.data.game;

import com.abs.casino.common.cache.IDistributedCacheEntry;
import com.abs.casino.common.cache.data.currency.Currency;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class GameSessionInfo implements IDistributedCacheEntry, KryoSerializable {
    private static final byte VERSION = 2;
    private long id;
    private long bankId;
    private Currency currency;
    private String nickName;
    private String extGameId;
    private String gameName;
    private long gameRevenue;
    private long endTime;
    private String currencyFraction;

    public GameSessionInfo() {
    }

    public GameSessionInfo(long id, long bankId, Currency currency, String currencyFraction,
                           String nickName, String extGameId, String gameName, long gameRevenue, long endTime) {
        this.id = id;
        this.bankId = bankId;
        this.nickName = nickName;
        this.extGameId = extGameId;
        this.gameName = gameName;
        this.gameRevenue = gameRevenue;
        this.endTime = endTime;
        this.currency = currency;
        this.currencyFraction = currencyFraction;
    }

    public long getId() {
        return id;
    }

    public synchronized void setId(long id) {
        this.id = id;
    }

    public long getBankId() {
        return bankId;
    }

    public synchronized void setBankId(long bankId) {
        this.bankId = bankId;
    }

    public Currency getCurrency() {
        return currency;
    }

    public synchronized void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public String getCurrencyFraction() {
        return currencyFraction;
    }

    public void setCurrencyFraction(String currencyFraction) {
        this.currencyFraction = currencyFraction;
    }

    public String getNickName() {
        return nickName;
    }

    public synchronized void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getExtGameId() {
        return extGameId;
    }

    public synchronized void setExtGameId(String extGameId) {
        this.extGameId = extGameId;
    }

    public String getGameName() {
        return gameName;
    }

    public synchronized void setGameName(String gameName) {
        this.gameName = gameName;
    }

    public long getGameRevenue() {
        return gameRevenue;
    }

    public synchronized void setGameRevenue(long gameRevenue) {
        this.gameRevenue = gameRevenue;
    }

    public long getEndTime() {
        return endTime;
    }

    public synchronized void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GameSessionInfo that = (GameSessionInfo) o;

        return id == that.id;

    }

    @Override
    public int hashCode() {
        return (int) (id ^ id >>> 32);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("GameSessionInfo");
        sb.append("[id=").append(id);
        sb.append(", bankId=").append(bankId);
        sb.append(", currency=").append(currency);
        sb.append(", currencyFraction=").append(currencyFraction);
        sb.append(", nickName='").append(nickName).append('\'');
        sb.append(", extGameId=").append(extGameId);
        sb.append(", gameName=").append(gameName);
        sb.append(", gameRevenue=").append(gameRevenue);
        sb.append(", endTime=").append(endTime);
        sb.append(']');
        return sb.toString();
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        output.writeLong(id);
        output.writeLong(bankId);
        kryo.writeObjectOrNull(output, currency, Currency.class);
        output.writeString(nickName);
        output.writeString(extGameId);
        output.writeString(gameName);
        output.writeLong(gameRevenue);
        output.writeLong(endTime);
        output.writeString(currencyFraction);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        byte ver = input.readByte();
        id = input.readLong();
        bankId = input.readLong();
        currency = kryo.readObjectOrNull(input, Currency.class, Currency.SERIALIZER);
        nickName = input.readString();
        if (ver > 0) {
            extGameId = input.readString();
        }
        gameName = input.readString();
        gameRevenue = input.readLong();
        endTime = input.readLong();
        if (ver > 1) {
            currencyFraction = input.readString();
        }
    }
}
