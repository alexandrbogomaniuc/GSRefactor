package com.betsoft.casino.mp.bgsectorx.model.math;

import com.betsoft.casino.mp.model.IEnemyPrize;

public enum EnemyPrize implements IEnemyPrize {
    PRIZE_1,
    PRIZE_2,
    PRIZE_3,
    PRIZE_4,
    PRIZE_5,
    PRIZE_6,
    PRIZE_7;

    @Override
    public int getOrdinalValue() {
        return ordinal();
    }
}
