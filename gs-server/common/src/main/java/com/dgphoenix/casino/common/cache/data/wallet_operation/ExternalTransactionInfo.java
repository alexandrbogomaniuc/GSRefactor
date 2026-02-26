package com.abs.casino.common.cache.data.wallet_operation;

public class ExternalTransactionInfo {
    private double balance;
    private String refTransactionId;
    private String refTransactionDate;
    private String extraData;

    public ExternalTransactionInfo(double balance, String refTransactionId, String refTransactionDate, String extraData) {
        this.balance = balance;
        this.refTransactionId = refTransactionId;
        this.refTransactionDate = refTransactionDate;
        this.extraData = extraData;
    }

    public double getBalance() {
        return balance;
    }

    public String getRefTransactionId() {
        return refTransactionId;
    }

    public String getRefTransactionDate() {
        return refTransactionDate;
    }

    public String getExtraData() {
        return extraData;
    }
}