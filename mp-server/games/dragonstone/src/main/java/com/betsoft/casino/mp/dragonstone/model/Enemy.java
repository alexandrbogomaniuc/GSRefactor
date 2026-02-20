package com.betsoft.casino.mp.dragonstone.model;

import com.betsoft.casino.mp.common.AbstractEnemy;
import com.betsoft.casino.mp.dragonstone.model.math.EnemyType;
import com.betsoft.casino.mp.model.IEnemyPrize;
import com.betsoft.casino.mp.model.IMathEnemy;
import com.betsoft.casino.mp.model.IMember;
import com.betsoft.casino.mp.model.movement.Trajectory;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.List;

/**
 * User: flsh
 * Date: 21.09.17.
 */
public class Enemy extends AbstractEnemy<EnemyClass, Enemy> {

    private static final Logger LOG = LogManager.getLogger(Enemy.class);

    private EnemyClass enemyClass;

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
        return "";
    }

    @Override
    public List<IEnemyPrize> getAwardedPrizes() {
        return null;
    }

    @Override
    public void setAwardedPrizes(List<IEnemyPrize> awardedPrizes) {
    }

    @SuppressWarnings("Duplicates")
    @Override
    public String toString() {
        return "Enemy [" + super.toString() +
                ", enemyClass=" + enemyClass +
                ']';
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
    }

    @Override
    protected void readAwardedPrizes(Kryo kryo, Input input) {
    }

    @Override
    protected void serializeInheritorFields(JsonGenerator gen, SerializerProvider serializers) throws IOException {
    }

    @Override
    protected void serializeAwardedPrizes(JsonGenerator gen, SerializerProvider serializers) throws IOException {
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
    }

    @Override
    protected void deserializeEnemyClass(JsonParser p, JsonNode node, DeserializationContext ctxt) {
        ObjectMapper om = (ObjectMapper) p.getCodec();
        enemyClass = om.convertValue("enemyClass", EnemyClass.class);
    }

    @Override
    public boolean isLocationNearEnd(long time) {
        return super.isLocationNearEnd(time);
    }

    @Override
    public double getFullEnergy() {
        return fullEnergy;
    }
}
