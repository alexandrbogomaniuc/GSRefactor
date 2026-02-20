package com.betsoft.casino.mp.model;

import com.dgphoenix.casino.gs.singlegames.tools.cbservtools.CBGameException;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * User: flsh
 * Date: 19.05.2020.
 */
public interface IFreeShots {
    boolean noFreeShotsForFire();

    Map<Integer, AtomicInteger> getCurrentQueue();

    int getUnplayedFreeShots();

    double[] getCurrentPositionForFire();

    void moveNextPosition();

    void processTempFreeShots(FreeShotQueueType shotQueueType);

    void moveToNextStep() throws CBGameException;

    void addToTempQueue(int specialWeaponId, int count);

    void setCurrentQueue(Map<Integer, AtomicInteger> currentQueue);

    Map<Integer, AtomicInteger> getNextQueue();

    void setNextQueue(Map<Integer, AtomicInteger> nextQueue);

    Map<Integer, AtomicInteger> getTempQueue();

    void setTempQueue(Map<Integer, AtomicInteger> tempQueue);

    boolean isLeftToRight();

    void setLeftToRight(boolean leftToRight);

    int getCurrentPos();

    void setCurrentPos(int currentPos);

    long getLastShotTime();

    void setLastShotTime(long lastShotTime);
}
