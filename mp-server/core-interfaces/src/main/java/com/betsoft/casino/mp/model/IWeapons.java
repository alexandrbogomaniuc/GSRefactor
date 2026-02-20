package com.betsoft.casino.mp.model;

import com.betsoft.casino.utils.ITransportObject;

import java.util.List;

/**
 * User: flsh
 * Date: 25.05.2020.
 */
public interface IWeapons<W extends ITransportWeapon> extends ITransportObject {
    int getAmmoAmount();

    boolean isFreeShots();

    List<W> getWeapons();
}
