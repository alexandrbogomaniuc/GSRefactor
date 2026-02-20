package com.betsoft.casino.mp.model;

import com.betsoft.casino.utils.IServerMessage;
import com.betsoft.casino.utils.ITransportObject;

public interface ICrashAllBets extends ITransportObject, IServerMessage {

    int getSeatId();

    String getName();

    long getBalance();

    long getAmount();
}
