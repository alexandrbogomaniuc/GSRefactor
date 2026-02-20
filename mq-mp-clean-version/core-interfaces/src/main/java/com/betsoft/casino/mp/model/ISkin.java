package com.betsoft.casino.mp.model;

/**
 * User: flsh
 * Date: 19.05.2020.
 */
public interface ISkin {
    float getSpeed();

    void setSpeed(float speed);

    float getSpeedDeltaPositive();

    void setSpeedDeltaPositive(float speedDeltaPositive);

    float getSpeedDeltaNegative();

    void setSpeedDeltaNegative(float speedDeltaNegative);
}
