package com.betsoft.casino.mp.model;

import com.betsoft.casino.utils.ITransportObject;

/**
 * User: flsh
 * Date: 25.05.2020.
 */
public interface IShotResponse extends ITransportObject {
    int getSeatId();

    int getWeaponId();

    void setRemainingSWShots(int remainingSWShots);

    int getRemainingSWShots();
}
