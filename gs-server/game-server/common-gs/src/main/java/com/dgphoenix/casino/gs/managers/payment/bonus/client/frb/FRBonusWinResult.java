package com.abs.casino.gs.managers.payment.bonus.client.frb;


public class FRBonusWinResult {

    private long balance;
    private boolean isSuccess;


    public FRBonusWinResult(long balance, boolean isSuccess) {
        this.balance = balance;
        this.isSuccess = isSuccess;
    }

    public long getBalance() {
        return balance;
    }

    public void setBalance(long balance) {
        this.balance = balance;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public void setSuccess(boolean success) {
        this.isSuccess = success;
    }
}
