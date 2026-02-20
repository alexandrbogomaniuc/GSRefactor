package com.betsoft.casino.mp.model;

import java.util.List;

/**
 * User: flsh
 * Date: 08.02.19.
 */
public interface IEnemyRange<TYPE extends IEnemyType> {
    List<TYPE> getEnemies();
}
