package com.betsoft.casino.mp.model;


/**
 * User: flsh
 * Date: 19.05.2020.
 */
public interface ITransportAsteroid {
    int getType();
    void setType(int type);

    Double getSpeed();
    void setSpeed(Double speed);

    Double getX();
    void setX(Double x);

    Double getY();
    void setY(Double y);

    Double getSlow();
    void setSlow(Double slow);
}
