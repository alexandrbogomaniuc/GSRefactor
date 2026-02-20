package com.betsoft.casino.mp.model;

import java.util.List;

/**
 * User: flsh
 * Date: 22.05.2020.
 */
public interface IEnemyResultPrize {
    Long getEnemyId();

    void setEnemyId(Long enemyId);

    Money getCachPrize();

    void setCachPrize(Money cachPrize);

    List<ITransportWeapon> getAwardedWeapons();

    void setAwardedWeapons(List<ITransportWeapon> awardedWeapons);
}
