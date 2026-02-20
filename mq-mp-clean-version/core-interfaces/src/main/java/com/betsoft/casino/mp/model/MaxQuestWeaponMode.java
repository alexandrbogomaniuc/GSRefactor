package com.betsoft.casino.mp.model;

public enum MaxQuestWeaponMode {
    LOOT_BOX, PAID_SHOTS;

    public static MaxQuestWeaponMode valueOf(int ordinal) {
        return values()[ordinal];
    }
}

