package com.dgphoenix.casino.common.cache.data.payment.transfer;

/**
 * User: flsh
 * Date: 11.08.2009
 */
public enum TransactionType {
    DEPOSIT("Deposit"),
    WITHDRAWAL("Withdrawal"),
    ADJUSTMENT("Adjustment"),
    BONUS("Bonus");

    private final String name;

    private TransactionType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
