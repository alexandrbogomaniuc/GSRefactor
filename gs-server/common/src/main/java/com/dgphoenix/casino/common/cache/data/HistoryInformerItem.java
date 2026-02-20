/*
 * HistoryInformerItem
 *
 * Author: Stepan Vvedenskiy
 * Date: 11.01.2018
 */

package com.dgphoenix.casino.common.cache.data;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class HistoryInformerItem implements KryoSerializable {
    private static final int VERSION = 0;

    private long gameSessionId;
    private long accountId;
    private String externalAccountId;
    private long bankId;
    private long gameId;
    private long createTime = System.currentTimeMillis();
    private String data;

    private transient int iterations; //stores in column

    public HistoryInformerItem() {}

    public HistoryInformerItem(long gameSessionId, long accountId, String externalAccountId,
                               long bankId, long gameId, String data) {
        this.gameSessionId = gameSessionId;
        this.accountId = accountId;
        this.externalAccountId = externalAccountId;
        this.bankId = bankId;
        this.gameId = gameId;
        this.data = data;
    }

    public int getIterations() {
        return iterations;
    }

    public void setIterations(int value) {
        iterations = value;
    }

    public long getGameSessionId() {
        return gameSessionId;
    }

    public void setGameSessionId(long gameSessionId) {
        this.gameSessionId = gameSessionId;
    }

    public long getAccountId() {
        return accountId;
    }

    public void setAccountId(long accountId) {
        this.accountId = accountId;
    }

    public String getExternalAccountId() {
        return externalAccountId;
    }

    public void setExternalAccountId(String externalAccountId) {
        this.externalAccountId = externalAccountId;
    }

    public long getBankId() {
        return bankId;
    }

    public void setBankId(long bankId) {
        this.bankId = bankId;
    }

    public long getGameId() {
        return gameId;
    }

    public void setGameId(long gameId) {
        this.gameId = gameId;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeInt(VERSION);
        output.writeLong(gameSessionId);
        output.writeLong(accountId);
        output.writeString(externalAccountId);
        output.writeLong(bankId);
        output.writeLong(gameId);
        output.writeLong(createTime);
        output.writeString(data);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        int version = input.readInt();
        gameSessionId = input.readLong();
        accountId = input.readLong();
        externalAccountId = input.readString();
        bankId = input.readLong();
        gameId = input.readLong();
        createTime = input.readLong();
        data = input.readString();
    }

    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
        return "HistoryInformerItem[" +
                "gameSessionId=" + gameSessionId +
                ", accountId=" + accountId +
                ", externalAccountId='" + externalAccountId + '\'' +
                ", bankId=" + bankId +
                ", gameId=" + gameId +
                ", createTime=" + formatter.format(LocalDateTime.ofInstant(Instant.ofEpochMilli(createTime), ZoneId.systemDefault())) +
                ", iterations=" + iterations +
                ", dataLength=" + (data == null ? 0 : data.length()) +
                ", data='" + (data == null || data.length() < 200 ? data : data.substring(0, 200) + "...") + '\'' +
                ']';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        HistoryInformerItem that = (HistoryInformerItem) o;

        if (gameSessionId != that.gameSessionId) return false;
        if (accountId != that.accountId) return false;
        if (bankId != that.bankId) return false;
        if (gameId != that.gameId) return false;
        if (createTime != that.createTime) return false;
        if (!externalAccountId.equals(that.externalAccountId)) return false;
        return data.equals(that.data);
    }

    @Override
    public int hashCode() {
        int result = (int) (gameSessionId ^ (gameSessionId >>> 32));
        result = 31 * result + (int) (accountId ^ (accountId >>> 32));
        result = 31 * result + externalAccountId.hashCode();
        result = 31 * result + (int) (bankId ^ (bankId >>> 32));
        result = 31 * result + (int) (gameId ^ (gameId >>> 32));
        result = 31 * result + (int) (createTime ^ (createTime >>> 32));
        result = 31 * result + data.hashCode();
        return result;
    }
}
