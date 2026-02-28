package com.betsoft.casino.mp.model.bots;

import java.io.IOException;

import com.abs.casino.common.cache.JsonSelfSerializable;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;

/**
 * User: flsh
 * Date: 14.07.2022.
 */
public class ActiveBot implements KryoSerializable, JsonSelfSerializable<ActiveBot> {
    private static final byte VERSION = 4;

    private long botId;
    private long accountId;
    private long roomId;
    private String sessionId;
    private long gameId;
    private long dateTime;
    private long expiresAt;
    private String nickname;
    private long bankId;
    private long buyIn;

    public ActiveBot(long botId, long roomId, long gameId, long accountId, String sessionId,
                     long expiresAt, String nickname, long bankId, long buyIn) {
        this.botId = botId;
        this.roomId = roomId;
        this.gameId = gameId;
        this.accountId = accountId;
        this.sessionId = sessionId;
        this.dateTime = System.currentTimeMillis();
        this.expiresAt = expiresAt;
        this.nickname = nickname;
        this.bankId = bankId;
        this.buyIn = buyIn;
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

    public long getBotId() {
        return botId;
    }

    public void setBotId(long botId) {
        this.botId = botId;
    }

    public long getAccountId() {
        return accountId;
    }

    public void setAccountId(long accountId) {
        this.accountId = accountId;
    }

    public long getRoomId() {
        return roomId;
    }

    public void setRoomId(long roomId) {
        this.roomId = roomId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public long getGameId() {
        return gameId;
    }

    public void setGameId(long gameId) {
        this.gameId = gameId;
    }

    public long getDateTime() {
        return dateTime;
    }

    public void setDateTime(long dateTime) {
        this.dateTime = dateTime;
    }

    public long getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(long expiresAt) {
        this.expiresAt = expiresAt;
    }

    public boolean expired() {
        return this.expiresAt > 0 && this.expiresAt <= System.currentTimeMillis();
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        output.writeLong(botId, true);
        output.writeLong(accountId, true);
        output.writeLong(roomId, true);
        output.writeString(sessionId);
        output.writeLong(gameId, true);
        output.writeLong(dateTime, true);
        output.writeLong(expiresAt, true);
        output.writeString(nickname);
        output.writeLong(bankId, true);
        output.writeLong(buyIn, true);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        byte version = input.readByte();
        botId = input.readLong(true);
        accountId = input.readLong(true);
        roomId = input.readLong(true);
        sessionId = input.readString();

        if(version >= 1) {
            //noinspection unchecked
            gameId = input.readLong(true);
            dateTime = input.readLong(true);
        }

        if(version >= 2) {
            //noinspection unchecked
            expiresAt = input.readLong(true);
        }

        if(version >= 3) {
            nickname = input.readString();
        }

        if(version >= 4) {
            //noinspection unchecked
            bankId = input.readLong(true);
            buyIn = input.readLong(true);
        }
    }

    @Override
    public void serializeObject(JsonGenerator gen, SerializerProvider serializers) throws IOException {
        

        gen.writeNumberField("botId", botId);
        gen.writeNumberField("accountId", accountId);
        gen.writeNumberField("roomId", roomId);
        gen.writeStringField("sessionId", sessionId);
        gen.writeNumberField("gameId", gameId);
        gen.writeNumberField("dateTime", dateTime);
        gen.writeNumberField("expiresAt", expiresAt);
        gen.writeStringField("nickname", nickname);
        gen.writeNumberField("bankId", bankId);
        gen.writeNumberField("buyIn", buyIn);


    }

    @Override
    public ActiveBot deserializeObject(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode n = p.getCodec().readTree(p);

        botId = n.get("botId").longValue();
        accountId = n.get("accountId").longValue();
        roomId = n.get("roomId").longValue();
        sessionId = readNullableText(n, "sessionId");

        gameId = n.get("gameId").asLong();
        dateTime = n.get("dateTime").longValue();

        expiresAt = n.get("expiresAt").asLong();

        nickname = readNullableText(n, "nickname");

        bankId = n.get("bankId").longValue();
        buyIn = n.get("buyIn").longValue();

        return this;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ActiveBot [");
        sb.append("botId=").append(botId);
        sb.append(", accountId=").append(accountId);
        sb.append(", roomId=").append(roomId);
        sb.append(", sessionId='").append(sessionId).append('\'');
        sb.append(", gameId=").append(gameId);
        sb.append(", dateTime=").append(dateTime);
        sb.append(", expiresAt=").append(expiresAt);
        sb.append(", nickname='").append(nickname).append('\'');
        sb.append(", bankId=").append(bankId);
        sb.append(", buyIn=").append(buyIn);
        sb.append(']');
        return sb.toString();
    }
}
