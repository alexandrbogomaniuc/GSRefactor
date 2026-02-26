package com.abs.casino.actions.api.promo;

/**
 * User: flsh
 * Date: 03.09.2019.
 */
public enum ErrorCodes {
    BANK_ID_NOT_SPECIFIED(1, "Bank id not specified"),
    CAMPAIGN_ID_NOT_SPECIFIED(2, "Campaign id not specified"),
    USER_NOT_SPECIFIED(3, "User not specified"),
    BANK_NOT_FOUND(4, "Bank not found"),
    CAMPAIGN_NOT_FOUND(5, "Campaign not found"),
    USER_NOT_FOUND(6, "User not found"),
    RANK_UNKNOWN(7, "Rank unknown"),
    UNEXPECTED_ERROR(8, "Unexpected error");

    private int code;
    private String description;

    ErrorCodes(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ErrorCodes [");
        sb.append("code=").append(code);
        sb.append(", description='").append(description).append('\'');
        sb.append(']');
        return sb.toString();
    }

}
