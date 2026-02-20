package com.betsoft.casino.mp.model;

import com.dgphoenix.casino.gs.singlegames.tools.cbservtools.CBGameException;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class FreeShots implements IFreeShots, KryoSerializable, Serializable {
    private static final byte VERSION = 0;
    private Map<Integer, AtomicInteger> currentQueue;
    private Map<Integer, AtomicInteger> nextQueue;
    private Map<Integer, AtomicInteger> tempQueue;
    private boolean leftToRight;
    private int currentPos;
    private long lastShotTime;

    private static final double[][] points = new double[][]{
            {152, 270}, {304, 270}, {456, 270}, {608, 270}, {760, 270}
    };

//    private static final double[][] points = new double[][]{
//            {71, 31}, {63, 40}, {54, 48}, {45, 59}, {36, 69}
//    };


    public FreeShots() {
        this.currentQueue = new HashMap<>();
        this.nextQueue = new HashMap<>();
        this.tempQueue = new HashMap<>();
        this.leftToRight = true;
        this.currentPos = 0;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        kryo.writeObject(output, currentQueue);
        kryo.writeObject(output, nextQueue);
        kryo.writeObject(output, tempQueue);
        output.writeBoolean(leftToRight);
        output.writeInt(currentPos, true);
        output.writeLong(lastShotTime, true);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void read(Kryo kryo, Input input) {
        input.readByte();
        currentQueue = (Map<Integer, AtomicInteger>) kryo.readObject(input, HashMap.class);
        nextQueue = (Map<Integer, AtomicInteger>) kryo.readObject(input, HashMap.class);
        tempQueue = (Map<Integer, AtomicInteger>) kryo.readObject(input, HashMap.class);
        leftToRight = input.readBoolean();
        currentPos = input.readInt(true);
        lastShotTime = input.readLong(true);
    }

    @Override
    public boolean noFreeShotsForFire() {
        return currentQueue.isEmpty() && nextQueue.isEmpty();
    }

    @Override
    public Map<Integer, AtomicInteger> getCurrentQueue() {
        return currentQueue;
    }

    @Override
    public int getUnplayedFreeShots() {
        return currentQueue.isEmpty() ? 0 : currentQueue.values().stream().findFirst().get().get();
    }

    @Override
    public double[] getCurrentPositionForFire() {
        return points[currentPos];
    }

    @Override
    public void moveNextPosition() {
        if (leftToRight) {
            currentPos++;
            if (currentPos > 4) {
                currentPos = 3;
                leftToRight = false;
            }
        } else {
            currentPos--;
            if (currentPos < 0) {
                currentPos = 1;
                leftToRight = true;
            }
        }
    }

    @Override
    public void processTempFreeShots(FreeShotQueueType shotQueueType) {
        if (tempQueue.isEmpty())
            return;

        Map<Integer, AtomicInteger> finalRealQueue;
        if (shotQueueType.equals(FreeShotQueueType.CURRENT)) {
            finalRealQueue = currentQueue;
            lastShotTime = System.currentTimeMillis() + 500;
        } else {
            finalRealQueue = nextQueue;
        }

        tempQueue.forEach((key, value) -> {
                    finalRealQueue.putIfAbsent(key, new AtomicInteger());
                    finalRealQueue.put(key, new AtomicInteger(finalRealQueue.get(key).addAndGet(value.get())));
                }
        );
        tempQueue.clear();
    }

    @Override
    public void moveToNextStep() throws CBGameException {
        if (!currentQueue.isEmpty() || nextQueue.isEmpty()) {
            throw new CBGameException("error, player has current free shots or has not next free shots");
        }

        currentQueue.putAll(nextQueue);
        nextQueue.clear();
        leftToRight = !leftToRight;
        currentPos = leftToRight ? 0 : 4;
        lastShotTime = System.currentTimeMillis() + 4000;
    }

    @Override
    public void addToTempQueue(int specialWeaponId, int count) {
        AtomicInteger oldValue = tempQueue.get(specialWeaponId);
        if (oldValue == null) {
            tempQueue.put(specialWeaponId, new AtomicInteger(count));
        } else {
            tempQueue.put(specialWeaponId, new AtomicInteger(oldValue.addAndGet(count)));
        }
    }

    @Override
    public void setCurrentQueue(Map<Integer, AtomicInteger> currentQueue) {
        this.currentQueue = currentQueue;
    }

    @Override
    public Map<Integer, AtomicInteger> getNextQueue() {
        return nextQueue;
    }

    @Override
    public void setNextQueue(Map<Integer, AtomicInteger> nextQueue) {
        this.nextQueue = nextQueue;
    }

    @Override
    public Map<Integer, AtomicInteger> getTempQueue() {
        return tempQueue;
    }

    @Override
    public void setTempQueue(Map<Integer, AtomicInteger> tempQueue) {
        this.tempQueue = tempQueue;
    }

    @Override
    public boolean isLeftToRight() {
        return leftToRight;
    }

    @Override
    public void setLeftToRight(boolean leftToRight) {
        this.leftToRight = leftToRight;
    }

    @Override
    public int getCurrentPos() {
        return currentPos;
    }

    @Override
    public void setCurrentPos(int currentPos) {
        this.currentPos = currentPos;
    }

    @Override
    public long getLastShotTime() {
        return lastShotTime;
    }

    @Override
    public void setLastShotTime(long lastShotTime) {
        this.lastShotTime = lastShotTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FreeShots freeShots = (FreeShots) o;
        return leftToRight == freeShots.leftToRight &&
                currentPos == freeShots.currentPos &&
                lastShotTime == freeShots.lastShotTime &&
                Objects.equals(currentQueue, freeShots.currentQueue) &&
                Objects.equals(nextQueue, freeShots.nextQueue) &&
                Objects.equals(tempQueue, freeShots.tempQueue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(currentQueue, nextQueue, tempQueue, leftToRight, currentPos, lastShotTime);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("FreeShots[");
        sb.append("currentQueue=").append(currentQueue);
        sb.append(", nextQueue=").append(nextQueue);
        sb.append(", tempQueue=").append(tempQueue);
        sb.append(", leftToRight=").append(leftToRight);
        sb.append(", currentPos=").append(currentPos);
        sb.append(", lastShotTime=").append(lastShotTime);
        sb.append(']');
        return sb.toString();
    }
}
