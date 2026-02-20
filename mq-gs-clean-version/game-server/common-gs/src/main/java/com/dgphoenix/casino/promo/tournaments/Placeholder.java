package com.dgphoenix.casino.promo.tournaments;

/** String constants for parameters to replace in tournament welcome message and rules */
public enum Placeholder {
    TOP_PRIZE_VALUE_PLAYER_CURRENCY("TOP_PRIZE_VALUE_PLAYER_CURRENCY"),
    TOP_PRIZE_VALUE_MAIN_CURRENCY("TOP_PRIZE_VALUE_MAIN_CURRENCY"),
    TOTAL_PRIZE_VALUE_PLAYER_CURRENCY ("TOTAL_PRIZE_VALUE_PLAYER_CURRENCY"),
    TOTAL_PRIZE_VALUE_MAIN_CURRENCY("TOTAL_PRIZE_VALUE_MAIN_CURRENCY"),
    MIN_BET_PLAYER_CURRENCY("MIN_BET_PLAYER_CURRENCY"),
    MIN_BET_MAIN_CURRENCY("MIN_BET_MAIN_CURRENCY"),
    MIN_BET_SUM_PLAYER_CURRENCY("MIN_BET_SUM_PLAYER_CURRENCY"),
    MIN_BET_SUM_MAIN_CURRENCY("MIN_BET_SUM_MAIN_CURRENCY"),
    MIN_SPINS("MIN_SPINS");

    /** prefix for placeholder identification in text */
    public static final String PREFIX = "<";
    /** suffix for placeholder identification in text */
    public static final String SUFFIX = ">";

    private String placeholderName;

    Placeholder(String placeholderName) {
        this.placeholderName = placeholderName;
    }

    public String getPlaceholderName() {
        return placeholderName;
    }
}
