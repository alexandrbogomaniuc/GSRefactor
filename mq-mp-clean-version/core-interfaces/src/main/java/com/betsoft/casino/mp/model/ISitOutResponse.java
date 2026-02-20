package com.betsoft.casino.mp.model;

import com.betsoft.casino.utils.ITransportObject;

/**
 * User: flsh
 * Date: 25.05.2020.
 */
public interface ISitOutResponse extends ITransportObject {
    int getId();

    String getNickname();

    long getOutDate();

    long getCompensateSpecialWeapons();

    long getSurplusHvBonus();

    long getTotalReturnedSpecialWeapons();
}
