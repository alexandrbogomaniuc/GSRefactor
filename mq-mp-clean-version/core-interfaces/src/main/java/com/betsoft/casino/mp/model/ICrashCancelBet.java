package com.betsoft.casino.mp.model;

import com.betsoft.casino.utils.IServerMessage;
import com.betsoft.casino.utils.ITransportObject;

public interface ICrashCancelBet extends ITransportObject, IServerMessage {
    double getCurrentMult();

    int getSeatId();

    long getSeatWin();

    String getCrashBetId();

    String getName();
}

