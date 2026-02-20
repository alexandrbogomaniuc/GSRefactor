package com.betsoft.casino.mp.model;

import com.betsoft.casino.utils.IServerMessage;
import com.betsoft.casino.utils.ITransportObject;

public interface IRoomManagerChanged extends ITransportObject, IServerMessage {
    int getRoomManager();
}
