package com.betsoft.casino.mp.model;

import java.util.List;

/**
 * User: flsh
 * Date: 15.02.19.
 */
public interface ITreasureGroup {
    List<ITreasure> getTreasures();

    int getOrdinalValue();
}
