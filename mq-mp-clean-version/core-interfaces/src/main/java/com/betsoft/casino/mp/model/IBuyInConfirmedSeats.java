package com.betsoft.casino.mp.model;

import com.betsoft.casino.utils.IServerMessage;
import com.betsoft.casino.utils.ITransportObject;

import java.util.List;

public interface IBuyInConfirmedSeats extends ITransportObject, IServerMessage {
    List<Integer> getConfirmedSeatsId();
}
