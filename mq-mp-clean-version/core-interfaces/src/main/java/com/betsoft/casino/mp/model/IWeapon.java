package com.betsoft.casino.mp.model;

/**
 * User: flsh
 * Date: 20.08.18.
 */
public interface IWeapon {
    int getShots();

    void setShots(int shots);

    void addShots(int shots);

    SpecialWeaponType getType();
}
