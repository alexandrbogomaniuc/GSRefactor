package com.abs.casino.gs.managers.payment.wallet;

public class CommonWalletStatusResult {
    private String extSystemTransactionId;
    private boolean isSuccess;

    public CommonWalletStatusResult(String extSystemTransactionId, boolean isSuccess) {
        this.extSystemTransactionId = extSystemTransactionId;
        this.isSuccess = isSuccess;

    }

    public String getExtSystemTransactionId() {
        return extSystemTransactionId;
    }

    public void setExtSystemTransactionId(String extSystemTransactionId) {
        this.extSystemTransactionId = extSystemTransactionId;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public void setSuccess(boolean success) {
        isSuccess = success;
    }
}
