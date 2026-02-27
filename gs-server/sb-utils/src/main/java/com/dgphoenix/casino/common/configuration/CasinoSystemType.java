package com.abs.casino.common.configuration;

/**
 * User: flsh
 * Date: 5/10/12
 */
public enum CasinoSystemType {
    MULTIBANK(false, false),
    SINGLE_BANK(true, false),
    SINGLE_BANK_SAME_ID(true, true);

    //same extId and internalId
    private boolean useSameIdForAccounts;
    private boolean singleBank;

    private CasinoSystemType(boolean singleBank, boolean useSameIdForAccounts) {
        this.singleBank = singleBank;
        this.useSameIdForAccounts = useSameIdForAccounts;
    }

    public boolean isUseSameIdForAccounts() {
        return useSameIdForAccounts;
    }

    public boolean isSingleBank() {
        return singleBank;
    }
}
