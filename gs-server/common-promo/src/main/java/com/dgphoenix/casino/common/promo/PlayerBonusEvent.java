package com.abs.casino.common.promo;

import java.io.IOException;

import com.abs.casino.gamecombos.ReelDetails;
import com.abs.casino.common.promo.AbstractParticipantEvent;
import com.abs.casino.common.promo.SignificantEventType;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;

/**
 * User: flsh
 * Date: 23.11.16.
 */
public class PlayerBonusEvent extends AbstractParticipantEvent<PlayerBonusEvent> {
    private static final byte VERSION = 2;
    private ReelDetails reelDetails;

    private PlayerBonusEvent() {
    }

    public PlayerBonusEvent(long gameId, long eventDate, long accountId, String accountExternalId,
                            ReelDetails reelDetails) {
        super(gameId, eventDate, accountId, accountExternalId);
        this.reelDetails = reelDetails;
    }

    @Override
    public SignificantEventType getType() {
        return SignificantEventType.BONUS;
    }

    public ReelDetails getReelDetails() {
        return reelDetails;
    }

    public void setReelDetails(ReelDetails reelDetails) {
        this.reelDetails = reelDetails;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        baseWrite(kryo, output);
        kryo.writeObjectOrNull(output, reelDetails, ReelDetails.class);
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
        reelDetails = kryo.readObjectOrNull(input, ReelDetails.class);
    }

    @Override
    public void serializeObject(JsonGenerator gen, SerializerProvider serializers)
            throws IOException {
        baseSerialize(gen, serializers);

        gen.writeObjectField("reelDetails", reelDetails);
    }

    @Override
    public PlayerBonusEvent deserializeObject(JsonParser p, DeserializationContext ctxt)
            throws IOException {
        JsonNode node = p.getCodec().readTree(p);

        baseDeserialize(p, node, ctxt);
        reelDetails = ((ObjectMapper)p.getCodec()).treeToValue(node.get("reelDetails"), ReelDetails.class);
        return this;
    }

    @Override
    public String toString() {
        return "PlayerBonusEvent[" +
                "gameId=" + gameId +
                ", eventDate=" + eventDate +
                ", accountId=" + accountId +
                ", accountExternalId='" + accountExternalId + '\'' +
                ", reelDetails=" + reelDetails +
                ']';
    }
}
