package com.dgphoenix.casino.gamecombos;


public enum ComboFeature {
    BUY_FEATURE("Buy Free Spins"),
    FREE_SPINS ("Free Spins"),
    RESPIN     ("Respin"),
    BONUS      ("Bonus"),
    CLICK_ME   ("Click Me"),
    JACKPOT    ("Jackpot"),
    DOUBLE_UP  ("Double Up"),
    UNKNOWN    ("Unknown feature");

    private String title;

    ComboFeature(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
