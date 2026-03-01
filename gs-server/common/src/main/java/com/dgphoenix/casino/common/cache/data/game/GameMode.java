package com.abs.casino.common.cache.data.game;

import com.abs.casino.common.util.string.StringUtils;

/**
 * Created
 * Date: 28.11.2008
 * Time: 12:44:08
 */
public enum GameMode {
    REAL("Real", "real"),
    FREE("Fun", "free"),
    BONUS("Bonus", "real");

    private final String moneyType;
    private final String modePath;

    GameMode(String moneyType, String modePath) {
        this.moneyType = moneyType;
        this.modePath = modePath;
    }

    public String getMoneyType() {
        return moneyType;
    }

    public String getModePath() {
        return modePath;
    }

    public static GameMode getByName(String stringValueOfGameMode) {
        if (StringUtils.isTrimmedEmpty(stringValueOfGameMode)) {
            return FREE;
        }
        String mode = stringValueOfGameMode.trim();
        if (BONUS.name().equalsIgnoreCase(mode)) {
            return BONUS;
        } else if (REAL.name().equalsIgnoreCase(mode)) {
            return REAL;
        }
        return FREE;
    }
}
