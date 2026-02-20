package com.betsoft.casino.mp.model;

import com.betsoft.casino.utils.IServerMessage;
import com.betsoft.casino.utils.ITransportObject;

public interface IChangeEnemyMode extends ITransportObject, IServerMessage {
    long getEnemyId();
    int getEnemyModeId();
}
