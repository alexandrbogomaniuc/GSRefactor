package com.betsoft.casino.mp.model;

import com.abs.casino.common.cache.Identifiable;

/**
 * User: flsh
 * Date: 19.05.2020.
 */
public interface ITransportEnemy extends Identifiable {
    int getWidth();

    int getHeight();

    float getSpeed();

    int getPrizes();

    double getSumAward();

    int getSkins();

    boolean isBoss();
}
