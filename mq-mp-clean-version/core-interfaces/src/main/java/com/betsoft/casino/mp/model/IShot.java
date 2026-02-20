package com.betsoft.casino.mp.model;

import com.betsoft.casino.utils.InboundObject;

/**
 * User: flsh
 * Date: 19.05.2020.
 */
public interface IShot extends InboundObject {
    int getWeaponId();

    long getEnemyId();

    float getX();

    float getY();

    boolean isPaidSpecialShot();

    int getRealWeaponId();

    String getBulletId();

    int getWeaponPrice();
}
