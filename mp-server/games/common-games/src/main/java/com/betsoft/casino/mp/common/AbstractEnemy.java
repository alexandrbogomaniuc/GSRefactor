package com.betsoft.casino.mp.common;

import com.betsoft.casino.mp.model.*;
import com.betsoft.casino.mp.model.movement.Point;
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

import org.apache.logging.log4j.Logger;
import org.kynosarges.tektosyne.geometry.PointD;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * User: flsh
 * Date: 08.02.19.
 */
public abstract class AbstractEnemy<ENEMY_CLASS extends IEnemyClass, EC extends IEnemy> implements IEnemy<ENEMY_CLASS, EC> {
    protected static final byte VERSION = 2;

    protected long id;
    protected transient IMovementStrategy movementStrategy;
    protected double speed;
    protected Money awardedSum;
    //if energy<0 enemy is dead, remember that energy=0 for regular enemies
    protected double energy;
    protected double fullEnergy;
    protected int skin;
    protected Trajectory trajectory;
    protected int highEnemyNumberShots;
    protected static final int RETINUE_SKIN_ID = 99;
    protected long lastFreezeTime;
    private IMathEnemy mathEnemy;
    private long parentEnemyId;
    private long parentEnemyTypeId;
    private transient int currentTrajectoryId;
    private List<IMember> members;
    protected int swarmType;
    protected int swarmId;
    protected boolean shouldReturn;
    protected long respawnDelay;
    protected long returnTime;
    protected EnemyMode enemyMode;
    protected int lives;
    protected int currentTotalWinOfEnemy;

    public AbstractEnemy(long id, int skin, Trajectory trajectory, IMathEnemy mathEnemy, long parentEnemyId, List<IMember> members) {
        this.id = id;
        this.skin = skin;
        this.trajectory = trajectory;
        this.lastFreezeTime = 0;
        this.mathEnemy = mathEnemy;
        this.parentEnemyId = parentEnemyId;
        this.members = members;
        this.returnTime = 0;
        this.enemyMode = EnemyMode.X_1;
        this.lives = 0;
        this.currentTotalWinOfEnemy = 0;
        this.fullEnergy = 0;
        this.parentEnemyTypeId = -1;
    }

    public long getParentEnemyTypeId() {
        return parentEnemyTypeId;
    }

    public void setParentEnemyTypeId(long parentEnemyTypeId) {
        this.parentEnemyTypeId = parentEnemyTypeId;
    }

    public void setFullEnergy(double fullEnergy) {
        this.fullEnergy = fullEnergy;
    }

    protected abstract Logger getLogger();

    protected abstract void writeEnemyClass(Kryo kryo, Output output);

    protected abstract void readEnemyClass(Kryo kryo, Input input);

    protected abstract void writeAwardedPrizes(Kryo kryo, Output output);

    protected abstract void readAwardedPrizes(Kryo kryo, Input input);

    @Override
    public boolean isBoss() {
        return getEnemyClass().getEnemyType().isBoss();
    }

    @Override
    public boolean update() {
        return movementStrategy.update();
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public double getSpeed() {
        return speed;
    }

    @Override
    public void setSpeed(double speed) {
        this.speed = speed;
    }

    @Override
    public long getLastFreezeTime() {
        return lastFreezeTime;
    }

    @Override
    public void setLastFreezeTime(long lastFreezeTime) {
        this.lastFreezeTime = lastFreezeTime;
    }

    public int getCurrentTrajectoryId() {
        return currentTrajectoryId;
    }

    public void setCurrentTrajectoryId(int currentTrajectoryId) {
        this.currentTrajectoryId = currentTrajectoryId;
    }

    public void addToSwarm(ISwarmType swarmType, int swarmId) {
        this.swarmType = swarmType.getTypeId();
        this.swarmId = swarmId;
    }

    public void setSwarmType(int swarmType) {
        this.swarmType = swarmType;
    }

    public void addToSwarm(int swarmTypeId, int swarmId) {
        this.swarmType = swarmTypeId;
        this.swarmId = swarmId;
    }

    public boolean isPartOfSwarm() {
        return swarmType > 0;
    }

    public int getSwarmType() {
        return swarmType;
    }

    public int getSwarmId() {
        return swarmId;
    }

    public boolean isShouldReturn() {
        return shouldReturn;
    }

    public void setShouldReturn(boolean shouldReturn) {
        this.shouldReturn = shouldReturn;
    }

    public long getRespawnDelay() {
        return respawnDelay;
    }

    public void setRespawnDelay(long respawnDelay) {
        this.respawnDelay = respawnDelay;
    }

    public long getReturnTime() {
        return returnTime;
    }

    public void setReturnTime(long returnTime) {
        this.returnTime = returnTime;
    }

    public EnemyMode getEnemyMode() {
        if (enemyMode == null)
            enemyMode = EnemyMode.X_1;
        return enemyMode;
    }

    public int getCurrentTotalWinOfEnemy() {
        return currentTotalWinOfEnemy;
    }

    public void setCurrentTotalWinOfEnemy(int currentTotalWinOfEnemy) {
        this.currentTotalWinOfEnemy = currentTotalWinOfEnemy;
    }

    public void setEnemyMode(EnemyMode enemyMode) {
        this.enemyMode = enemyMode;
    }

    @Override
    public PointD getLocation(long time) {
        List<Point> points = trajectory.getPoints();

        if (points.get(0).getTime() > time) {
            return new PointD(points.get(0).getX(), points.get(0).getY());
        }

        Point lastPoint = points.get(points.size() - 1);
        if (time >= lastPoint.getTime()) {
            return new PointD(lastPoint.getX(), lastPoint.getY());
        }

        int i = 1;
        while (i < points.size() && time > points.get(i).getTime()) {
            i++;
        }

        Point a = points.get(i - 1);
        Point b = points.get(i);
        double percent = ((double) (time - a.getTime())) / (b.getTime() - a.getTime());

        return new PointD(a.getX() + (b.getX() - a.getX()) * percent, a.getY() + (b.getY() - a.getY()) * percent);
    }

    @Override
    public boolean isLocationNearEnd(long time) {
        List<Point> points = trajectory.getPoints();

        if (points.get(0).getTime() > time) {
            return false;
        }

        Point lastPoint = points.get(points.size() - 1);
        if (time >= lastPoint.getTime()) {
            return true;
        }

        int i = 1;
        while (i < points.size() && time > points.get(i).getTime()) {
            i++;
        }
        return i >= points.size() - 7;
    }

    @Override
    public boolean isMovable() {
        return movementStrategy != null;
    }

    @Override
    public boolean isDestroyable() {
        return true;
    }

    @Override
    public boolean isRespawn() {
        return true;
    }

    @Override
    public boolean isCollidable() {
        return true;
    }

    @Override
    public short getWidth() {
        return getEnemyClass().getWidth();
    }

    @Override
    public short getHeight() {
        return getEnemyClass().getHeight();
    }

    @Override
    public Money getAwardedSum() {
        return awardedSum;
    }

    @Override
    public void setAwardedSum(Money awardedSum) {
        this.awardedSum = awardedSum;
    }

    @Override
    public double getEnergy() {
        return energy;
    }

    @Override
    public void setEnergy(double energy) {
        this.energy = energy;
    }

    @Override
    public IMovementStrategy getMovementStrategy() {
        return movementStrategy;
    }

    @Override
    public void setMovementStrategy(IMovementStrategy movementStrategy) {
        this.movementStrategy = movementStrategy;
    }

    @Override
    public int getSkin() {
        return skin;
    }

    @Override
    public void setSkin(int skin) {
        this.skin = skin;
    }

    @Override
    public Trajectory getTrajectory() {
        return trajectory;
    }

    @Override
    public void setTrajectory(Trajectory trajectory) {
        this.trajectory = trajectory;
    }

    @Override
    public long getLeaveTime() {
        List<Point> points = this.trajectory.getPoints();
        if (points.isEmpty()) {
            getLogger().error("Bad trajectory form *.json file");
            return 0;
        }
        return points.get(points.size() - 1).getTime();
    }

    @Override
    public int getHighEnemyNumberShots() {
        return highEnemyNumberShots;
    }

    @Override
    public void incHighEnemyNumberShots() {
        highEnemyNumberShots++;
    }

    @Override
    public void setHighEnemyNumberShots(int highEnemyNumberShots) {
        this.highEnemyNumberShots = highEnemyNumberShots;
    }

    public List<IMember> getMembers() {
        if (members == null) {
            members = new ArrayList<>();
        }
        return members;
    }

    public void setMembers(List<IMember> members) {
        this.members = members;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("");
        sb.append(", id=").append(id);
        sb.append(", movementStrategy=").append(movementStrategy);
        sb.append(", speed=").append(speed);
        sb.append(", awardedSum=").append(awardedSum);
        sb.append(", energy=").append(energy);
        sb.append(", fullEnergy=").append(fullEnergy);
        sb.append(", skin=").append(skin);
        sb.append(", trajectory=").append(trajectory);
        sb.append(", highEnemyNumberShots=").append(highEnemyNumberShots);
        sb.append(", lastFreezeTime=").append(lastFreezeTime);
        sb.append(", mathEnemy=").append(mathEnemy);
        sb.append(", parentEnemyId=").append(parentEnemyId);
        sb.append(", currentTrajectoryId=").append(currentTrajectoryId);
        sb.append(", members=").append(members);
        sb.append(", swarmType=").append(swarmType);
        sb.append(", swarmId=").append(swarmId);
        sb.append(", shouldReturn=").append(shouldReturn);
        sb.append(", respawnDelay=").append(respawnDelay);
        sb.append(", returnTime=").append(returnTime);
        sb.append(", enemyMode=").append(enemyMode);
        sb.append(", lives=").append(lives);
        sb.append(", currentTotalWinOfEnemy=").append(currentTotalWinOfEnemy);
        sb.append(", parentEnemyTypeId=").append(parentEnemyTypeId);
        return sb.toString();
    }

    protected void writeInheritorFields(Kryo kryo, Output output) {
        //nop by default.
    }

    protected void readInheritorFields(byte version, Kryo kryo, Input input) {
        //nop by default.
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        output.writeLong(id, true);
        writeEnemyClass(kryo, output);
        kryo.writeClassAndObject(output, movementStrategy);
        output.writeDouble(speed);
        writeAwardedPrizes(kryo, output);
        kryo.writeClassAndObject(output, awardedSum);
        output.writeDouble(energy);
        output.writeInt(skin, true);
        kryo.writeClassAndObject(output, trajectory);
        output.writeInt(highEnemyNumberShots, true);
        output.writeLong(lastFreezeTime, true);
        kryo.writeClassAndObject(output, mathEnemy);
        output.writeLong(parentEnemyId, true);
        writeInheritorFields(kryo, output);
        kryo.writeClassAndObject(output, members);
        output.writeInt(swarmType, true);
        output.writeInt(swarmId, true);
        output.writeBoolean(shouldReturn);
        output.writeLong(respawnDelay, true);
        output.writeLong(returnTime, true);
        output.writeInt(getEnemyMode().ordinal(), true);
        output.writeInt(lives, true);
        output.writeInt(currentTotalWinOfEnemy, true);
        output.writeDouble(fullEnergy);
        output.writeLong(parentEnemyTypeId, true);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void read(Kryo kryo, Input input) {
        byte version = input.readByte();
        id = input.readLong(true);
        readEnemyClass(kryo, input);
        movementStrategy = (IMovementStrategy) kryo.readClassAndObject(input);
        if (movementStrategy != null) {
            movementStrategy.setSelf(this);
        }
        speed = input.readDouble();
        readAwardedPrizes(kryo, input);
        awardedSum = (Money) kryo.readClassAndObject(input);
        energy = input.readDouble();
        skin = input.readInt(true);
        trajectory = (Trajectory) kryo.readClassAndObject(input);
        highEnemyNumberShots = input.readInt(true);
        lastFreezeTime = input.readLong(true);
        mathEnemy = (IMathEnemy) kryo.readClassAndObject(input);
        parentEnemyId = input.readLong(true);
        readInheritorFields(version, kryo, input);
        members = (List<IMember>) kryo.readClassAndObject(input);
        swarmType = input.readInt(true);
        swarmId = input.readInt(true);
        shouldReturn = input.readBoolean();
        respawnDelay = input.readLong(true);
        returnTime = input.readLong(true);
        enemyMode = EnemyMode.values()[input.readInt(true)];
        lives = input.readInt(true);
        currentTotalWinOfEnemy = input.readInt(true);
        if (version >= 1) {
            fullEnergy = input.readDouble();
        }
        if (version >= 2) {
            parentEnemyTypeId = input.readLong(true);
        }
    }

    @Override
    public void serializeObject(JsonGenerator gen, SerializerProvider serializers) throws IOException {
        

        gen.writeNumberField("id", id);
        serializeEnemyClass(gen, serializers);
        gen.writeObjectField("movementStrategy", movementStrategy);
        gen.writeNumberField("speed", speed);
        serializeAwardedPrizes(gen, serializers);
        gen.writeObjectField("awardedSum", awardedSum);
        gen.writeNumberField("energy", energy);
        gen.writeNumberField("skin", skin);
        gen.writeObjectField("trajectory", trajectory);
        gen.writeNumberField("highEnemyNumberShots", highEnemyNumberShots);
        gen.writeNumberField("lastFreezeTime", lastFreezeTime);
        gen.writeObjectField("mathEnemy", mathEnemy);
        gen.writeNumberField("parentEnemyId", parentEnemyId);
        serializeInheritorFields(gen, serializers);
        serializeListField(gen, "members", members, new TypeReference<List<IMember>>() {});
        gen.writeNumberField("swarmType", swarmType);
        gen.writeNumberField("swarmId", swarmId);
        gen.writeBooleanField("shouldReturn", shouldReturn);
        gen.writeNumberField("respawnDelay", respawnDelay);
        gen.writeNumberField("returnTime", returnTime);
        gen.writeNumberField("enemyModeId", getEnemyMode().ordinal());
        gen.writeNumberField("lives", lives);
        gen.writeNumberField("currentTotalWinOfEnemy", currentTotalWinOfEnemy);
        gen.writeNumberField("fullEnergy", fullEnergy);
        gen.writeNumberField("parentEnemyTypeId", parentEnemyTypeId);


    }

    protected abstract void serializeInheritorFields(JsonGenerator gen,
                                                     SerializerProvider serializers) throws IOException;

    protected abstract void serializeAwardedPrizes(JsonGenerator gen,
                                                   SerializerProvider serializers) throws IOException;

    protected abstract void serializeEnemyClass(JsonGenerator gen, SerializerProvider serializers) throws IOException;

    @Override
    public EC deserializeObject(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode node = p.getCodec().readTree(p);
        ObjectMapper om = (ObjectMapper) p.getCodec();

        id = node.get("id").longValue();
        deserializeEnemyClass(p, node, ctxt);
        movementStrategy = om.convertValue(node.get("movementStrategy"), IMovementStrategy.class);
        if (movementStrategy != null) {
            movementStrategy.setSelf(this);
        }
        speed = node.get("speed").doubleValue();
        deserializeAwardedPrizes(p, node, ctxt);
        awardedSum = om.convertValue(node.get("awardedSum"), Money.class);
        energy = node.get("energy").doubleValue();
        skin = node.get("skin").intValue();
        trajectory = om.convertValue(node.get("trajectory"), Trajectory.class);
        highEnemyNumberShots = node.get("highEnemyNumberShots").intValue();
        lastFreezeTime = node.get("lastFreezeTime").longValue();
        mathEnemy = om.convertValue(node.get("mathEnemy"), IMathEnemy.class);
        parentEnemyId = node.get("parentEnemyId").longValue();
        deserializeInheritorFields(p, node, ctxt);
        members = om.convertValue(node.get("members"), new TypeReference<List<IMember>>() {});
        swarmType = node.get("swarmType").intValue();
        swarmId = node.get("swarmId").intValue();
        shouldReturn = node.get("shouldReturn").booleanValue();
        respawnDelay = node.get("respawnDelay").longValue();
        returnTime = node.get("returnTime").longValue();
        enemyMode = EnemyMode.values()[node.get("enemyModeId").asInt()];
        lives = node.get("lives").asInt();
        currentTotalWinOfEnemy = node.get("currentTotalWinOfEnemy").intValue();
        fullEnergy = node.get("fullEnergy").doubleValue();
        parentEnemyTypeId = node.get("parentEnemyTypeId").longValue();

        return getDeserialized();
    }

    protected abstract EC getDeserialized();

    protected abstract void deserializeInheritorFields(JsonParser p,
                                                       JsonNode node,
                                                       DeserializationContext ctxt) throws IOException;

    protected abstract void deserializeAwardedPrizes(JsonParser p,
                                                     JsonNode node,
                                                     DeserializationContext ctxt) throws IOException;

    protected abstract void deserializeEnemyClass(JsonParser p,
                                                  JsonNode node,
                                                  DeserializationContext ctxt) throws IOException;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractEnemy enemy = (AbstractEnemy) o;
        return id == enemy.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public void makeEnemyRetinue() {
        this.skin = RETINUE_SKIN_ID;
    }

    @Override
    public boolean isFake() {
        return id == -10;
    }


    @Override
    public void checkFreezeTime(int maxFreezeTime) {
        if (getLastFreezeTime() != 0 && getFreezeTimeRemaining(maxFreezeTime) == 0) {
            this.lastFreezeTime = 0;
        }
    }

    @Override
    public int getFreezeTimeRemaining(int maxFreezeTime) {
        if (lastFreezeTime == 0) {
            return 0;
        }
        long diff = maxFreezeTime - (System.currentTimeMillis() - lastFreezeTime);
        return diff > 0 ? (int) diff : 0;
    }

    public IMathEnemy getMathEnemy() {
        return mathEnemy;
    }

    public void setMathEnemy(IMathEnemy mathEnemy) {
        this.mathEnemy = mathEnemy;
    }

    @Override
    public long getParentEnemyId() {
        return parentEnemyId;
    }

    public void setParentEnemyId(long parentEnemyId) {
        this.parentEnemyId = parentEnemyId;
    }

    @Override
    public double getFullEnergy() {
        return mathEnemy == null ? 0 : mathEnemy.getFullEnergy();
    }

    @Override
    public boolean isInvulnerable(long time) {
        List<Point> points = trajectory.getPoints();

        if (points.get(0).getTime() > time) {
            return true;
        }

        Point lastPoint = points.get(points.size() - 1);
        if (time >= lastPoint.getTime()) {
            return true;
        }

        int i = 1;
        while (i < points.size() && time > points.get(i).getTime()) {
            i++;
        }

        return points.get(i - 1).isInvulnerable();
    }

    public long getRemainingLifeTime(long sinceTime) {
        return Math.max(0, trajectory.getLastPoint().getTime() - sinceTime);
    }

    @Override
    public int getLives() {
        return lives;
    }

    @Override
    public void setLives(int lives) {
        this.lives = lives;
    }
}
