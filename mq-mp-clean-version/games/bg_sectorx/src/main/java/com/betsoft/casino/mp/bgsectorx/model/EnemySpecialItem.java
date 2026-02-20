package com.betsoft.casino.mp.bgsectorx.model;

import com.betsoft.casino.mp.model.IMathEnemy;
import com.betsoft.casino.mp.model.IMember;
import com.betsoft.casino.mp.model.movement.Trajectory;
import com.betsoft.casino.mp.bgsectorx.model.math.EnemyType;
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

public class EnemySpecialItem extends Enemy {
    private List<EnemyType> enemiesForKilling;
    private long additionalKillAwardPayout;
    private int currentMultiplier;
    private int totalPayout;

    public EnemySpecialItem(long id, EnemyClass enemyClass, int skin, Trajectory trajectory, IMathEnemy mathEnemy,
                            long parentEnemyId, List<IMember> members, List<EnemyType> enemiesForKilling, long additionalKillAwardPayout,
                            int currentMultiplier, int totalPayout) {
        super(id, enemyClass, skin, trajectory, mathEnemy, parentEnemyId, members);
        this.enemiesForKilling = enemiesForKilling;
        this.additionalKillAwardPayout = additionalKillAwardPayout;
        this.currentMultiplier = currentMultiplier;
        this.totalPayout = totalPayout;
    }

    public int getCurrentMultiplier() {
        return currentMultiplier;
    }

    public void setCurrentMultiplier(int currentMultiplier) {
        this.currentMultiplier = currentMultiplier;
    }

    public List<EnemyType> getEnemiesForKilling() {
        return enemiesForKilling;
    }

    public void setEnemiesForKilling(List<EnemyType> enemiesForKilling) {
        this.enemiesForKilling = enemiesForKilling;
    }

    public long getAdditionalKillAwardPayout() {
        return additionalKillAwardPayout;
    }

    public void setAdditionalKillAwardPayout(long additionalKillAwardPayout) {
        this.additionalKillAwardPayout = additionalKillAwardPayout;
    }

    public int getTotalPayout() {
        return totalPayout;
    }

    public void setTotalPayout(int totalPayout) {
        this.totalPayout = totalPayout;
    }

    @Override
    protected void writeInheritorFields(Kryo kryo, Output output) {
        super.writeInheritorFields(kryo, output);
        kryo.writeClassAndObject(output, enemiesForKilling);
        output.writeLong(additionalKillAwardPayout, true);
        output.writeInt(currentMultiplier, true);
        output.writeInt(totalPayout, true);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void readInheritorFields(byte version, Kryo kryo, Input input) {
        super.readInheritorFields(version, kryo, input);
        enemiesForKilling = (List<EnemyType>) kryo.readClassAndObject(input);
        additionalKillAwardPayout = input.readLong(true);
        currentMultiplier = input.readInt(true);
        totalPayout = input.readInt(true);
    }

    @Override
    protected void serializeInheritorFields(JsonGenerator gen, SerializerProvider serializers) throws IOException {
        super.serializeInheritorFields(gen, serializers);
        serializeListField(gen, "enemiesForKilling", enemiesForKilling, new TypeReference<List<EnemyType>>() {});
        gen.writeNumberField("additionalKillAwardPayout", additionalKillAwardPayout);
        gen.writeNumberField("currentMultiplier", currentMultiplier);
        gen.writeNumberField("totalPayout", totalPayout);
    }

    @Override
    protected void deserializeInheritorFields(JsonParser p,
                                              JsonNode node,
                                              DeserializationContext ctxt) {
        super.deserializeInheritorFields(p, node, ctxt);

        ObjectMapper om = (ObjectMapper) p.getCodec();
        enemiesForKilling = om.convertValue(node.get("enemiesForKilling"), new TypeReference<List<EnemyType>>() {});
        additionalKillAwardPayout = node.get("additionalKillAwardPayout").longValue();
        currentMultiplier = node.get("currentMultiplier").intValue();
        totalPayout = node.get("totalPayout").intValue();
    }

    @Override
    protected EnemySpecialItem getDeserialized() {
        return this;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("EnemySpecialItem{");
        sb.append("id=").append(id);
        sb.append(", movementStrategy=").append(movementStrategy);
        sb.append(", speed=").append(speed);
        sb.append(", awardedSum=").append(awardedSum);
        sb.append(", energy=").append(energy);
        sb.append(", fullEnergy=").append(fullEnergy);
        sb.append(", skin=").append(skin);
        sb.append(", trajectory=").append(trajectory);
        sb.append(", highEnemyNumberShots=").append(highEnemyNumberShots);
        sb.append(", lastFreezeTime=").append(lastFreezeTime);
        sb.append(", swarmType=").append(swarmType);
        sb.append(", swarmId=").append(swarmId);
        sb.append(", shouldReturn=").append(shouldReturn);
        sb.append(", respawnDelay=").append(respawnDelay);
        sb.append(", returnTime=").append(returnTime);
        sb.append(", enemyMode=").append(enemyMode);
        sb.append(", lives=").append(lives);
        sb.append(", currentTotalWinOfEnemy=").append(currentTotalWinOfEnemy);
        sb.append(", enemiesForKilling=").append(enemiesForKilling);
        sb.append(", additionalKillAwardPayout=").append(additionalKillAwardPayout);
        sb.append(", currentMultiplier=").append(currentMultiplier);
        sb.append(", totalPayout=").append(totalPayout);
        sb.append('}');
        return sb.toString();
    }
}
