package com.betsoft.casino.mp.model;

import com.betsoft.casino.utils.InboundObject;

/**
 * User: flsh
 * Date: 19.05.2020.
 */
public interface IMineCoordinates extends InboundObject {
    float getX();

    float getY();

    boolean isPaidSpecialShot();

    void setPaidSpecialShot(boolean paidSpecialShot);
}
