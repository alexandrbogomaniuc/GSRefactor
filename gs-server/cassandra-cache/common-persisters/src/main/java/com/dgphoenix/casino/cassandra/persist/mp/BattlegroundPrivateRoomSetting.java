package com.abs.casino.cassandra.persist.mp;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class BattlegroundPrivateRoomSetting implements KryoSerializable {

    private static final byte VERSION = 2;

    private String privateRoomId;
    private String currency;
    private int gameId;
    private long bankId;
    private long buyIn;
    private int serverId;

    public BattlegroundPrivateRoomSetting() {}

    public BattlegroundPrivateRoomSetting(String privateRoomId, String currency, int gameId, long bankId, long buyIn, int serverId) {
        this.privateRoomId = privateRoomId;
        this.currency = currency;
        this.gameId = gameId;
        this.bankId = bankId;
        this.buyIn = buyIn;
        this.serverId = serverId;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        output.writeString(privateRoomId);
        output.writeString(currency);
        output.writeInt(gameId, true);
        output.writeLong(bankId, true);
        output.writeLong(buyIn, true);
        output.writeInt(serverId, true);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        byte version = input.readByte();
        privateRoomId = input.readString();
        currency = input.readString();
        gameId = input.readInt(true);
        bankId = input.readLong(true);
        if (version > 0) {
            buyIn = input.readLong(true);
        }
        if (version > 1) {
            serverId = input.readInt(true);
        }
    }

    public String getPrivateRoomId() {
        return privateRoomId;
    }

    public void setPrivateRoomId(String privateRoomId) {
        this.privateRoomId = privateRoomId;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public int getGameId() {
        return gameId;
    }

    public void setGameId(int gameId) {
        this.gameId = gameId;
    }

    public long getBankId() {
        return bankId;
    }

    public void setBankId(long bankId) {
        this.bankId = bankId;
    }

    public long getBuyIn() {
        return buyIn;
    }

    public void setBuyIn(long buyIn) {
        this.buyIn = buyIn;
    }

    public int getServerId() {
        return serverId;
    }

    public void setServerId(int serverId) {
        this.serverId = serverId;
    }

    @Override
    public String toString() {
        return "BattlegroundPrivateRoomSetting{" +
                "privateRoomId='" + privateRoomId + '\'' +
                ", currency='" + currency + '\'' +
                ", gameId=" + gameId +
                ", bankId=" + bankId +
                ", buyIn=" + buyIn +
                ", serverId=" + serverId +
                '}';
    }
}
