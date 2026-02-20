package com.betsoft.casino.mp.model;

import com.betsoft.casino.mp.model.movement.Trajectory;
import com.betsoft.casino.utils.ITransportObject;

import java.util.Map;

/**
 * User: flsh
 * Date: 25.05.2020.
 */
public interface IUpdateTrajectories extends ITransportObject {
    Map<Long, Trajectory> getTrajectories();

    int getFreezeTime();

    int getAnimationId();
}
