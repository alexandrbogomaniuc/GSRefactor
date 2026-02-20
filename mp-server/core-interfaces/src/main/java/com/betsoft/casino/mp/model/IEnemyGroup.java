package com.betsoft.casino.mp.model;

import java.util.List;

/**
 * User: flsh
 * Date: 15.02.19.
 */
public interface IEnemyGroup {
    int getId();

    String getName();

    List<IEnemyType> getEnemies();

    int getOrdinalValue();
}
