package com.dgphoenix.casino.common.promo;

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
public class PlayerWinEvent extends AbstractParticipantEvent<PlayerWinEvent> {
    private static final byte VERSION = 2;
    //winAmount in cents
    private Long winAmount;
    private String currencyCode;

    private PlayerWinEvent() {
    }

    public PlayerWinEvent(long gameId, long eventDate, long accountId, String accountExternalId,
                          long winAmount, String currencyCode) {
        super(gameId, eventDate, accountId, accountExternalId);
        this.winAmount = winAmount > 0 ? winAmount : null;
        this.currencyCode = currencyCode;
    }

    @Override
    public SignificantEventType getType() {
        return SignificantEventType.WIN;
    }

    public Long getWinAmount() {
        return winAmount;
    }

    public String getCurrency() {
        return currencyCode;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        baseWrite(kryo, output);
        output.writeLong(winAmount, true);
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
        winAmount = input.readLong(true);
        currencyCode = input.readString();
    }

    @Override
    public void serializeObject(JsonGenerator gen, SerializerProvider serializers)
            throws IOException {
        baseSerialize(gen, serializers);

        gen.writeNumberField("winAmount", winAmount);
        gen.writeStringField("currencyCode", currencyCode);
    }

    @Override
    public PlayerWinEvent deserializeObject(JsonParser p, DeserializationContext ctxt)
            throws IOException {
        JsonNode node = p.getCodec().readTree(p);

        baseDeserialize(p, node, ctxt);
        winAmount = node.get("winAmount").longValue();
        currencyCode = readNullableText(node, "currencyCode");
        return this;
    }

    @Override
    public String toString() {
        return "PlayerWinEvent[" +
                "gameId=" + gameId +
                ", eventDate=" + eventDate +
                ", accountId=" + accountId +
                ", accountExternalId='" + accountExternalId + '\'' +
                ", currencyCode=" + currencyCode +
                ", winAmount=" + winAmount +
                ']';
    }
}
