package com.betsoft.casino.bots.mqb;

/**
 * User: flsh
 * Date: 13.07.2022.
 */
public class GetBalancesResponse {
    private String status;
    private long balance;

    public GetBalancesResponse() {
    }

    public GetBalancesResponse(String status, long balance) {
        this.status = status;
        this.balance = balance;
    }

    public String getStatus() {
        return status;
    }

    public long getBalance() {
        return balance;
    }

    public boolean isSuccess() {
        return "ok".equalsIgnoreCase(status) || "successful".equalsIgnoreCase(status);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("GetBalancesResponse [");
        sb.append("status='").append(status).append('\'');
        sb.append(", balance=").append(balance);
        sb.append(']');
        return sb.toString();
    }
}
