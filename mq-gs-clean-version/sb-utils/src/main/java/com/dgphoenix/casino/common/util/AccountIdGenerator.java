package com.dgphoenix.casino.common.util;

import com.dgphoenix.casino.common.configuration.CasinoSystemType;

/**
 * User: flsh
 * Date: 12/28/11
 */
public class AccountIdGenerator {
    //low 51 bit; dec:2.251.799.813.685.247, bin: 111111111111111111111111111111111111111111111111111
    public static final long MAX_ACCOUNT_VALUE = 0x0007ffffffffffffL;
    public static final long MAX_BANK_ID_VALUE = 0xfffL; // first bit is sign, next 12 bankId; dec: 4095, bin:111111111111

    public static long generate(long bankId, long accountId, CasinoSystemType type) {
        if (type.isUseSameIdForAccounts()) {
            return accountId;
        }
        return generateComposed(bankId, accountId);
    }

    //don't use this directly, use 'generate' method. protected visibility need only for junit tests in same package
    protected static long generateComposed(long bankId, long accountId) {
/*
        System.out.println("MAX_BANK_ID_VALUE=" + MAX_BANK_ID_VALUE + ", " + Long.toHexString(MAX_BANK_ID_VALUE) + ", "
                + Long.toBinaryString(MAX_BANK_ID_VALUE));
        System.out.println("MAX_ACCOUNT_VALUE=" + MAX_ACCOUNT_VALUE + ", " + Long.toHexString(MAX_ACCOUNT_VALUE) + ", "
                + Long.toBinaryString(MAX_ACCOUNT_VALUE));
        System.out.println("bankId=" + bankId + ", " + Long.toHexString(bankId) + ", " + Long.toBinaryString(bankId));
        System.out.println("accountId=" + accountId + ", " + Long.toHexString(accountId) + ", " +
                Long.toBinaryString(accountId));
*/
        if(bankId > MAX_BANK_ID_VALUE) {
            throw new IllegalArgumentException("bankId too large");
        }
        if(bankId <=0) {
            throw new IllegalArgumentException("bankId cannot be negative");
        }
        if(accountId > MAX_ACCOUNT_VALUE) {
            throw new IllegalArgumentException("accountId too large");
        }
        if(accountId <= 0) {
            throw new IllegalArgumentException("accountId cannot be negative");
        }

        long composedId = bankId << 51;
        //System.out.println("composedId=" + composedId + ", " + Long.toHexString(composedId) + ", " + Long.toBinaryString(composedId));
        composedId += accountId;
        //System.out.println("composedId=" + composedId + ", " + Long.toHexString(composedId) + ", " + Long.toBinaryString(composedId));
        return composedId;
    }

    public static long getExternalIdByAccountId(long accountId, CasinoSystemType type) {
        if (type.isUseSameIdForAccounts()) {
            return accountId;
        }
        return accountId & MAX_ACCOUNT_VALUE;
    }
}