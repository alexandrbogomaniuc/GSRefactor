package com.betsoft.casino.mp.model;

import com.betsoft.casino.utils.IServerMessage;
import com.betsoft.casino.utils.InboundObject;

/**
 * User: flsh
 * Date: 25.05.2020.
 */
public interface IBalanceUpdated extends InboundObject, IServerMessage {
    long getBalance();

    int getServerAmmo();
}
