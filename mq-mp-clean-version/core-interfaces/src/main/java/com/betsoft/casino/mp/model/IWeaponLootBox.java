package com.betsoft.casino.mp.model;

import com.betsoft.casino.utils.ITransportObject;

/**
 * User: flsh
 * Date: 19.05.2020.
 */
public interface IWeaponLootBox extends ITransportObject {
    int getWeaponId();

    int getShots();

    long getBalance();

    float getCurrentWin();
}
