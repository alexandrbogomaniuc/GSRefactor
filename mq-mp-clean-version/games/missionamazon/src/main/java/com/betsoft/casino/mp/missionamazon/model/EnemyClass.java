package com.betsoft.casino.mp.missionamazon.model;

import com.betsoft.casino.mp.common.AbstractEnemyClass;
import com.betsoft.casino.mp.missionamazon.model.math.EnemyPrize;
import com.betsoft.casino.mp.missionamazon.model.math.EnemyType;
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
import java.util.Objects;

/**
 * User: flsh
 * Date: 21.09.17.
 */

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        EnemyClass that = (EnemyClass) o;

        if (!Objects.equals(awardedPrizes, that.awardedPrizes))
            return false;
        return enemyType == that.enemyType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), awardedPrizes, enemyType);
    }

    @Override
    public String toString() {
        return "EnemyClass" + "[" +
                "awardedPrizes=" + awardedPrizes +
                ", enemyType=" + enemyType +
                ']';
    }
}
