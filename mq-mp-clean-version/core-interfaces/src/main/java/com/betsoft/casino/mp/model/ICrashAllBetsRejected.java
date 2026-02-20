package com.betsoft.casino.mp.model;

import com.betsoft.casino.utils.IServerMessage;
import com.betsoft.casino.utils.ITransportObject;

public interface ICrashAllBetsRejected extends ITransportObject, IServerMessage {

    int getSeatId();

    String getName();

    Long getBalance();

    void setBalance(Long balance);

}
