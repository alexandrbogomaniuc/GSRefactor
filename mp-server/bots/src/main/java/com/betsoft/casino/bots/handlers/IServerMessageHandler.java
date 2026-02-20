package com.betsoft.casino.bots.handlers;

import com.betsoft.casino.utils.ITransportObject;

public interface IServerMessageHandler<T extends ITransportObject> {

    void handle(T response);
}
