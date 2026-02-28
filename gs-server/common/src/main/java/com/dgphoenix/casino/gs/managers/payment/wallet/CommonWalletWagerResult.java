package com.abs.casino.gs.managers.payment.wallet;

import com.dgphoenix.casino.common.util.string.StringUtils;

public class CommonWalletWagerResult {
    private String extSystemTransactionId;
    private double balance;
    private boolean isSuccess;
    private Double bonusBet;
    private Double bonusWin;
    private String responseCode;
    private boolean numericResponseCode;
    private String errorMessage;

    public CommonWalletWagerResult(String extSystemTransactionId, double balance, boolean isSuccess) {
        this.extSystemTransactionId = extSystemTransactionId;
        this.balance = balance;
        this.isSuccess = isSuccess;
    }

    public CommonWalletWagerResult(String extSystemTransactionId, double balance, boolean isSuccess, String responseCode) {
        this.extSystemTransactionId = extSystemTransactionId;
        this.balance = balance;
        this.isSuccess = isSuccess;
        this.responseCode = responseCode;
    }

    public CommonWalletWagerResult(String errorCode) {
        this.isSuccess = false;
        this.responseCode = errorCode;
        try {
            Long.parseLong(errorCode);
            this.numericResponseCode = true;
        } catch (NumberFormatException e) {
            // ignore
        }
    }

    public CommonWalletWagerResult(String errorCode, String errorMessage) {
        this(errorCode);
        this.errorMessage = errorMessage;
    }

    public CommonWalletWagerResult(int errorCode) {
        this.isSuccess = false;
        this.responseCode = String.valueOf(errorCode);
        this.numericResponseCode = true;
    }

    public CommonWalletWagerResult(String extSystemTransactionId, double balance, boolean success, Double bonusBet,
                                   Double bonusWin) {
        this.extSystemTransactionId = extSystemTransactionId;
        this.balance = balance;
        isSuccess = success;
        this.bonusBet = bonusBet;
        this.bonusWin = bonusWin;
    }

    public CommonWalletWagerResult(String extSystemTransactionId, double balance, boolean success, Double bonusBet,
                                   Double bonusWin, String responseCode) {
        this.extSystemTransactionId = extSystemTransactionId;
        this.balance = balance;
        isSuccess = success;
        this.bonusBet = bonusBet;
        this.bonusWin = bonusWin;
        this.responseCode = responseCode;
    }

    public String getExtSystemTransactionId() {
        return extSystemTransactionId;
    }

    public void setExtSystemTransactionId(String extSystemTransactionId) {
        this.extSystemTransactionId = extSystemTransactionId;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(long balance) {
        this.balance = balance;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public void setSuccess(boolean success) {
        isSuccess = success;
    }

    public Double getBonusBet() {
        return bonusBet;
    }

    public void setBonusBet(Double bonusBet) {
        this.bonusBet = bonusBet;
    }

    public Double getBonusWin() {
        return bonusWin;
    }

    public void setBonusWin(Double bonusWin) {
        this.bonusWin = bonusWin;
    }

    public String getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(String responseCode) {
        this.responseCode = responseCode;
    }

    public boolean isNumericResponseCode() {
        return numericResponseCode;
    }

    public boolean isHasResponseCode() {
        return !StringUtils.isTrimmedEmpty(responseCode);
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("CommonWalletWagerResult");
        sb.append("[extSystemTransactionId='").append(extSystemTransactionId).append('\'');
        sb.append(", balance=").append(balance);
        sb.append(", isSuccess=").append(isSuccess);
        sb.append(", responseCode='").append(responseCode).append('\'');
        sb.append(", bonusBet=").append(bonusBet);
        sb.append(", bonusWin=").append(bonusWin);
        sb.append(", errorMessage='").append(errorMessage).append('\'');
        sb.append(']');
        return sb.toString();
    }
}
