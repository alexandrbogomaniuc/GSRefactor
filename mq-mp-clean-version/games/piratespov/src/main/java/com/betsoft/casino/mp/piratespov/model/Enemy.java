package com.betsoft.casino.mp.piratespov.model;

import com.betsoft.casino.mp.common.AbstractEnemy;
import com.betsoft.casino.mp.model.IEnemyPrize;
import com.betsoft.casino.mp.model.IMathEnemy;
import com.betsoft.casino.mp.model.IMember;
import com.betsoft.casino.mp.model.movement.Trajectory;
import com.betsoft.casino.mp.piratescommon.model.math.EnemyType;
import com.dgphoenix.casino.common.util.string.StringUtils;
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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * User: flsh
 * Date: 21.09.17.
 */
public class Enemy extends AbstractEnemy<EnemyClass, Enemy> {

    private static final Logger LOG = LogManager.getLogger(Enemy.class);

    private EnemyClass enemyClass;
    private List<IEnemyPrize> awardedPrizes = new ArrayList<>();

    public Enemy(long id, EnemyClass enemyClass, int skin, Trajectory trajectory, IMathEnemy mathEnemy, long parentEnemyId,
                 List<IMember> members) {
        super(id, skin, trajectory, mathEnemy, parentEnemyId, members);
        this.enemyClass = enemyClass;
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }

    @Override
    public EnemyClass getEnemyClass() {
        return enemyClass;
    }

    public EnemyType getEnemyType() {
        return enemyClass.getEnemyType();
    }

    @Override
    public String getAwardedPrizesAsString() {
        return StringUtils.toString(awardedPrizes, ",", enemyPrize -> String.valueOf(enemyPrize.getOrdinalValue()));
    }

    @Override
    public List<IEnemyPrize> getAwardedPrizes() {
        return awardedPrizes;
    }

    @Override
    public void setAwardedPrizes(List<IEnemyPrize> awardedPrizes) {
        this.awardedPrizes = awardedPrizes;
    }

    @SuppressWarnings("Duplicates")
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Enemy [");
        sb.append(super.toString());
        sb.append(", enemyClass=").append(enemyClass);
        sb.append(", awardedPrizes=").append(awardedPrizes);
        sb.append(']');
        return sb.toString();
    }

    @Override
    protected void writeEnemyClass(Kryo kryo, Output output) {
        kryo.writeObject(output, enemyClass);
    }

    @Override
    protected void readEnemyClass(Kryo kryo, Input input) {
        enemyClass = kryo.readObject(input, EnemyClass.class);
    }

    @Override
    protected void writeAwardedPrizes(Kryo kryo, Output output) {
        kryo.writeClassAndObject(output, awardedPrizes);
    }

    @Override
    protected void readAwardedPrizes(Kryo kryo, Input input) {
        awardedPrizes = (List<IEnemyPrize>) kryo.readClassAndObject(input);
    }

    @Override
    protected void serializeInheritorFields(JsonGenerator gen, SerializerProvider serializers) throws IOException {
    }

    @Override
    protected void serializeAwardedPrizes(JsonGenerator gen, SerializerProvider serializers) throws IOException {
        serializeListField(gen, "awardedPrizes", awardedPrizes, new TypeReference<List<IEnemyPrize>>() {});
    }

    @Override
    protected void serializeEnemyClass(JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeObjectField("enemyClass", enemyClass);
    }

    @Override
    protected Enemy getDeserialized() {
        return this;
    }

    @Override
    protected void deserializeInheritorFields(JsonParser p,
                                              JsonNode node,
                                              DeserializationContext ctxt) {
    }

    @Override
    protected void deserializeAwardedPrizes(JsonParser p,
                                            JsonNode node,
                                            DeserializationContext ctxt) {
        ObjectMapper om = (ObjectMapper) p.getCodec();
        awardedPrizes = om.convertValue(node.get("awardedPrizes"), new TypeReference<List<IEnemyPrize>>() {});
    }

    @Override
    protected void deserializeEnemyClass(JsonParser p, JsonNode node, DeserializationContext ctxt) {
        ObjectMapper om = (ObjectMapper) p.getCodec();
        enemyClass = om.convertValue("enemyClass", EnemyClass.class);
    }

    @Override
    public boolean isInvulnerable(long time) {
        return false;
    }
}
