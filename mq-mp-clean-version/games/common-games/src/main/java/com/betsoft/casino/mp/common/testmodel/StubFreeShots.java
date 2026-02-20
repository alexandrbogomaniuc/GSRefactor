package com.betsoft.casino.mp.common.testmodel;

import com.betsoft.casino.mp.model.FreeShotQueueType;
import com.betsoft.casino.mp.model.IFreeShots;
import com.dgphoenix.casino.gs.singlegames.tools.cbservtools.CBGameException;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class StubFreeShots implements IFreeShots {
    private static final double[][] points = new double[][]{
            {152, 270}, {304, 270}, {456, 270}, {608, 270}, {760, 270}
    };
    private Map<Integer, AtomicInteger> currentQueue;
    private Map<Integer, AtomicInteger> nextQueue;
    private Map<Integer, AtomicInteger> tempQueue;
    private boolean leftToRight;
    private int currentPos;
    private long lastShotTime;

    public StubFreeShots() {
        this.currentQueue = new HashMap<>();
        this.nextQueue = new HashMap<>();
        this.tempQueue = new HashMap<>();
        this.leftToRight = true;
        this.currentPos = 0;
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
    public void setCurrentQueue(Map<Integer, AtomicInteger> currentQueue) {
        this.currentQueue = currentQueue;
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
        StubFreeShots freeShots = (StubFreeShots) o;
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

