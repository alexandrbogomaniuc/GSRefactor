package com.betsoft.casino.mp.model;

import com.betsoft.casino.utils.ITransportObject;

import java.util.List;

/**
 * User: flsh
 * Date: 25.05.2020.
 */
public interface IAwards extends ITransportObject {
    long getEnemyId();

    List<IAward> getAwards();

    void addAward(IAward award);
}
