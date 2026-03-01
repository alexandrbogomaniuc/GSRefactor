package com.abs.casino.common.promo;

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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;

/**
 * User: flsh
 * Date: 11.01.17.
 */
public class RankPrize implements KryoSerializable, JsonSelfSerializable<RankPrize> {
    private static final byte VERSION = 0;

    private RankRange rankRange;
    private IMaterialPrize prize;

    public RankPrize() {
    }

    public RankPrize(RankRange rankRange, IMaterialPrize prize) {
        this.rankRange = rankRange;
        this.prize = prize;
    }

    public boolean isInRange(int rank) {
        return rankRange.isInRange(rank);
    }

    public RankRange getRankRange() {
        return rankRange;
    }

    public IMaterialPrize getPrize() {
        return prize;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        kryo.writeClassAndObject(output, rankRange);
        kryo.writeClassAndObject(output, prize);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        byte ver = input.readByte();
        rankRange = (RankRange) kryo.readClassAndObject(input);
        prize = (IMaterialPrize) kryo.readClassAndObject(input);
    }

    @Override
    public void serializeObject(JsonGenerator gen, SerializerProvider serializers)
            throws IOException {
        gen.writeObjectField("rankRange", rankRange);
        gen.writeObjectField("prize", prize);
    }

    @Override
    public RankPrize deserializeObject(JsonParser p, DeserializationContext ctxt)
            throws IOException {
        ObjectMapper om = (ObjectMapper) p.getCodec();
        JsonNode node = om.readTree(p);

        rankRange = om.treeToValue(node.get("rankRange"), RankRange.class);
        prize = om.treeToValue(node.get("prize"), IMaterialPrize.class);

        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RankPrize rankPrize = (RankPrize) o;

        if (!rankRange.equals(rankPrize.rankRange)) return false;
        return prize.equals(rankPrize.prize);

    }

    @Override
    public int hashCode() {
        return rankRange.hashCode();
    }

    @Override
    public String toString() {
        return "RankPrize[" +
                "rankRange=" + rankRange +
                ", prize=" + prize +
                ']';
    }
}
