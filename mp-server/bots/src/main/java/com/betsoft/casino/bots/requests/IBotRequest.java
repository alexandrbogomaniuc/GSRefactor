package com.betsoft.casino.bots.requests;

import com.betsoft.casino.utils.ITransportObject;

public interface IBotRequest {

    void send(int rid);

    void handle(ITransportObject response);

    default boolean isSingleResponse() {
        return true;
    }

    long getRequestStartTime();

    default long getElapsedTime() {
        return System.currentTimeMillis() - getRequestStartTime();
    }
}

