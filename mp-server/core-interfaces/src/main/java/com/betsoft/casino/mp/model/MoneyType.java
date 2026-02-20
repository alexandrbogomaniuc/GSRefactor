package com.betsoft.casino.mp.model;

/**
 * User: flsh
 * Date: 09.11.17.
 */
public enum MoneyType {
    FREE,
    REAL,
    FRB,
    CASHBONUS,
    TOURNAMENT;

    public static MoneyType valueOf(int ordinal) {
        return values()[ordinal];
    }
}
