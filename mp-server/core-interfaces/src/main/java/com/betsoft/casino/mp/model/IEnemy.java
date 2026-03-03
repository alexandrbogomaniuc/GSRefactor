package com.betsoft.casino.mp.model;

import com.abs.casino.common.cache.Identifiable;
import com.abs.casino.common.cache.JsonSelfSerializable;
import com.esotericsoftware.kryo.KryoSerializable;

import java.util.List;

/**
 * User: flsh
 * Date: 08.02.19.
 */
public interface IEnemy<ENEMY_CLASS extends IEnemyClass, EC extends IEnemy> extends IMapItem, Identifiable, 
        KryoSerializable, JsonSelfSerializable<EC> {

    boolean isBoss();

    ENEMY_CLASS getEnemyClass();

    String getAwardedPrizesAsString();

    List<IEnemyPrize> getAwardedPrizes();

    void setAwardedPrizes(List<IEnemyPrize> awardedPrizes);

    boolean isFake();

    long getLastFreezeTime();

    void setLastFreezeTime(long lastFreezeTime);

    void checkFreezeTime(int maxFreezeTime);

    int getFreezeTimeRemaining(int maxFreezeTime);

    long getParentEnemyId();

    double getFullEnergy();

    List<IMember> getMembers();

    boolean isInvulnerable(long time);

    int getSwarmType();

    int getSwarmId();

    EnemyMode getEnemyMode();

    void setEnemyMode(EnemyMode enemyMode);

    int getLives();

    void setLives(int lives);
}
