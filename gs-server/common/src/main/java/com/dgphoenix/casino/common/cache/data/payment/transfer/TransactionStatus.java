package com.abs.casino.common.cache.data.payment.transfer;

/**
 * User: flsh
 * Date: 11.08.2009
 */
public enum TransactionStatus {
    STARTED("Started"),
    PENDING("Pending"),
    APPROVED("Approved"),
    FAILED("Failed");

    private final String name;

    TransactionStatus(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
