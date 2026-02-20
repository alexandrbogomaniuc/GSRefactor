package com.betsoft.casino.mp.model;

import com.betsoft.casino.mp.model.movement.Trajectory;
import org.kynosarges.tektosyne.geometry.PointD;

/**
 * User: flsh
 * Date: 21.09.17.
 */
public interface IMapItem extends IPlayEventProducer {
    PointD getLocation(long time);

    boolean isMovable();

    boolean isDestroyable();

    boolean isRespawn();

    boolean isCollidable();

    short getWidth();

    short getHeight();

    Money getAwardedSum();

    void setAwardedSum(Money awardedSum);

    double getEnergy();

    void setEnergy(double energy);

    IMovementStrategy getMovementStrategy();

    void setMovementStrategy(IMovementStrategy movementStrategy);

    int getSkin();

    void setSkin(int skin);

    Trajectory getTrajectory();

    void setTrajectory(Trajectory trajectory);

    long getLeaveTime();

    int getHighEnemyNumberShots();

    void incHighEnemyNumberShots();

    void setHighEnemyNumberShots(int highEnemyNumberShots);

    boolean update();

    double getSpeed();

    void setSpeed(double speed);

    void makeEnemyRetinue();

    boolean isLocationNearEnd(long time);
}
