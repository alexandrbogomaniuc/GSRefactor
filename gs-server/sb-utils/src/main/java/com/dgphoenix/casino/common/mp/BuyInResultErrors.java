package com.abs.casino.common.mp;

/**
 * User: flsh
 * Date: 27.03.2020.
 */
public enum BuyInResultErrors {
    ACCOUNT_NOT_FOUND(1, "Account not found"),
    TRANSACTION_IN_PROGRESS(2, "Transaction in progress"),
    GAME_SESSION_NOT_FOUND(3, "GameSession not found"),
    PREV_OPERATION_NOT_COMPLETED(4, "Previous operation is not completed"),
    TRANSACTION_NOT_FOUND(0, "Transaction not found");
    private int code;
    private String description;

    BuyInResultErrors(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("BuyInResultErrors [");
        sb.append("code=").append(code);
        sb.append(", description='").append(description).append('\'');
        sb.append(']');
        return sb.toString();
    }
}
