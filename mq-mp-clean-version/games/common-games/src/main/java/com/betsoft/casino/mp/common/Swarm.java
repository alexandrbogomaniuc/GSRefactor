package com.betsoft.casino.mp.common;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Swarm<ENEMY extends AbstractEnemy> implements KryoSerializable {
    private static final byte VERSION = 0;

    private List<Long> enemyIds;
    private int removedEnemies;
    private int swarmType;
    private boolean shouldReturn;
    private long respawnDelay;
    private long returnTime;

    public Swarm() {}

    public Swarm(List<ENEMY> enemies, boolean shouldReturn, long respawnDelay) {
        this.enemyIds = getEnemyIds(enemies);
        this.swarmType = enemies.get(0).swarmType;
        this.shouldReturn = shouldReturn;
        this.respawnDelay = respawnDelay;
        this.returnTime = 0;
        this.removedEnemies = 0;
    }

    private List<Long> getEnemyIds(List<ENEMY> enemies) {
        return enemies.stream().map(AbstractEnemy::getId).collect(Collectors.toList());
    }

    public List<Long> getEnemyIds() {
        return enemyIds;
    }

    public void setEnemies(List<Long> enemyIds) {
        this.enemyIds = enemyIds;
    }

    public boolean incrementRemovedAndUpdate() {
        removedEnemies++;
        if (removedEnemies == enemyIds.size()) {
            returnTime = System.currentTimeMillis() + respawnDelay;
            removedEnemies = 0;
            return true;
        }
        return false;
    }

    public int getSwarmType() {
        return swarmType;
    }

    public long getRespawnDelay() {
        return respawnDelay;
    }

    public long getReturnTime() {
        return returnTime;
    }

    public void resetReturnTime() {
        this.returnTime = 0;
    }

    public boolean isShouldReturn() {
        return shouldReturn;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        kryo.writeObject(output, enemyIds);
        output.writeInt(removedEnemies, true);
        output.writeInt(swarmType, true);
        output.writeBoolean(shouldReturn);
        output.writeLong(respawnDelay, true);
        output.writeLong(returnTime, true);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void read(Kryo kryo, Input input) {
        input.readByte();
        enemyIds = kryo.readObject(input, ArrayList.class);
        removedEnemies = input.readInt(true);
        swarmType = input.readInt(true);
        shouldReturn = input.readBoolean();
        respawnDelay = input.readLong(true);
        returnTime = input.readLong(true);
    }

    @Override
    public String toString() {
        return "Swarm{" +
                "enemyIds=" + enemyIds +
                ", shouldReturn=" + shouldReturn +
                ", respawnDelay=" + respawnDelay +
                ", returnTime=" + returnTime +
                '}';
    }
}
