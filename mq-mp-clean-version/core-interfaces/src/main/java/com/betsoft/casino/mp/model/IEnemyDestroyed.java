package com.betsoft.casino.mp.model;

import com.betsoft.casino.utils.IServerMessage;
import com.betsoft.casino.utils.ITransportObject;

/**
 * User: flsh
 * Date: 25.05.2020.
 */
public interface IEnemyDestroyed extends ITransportObject, IServerMessage {
    long getEnemyId();

    int getReason();
}
