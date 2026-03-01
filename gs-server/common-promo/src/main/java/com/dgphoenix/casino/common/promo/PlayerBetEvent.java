package com.abs.casino.common.promo;

import java.io.IOException;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;

/**
 * User: flsh
 * Date: 23.11.16.
 */
public class PlayerBetEvent extends AbstractParticipantEvent<PlayerBetEvent> {
    private static final byte VERSION = 2;
    //betAmount in cents
    private long betAmount;
    private String currencyCode;

    private PlayerBetEvent() {
    }

    public PlayerBetEvent(long gameId, long eventDate, long accountId, String accountExternalId,
                          long betAmount, String currencyCode) {
        super(gameId, eventDate, accountId, accountExternalId);
        this.betAmount = betAmount;
        this.currencyCode = currencyCode;
    }

    @Override
    public SignificantEventType getType() {
        return SignificantEventType.BET;
    }

    public long getBetAmount() {
        return betAmount;
    }

    public void setBetAmount(long betAmount) {
        this.betAmount = betAmount;
    }

    public String getCurrency() {
        return currencyCode;
    }

    public void setCurrency(String currency) {
        this.currencyCode = currency;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        baseWrite(kryo, output);
        output.writeLong(betAmount, true);
        output.writeString(currencyCode);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        byte ver = input.readByte();
        baseRead(kryo, input);
        if (ver > 0) {
            baseReadV1(kryo, input);
        }
        if (ver > 1) {
            baseReadV2(kryo, input);
        }
        betAmount = input.readLong(true);
        currencyCode = input.readString();
    }

    @Override
    public void serializeObject(JsonGenerator gen, SerializerProvider serializers)
            throws IOException {
        baseSerialize(gen, serializers);

        gen.writeNumberField("betAmount", betAmount);
        gen.writeStringField("currencyCode", currencyCode);
    }

    @Override
    public PlayerBetEvent deserializeObject(JsonParser p, DeserializationContext ctxt)
            throws IOException {
        JsonNode node = p.getCodec().readTree(p);

        baseDeserialize(p, node, ctxt);
        betAmount = node.get("betAmount").longValue();
        currencyCode = readNullableText(node, "currencyCode");
        return this;
    }

    @Override
    public String toString() {
        return "PlayerBetEvent[" +
                "gameId=" + gameId +
                ", eventDate=" + eventDate +
                ", accountId=" + accountId +
                ", accountExternalId='" + accountExternalId + '\'' +
                ", currencyCode=" + currencyCode +
                ", betAmount=" + betAmount +
                ']';
    }
}
