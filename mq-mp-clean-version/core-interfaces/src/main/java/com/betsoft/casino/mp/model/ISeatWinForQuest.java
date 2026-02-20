package com.betsoft.casino.mp.model;

import com.betsoft.casino.utils.IServerMessage;
import com.betsoft.casino.utils.ITransportObject;

/**
 * User: flsh
 * Date: 26.05.2020.
 */
public interface ISeatWinForQuest extends ITransportObject, IServerMessage {
    long getSeatId();

    long getEnemyId();

    long getWinAmount();

    int getAwardedWeaponId();
}
