package com.betsoft.casino.mp.clashofthegods.model.math;

import com.betsoft.casino.mp.model.IEnemyPrize;

public enum EnemyPrize implements IEnemyPrize {
    Prize_1,
    Prize_2,
    Prize_3,
    Prize_4,
    Prize_5,
    Prize_6,
    Prize_7;

    @Override
    public int getOrdinalValue() {
        return ordinal();
    }
}
