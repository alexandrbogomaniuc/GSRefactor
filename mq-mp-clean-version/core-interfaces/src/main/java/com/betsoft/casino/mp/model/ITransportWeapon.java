package com.betsoft.casino.mp.model;

import java.io.Serializable;

/**
 * User: flsh
 * Date: 25.05.2020.
 */
public interface ITransportWeapon extends Serializable {
    int getId();

    void setId(int id);

    int getShots();

    void setShots(int shots);

    int getSourceId();

    void setSourceId(int sourceId);
}
