package com.betsoft.casino.mp.model;

import com.betsoft.casino.mp.model.movement.Trajectory;
import com.dgphoenix.casino.common.cache.Identifiable;
import org.kynosarges.tektosyne.geometry.PointD;

import java.util.List;
import java.util.Optional;

/**
 * User: flsh
 * Date: 19.05.2020.
 */
public interface IRoomEnemy<MEMBER extends IMember> extends Identifiable {
    void setId(long id);

    long getTypeId();

    void setTypeId(long typeId);

    double getSpeed();

    void setSpeed(double speed);

    String getAwardedPrizes();

    void setAwardedPrizes(String awardedPrizes);

    double getAwardedSum();

    void setAwardedSum(double awardedSum);

    double getEnergy();

    void setEnergy(int energy);

    int getSkin();

    void setSkin(int skin);

    Trajectory getTrajectory();

    void setTrajectory(Trajectory trajectory);

    double getFullEnergy();

    void setFullEnergy(double fullEnergy);

    List<MEMBER> getMembers();

    long getSwarmId();

    void setSwarmId(long swarmId);

    long getSwarmType();

    void setSwarmTypeId(long swarmType);

    long getParentEnemyId();

    long getParentEnemyTypeId();

    void setParentEnemyTypeId(long typeId);

    boolean isBoss();
}
