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

public class EndRoundEvent extends AbstractParticipantEvent<EndRoundEvent> {
    private static final byte VERSION = 3;
    //all amounts in cents
    private long betAmount;
    private long winAmount;

    public EndRoundEvent(long gameId, long eventDate, long accountId, String accountExternalId, long betAmount, long winAmount) {
        super(gameId, eventDate, accountId, accountExternalId);
        this.betAmount = betAmount;
        this.winAmount = winAmount;
    }

    @Override
    public SignificantEventType getType() {
        return SignificantEventType.END_ROUND;
    }

    public long getBetAmount() {
        return betAmount;
    }

    public long getWinAmount() {
        return winAmount;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        baseWrite(kryo, output);
        output.writeLong(betAmount, true);
        output.writeLong(winAmount, true);
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
        if (ver > 2) {
            betAmount = input.readLong(true);
            winAmount = input.readLong(true);
        }
    }

    @Override
    public void serializeObject(JsonGenerator gen, SerializerProvider serializers)
            throws IOException {
        baseSerialize(gen, serializers);
        gen.writeNumberField("betAmount", betAmount);
        gen.writeNumberField("winAmount", winAmount);
    }

    @Override
    public EndRoundEvent deserializeObject(JsonParser p, DeserializationContext ctxt)
            throws IOException {
        JsonNode node = p.getCodec().readTree(p);

        baseDeserialize(p, node, ctxt);
        betAmount = node.get("betAmount").asLong();
        winAmount = node.get("winAmount").asLong();
        return this;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("EndRoundEvent [");
        sb.append("betAmount=").append(betAmount);
        sb.append(", winAmount=").append(winAmount);
        sb.append(", gameId=").append(gameId);
        sb.append(", eventDate=").append(eventDate);
        sb.append(", accountId=").append(accountId);
        sb.append(", roundId=").append(roundId);
        sb.append(']');
        return sb.toString();
    }
}
