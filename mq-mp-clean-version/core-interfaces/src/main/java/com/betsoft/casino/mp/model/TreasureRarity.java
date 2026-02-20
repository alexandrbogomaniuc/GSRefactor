package com.betsoft.casino.mp.model;

public enum TreasureRarity {

    COMMON(1),
    UNCOMMON(2),
    RARE(3),
    EPIC(4),
    LEGENDARY(5);

    private final int value;

    TreasureRarity(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
