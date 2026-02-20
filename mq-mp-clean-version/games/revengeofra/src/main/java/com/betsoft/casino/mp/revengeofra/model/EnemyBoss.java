package com.betsoft.casino.mp.revengeofra.model;

import com.betsoft.casino.mp.model.IEnemyBoss;
import com.betsoft.casino.mp.model.IMathEnemy;
import com.betsoft.casino.mp.model.gameconfig.BossPartEnemy;
import com.betsoft.casino.mp.model.movement.Trajectory;
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
import java.util.ArrayList;
import java.util.List;

public class EnemyBoss extends Enemy implements IEnemyBoss {
    List<BossPartEnemy> headEnemies;
    List<BossPartEnemy> tailEnemies;
    long finalWin;

    public EnemyBoss(long id, EnemyClass enemyClass, int skin, Trajectory trajectory, IMathEnemy mathEnemy,
                     long parentEnemyId, List<BossPartEnemy> headEnemies, List<BossPartEnemy> tailEnemies, long finalWin) {
        super(id, enemyClass, skin, trajectory, mathEnemy, parentEnemyId, new ArrayList<>());
        this.headEnemies = headEnemies;
        this.tailEnemies = tailEnemies;
        this.finalWin = finalWin;
    }

    @Override
    public List<BossPartEnemy> getHeadEnemies() {
        return headEnemies;
    }

    public void setHeadEnemies(List<BossPartEnemy> headEnemies) {
        this.headEnemies = headEnemies;
    }

    @Override
    public List<BossPartEnemy> getTailEnemies() {
        return tailEnemies;
    }

    public void setTailEnemies(List<BossPartEnemy> tailEnemies) {
        this.tailEnemies = tailEnemies;
    }

    public long getFinalWin() {
        return finalWin;
    }

    public void setFinalWin(long finalWin) {
        this.finalWin = finalWin;
    }

    @Override
    protected void writeInheritorFields(Kryo kryo, Output output) {
        super.writeInheritorFields(kryo, output);
        kryo.writeClassAndObject(output, headEnemies);
        kryo.writeClassAndObject(output, tailEnemies);
        output.writeLong(finalWin, true);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void readInheritorFields(byte version, Kryo kryo, Input input) {
        super.readInheritorFields(version, kryo, input);
        headEnemies = (List<BossPartEnemy>) kryo.readClassAndObject(input);
        tailEnemies = (List<BossPartEnemy>) kryo.readClassAndObject(input);
        finalWin = input.readLong(true);
    }

    @Override
    protected void serializeInheritorFields(JsonGenerator gen, SerializerProvider serializers) throws IOException {
        super.serializeInheritorFields(gen, serializers);
        serializeListField(gen, "headEnemies", headEnemies, new TypeReference<List<BossPartEnemy>>() {});
        serializeListField(gen, "tailEnemies", tailEnemies, new TypeReference<List<BossPartEnemy>>() {});
        gen.writeNumberField("finalWin", finalWin);
    }

    @Override
    protected void deserializeInheritorFields(JsonParser p,
                                              JsonNode node,
                                              DeserializationContext ctxt) {
        super.deserializeInheritorFields(p, node, ctxt);

        ObjectMapper om = (ObjectMapper) p.getCodec();

        headEnemies = om.convertValue(node.get("headEnemies"), new TypeReference<List<BossPartEnemy>>() {});
        tailEnemies = om.convertValue(node.get("tailEnemies"), new TypeReference<List<BossPartEnemy>>() {});
        finalWin = node.get("finalWin").longValue();
    }

    @Override
    protected EnemyBoss getDeserialized() {
        return this;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("EnemyBoss{");
        sb.append("headEnemies=").append(headEnemies);
        sb.append(", tailEnemies=").append(tailEnemies);
        sb.append(", finalWin=").append(finalWin);
        sb.append('}');
        return sb.toString();
    }
}
