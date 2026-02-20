package com.betsoft.casino.mp.model;

import com.betsoft.casino.utils.IServerMessage;
import com.betsoft.casino.utils.ITransportObject;

/**
 * User: flsh
 * Date: 19.05.2020.
 */
public interface IMinePlace extends ITransportObject, IServerMessage {
    int getSeatId();

    void setSeatId(int seatId);

    float getX();

    void setX(float x);

    float getY();

    void setY(float y);

    String getMineId();

    void setMineId(String mineId);
}
