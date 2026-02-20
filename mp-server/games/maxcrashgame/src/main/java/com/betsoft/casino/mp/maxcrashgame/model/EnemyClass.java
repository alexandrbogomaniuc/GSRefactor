package com.betsoft.casino.mp.maxcrashgame.model;

import com.betsoft.casino.mp.common.AbstractEnemyClass;
import com.betsoft.casino.mp.maxcrashgame.model.math.EnemyPrize;
import com.betsoft.casino.mp.maxcrashgame.model.math.EnemyType;
import com.esotericsoftware.kryo.Kryo;
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
import java.util.List;


public class EnemyClass extends AbstractEnemyClass<EnemyClass> {
    private List<EnemyPrize> awardedPrizes;
    private EnemyType enemyType;

    public EnemyClass(long id, String name, double energy, float speed, EnemyType enemyType) {
        super(id, (short) 0, (short) 0, name, energy, speed);
        this.enemyType = enemyType;
    }

    public EnemyType getEnemyType() {
        return enemyType;
    }

    @SuppressWarnings("Duplicates")
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("EnemyClass[");
        sb.append(super.toString());
        sb.append(", awardedPrizes=").append(awardedPrizes);
        sb.append(", enemyType=").append(enemyType);
        sb.append(']');
        return sb.toString();
    }

    @Override
    protected void writeInheritorFields(Kryo kryo, Output output) {
        kryo.writeClassAndObject(output, awardedPrizes);
        output.writeInt(enemyType.getId(), true);
    }

    @Override
    protected void redInheritorFields(byte version, Kryo kryo, Input input) {
        //noinspection unchecked
        awardedPrizes = (List<EnemyPrize>) kryo.readClassAndObject(input);
        enemyType = EnemyType.getById(input.readInt(true));
    }

    @Override
    protected void serializeInheritorFields(JsonGenerator gen, SerializerProvider serializers) throws IOException {
        serializeListField(gen, "awardedPrizes", awardedPrizes, new TypeReference<List<EnemyPrize>>() {});
        gen.writeNumberField("enemyType", enemyType.getId());
    }

    @Override
    protected EnemyClass getDeserialized() {
        return this;
    }

    @Override
    protected void deserializeInheritorFields(JsonParser p,
                                              JsonNode node,
                                              DeserializationContext ctxt) {
        ObjectMapper om = (ObjectMapper) p.getCodec();
        awardedPrizes = om.convertValue(node.get("awardedPrizes"), new TypeReference<List<EnemyPrize>>() {});
        enemyType = EnemyType.getById(node.get("enemyType").intValue());
    }
}
