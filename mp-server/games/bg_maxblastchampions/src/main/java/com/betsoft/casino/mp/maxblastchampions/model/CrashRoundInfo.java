package com.betsoft.casino.mp.maxblastchampions.model;

import com.betsoft.casino.mp.model.ICrashRoundInfo;
import com.abs.casino.common.cache.JsonSelfSerializable;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

public class CrashRoundInfo implements ICrashRoundInfo, KryoSerializable, JsonSelfSerializable<CrashRoundInfo> {
    private static final byte VERSION = 1;

    private double mult;
    private long startTime;
    private long roundId;
    private int bets;
    private String salt;
    private String token;
    private Map<String, Double> winners;

    private double kilometerMult;

    public CrashRoundInfo() {}

    public CrashRoundInfo(double mult, long startTime, long roundId, int bets, String salt, String token, Map<String, Double> winners, double kilometerMult) {
        this.mult = mult;
        this.startTime = startTime;
        this.roundId = roundId;
        this.bets = bets;
        this.salt = salt;
        this.token = token;
        this.winners = winners;
        this.kilometerMult = kilometerMult;
    }

    public double getMult() {
        return mult;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getRoundId() {
        return roundId;
    }

    public int getBets() {
        return bets;
    }

    public String getToken() {
        return token;
    }

    public String getSalt() {
        return salt;
    }

    @Override
    public Map<String, Double> getWinners() {
        return winners == null ? new HashMap<>() : winners;
    }

    @Override
    public double getKilometerMult() {
        return kilometerMult;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        output.writeDouble(mult);
        output.writeLong(startTime, true);
        output.writeLong(roundId, true);
        output.writeInt(bets, true);
        output.writeString(salt);
        output.writeString(token);
        kryo.writeClassAndObject(output, getWinners());
        output.writeDouble(kilometerMult);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void read(Kryo kryo, Input input) {
        byte version = input.readByte();
        mult = input.readDouble();
        startTime = input.readLong(true);
        roundId = input.readLong(true);
        bets = input.readInt(true);
        salt = input.readString();
        token = input.readString();
        winners = (Map<String, Double>) kryo.readClassAndObject(input);
        if (version > 0) {
            kilometerMult = input.readDouble();
        }
    }

    @Override
    public void serializeObject(JsonGenerator gen, SerializerProvider serializers)
            throws IOException {
        gen.writeNumberField("mult", mult);
        gen.writeNumberField("startTime", startTime);
        gen.writeNumberField("roundId", roundId);
        gen.writeNumberField("bets", bets);
        gen.writeStringField("salt", salt);
        gen.writeStringField("token", token);
        serializeMapField(gen, "winners", getWinners(), new TypeReference<Map<String,Double>>() {});
        gen.writeNumberField("kilometerMult", kilometerMult);
    }

    @Override
    public CrashRoundInfo deserializeObject(JsonParser p, DeserializationContext ctxt)
            throws IOException {
        JsonNode node = p.getCodec().readTree(p);
        ObjectMapper om = (ObjectMapper) p.getCodec();

        mult = node.get("mult").asDouble();
        startTime = node.get("startTime").asLong();
        roundId = node.get("roundId").asLong();
        bets = node.get("bets").asInt();
        salt = readNullableText(node, "salt");
        token = readNullableText(node, "token");
        winners = om.treeToValue(node.get("winners"), new TypeReference<Map<String, Double>>() {});
        kilometerMult = node.get("kilometerMult").asDouble();

        return this;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", CrashRoundInfo.class.getSimpleName() + "[", "]")
                .add("mult=" + mult)
                .add("startTime=" + startTime)
                .add("roundId=" + roundId)
                .add("bets=" + bets)
                .add("salt='" + salt + "'")
                .add("token='" + token + "'")
                .add("winners='" + winners + "'")
                .add("kilometerMult='" + kilometerMult + "'")
                .toString();
    }
}
