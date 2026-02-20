package com.betsoft.casino.bots.mqb;

/**
 * User: flsh
 * Date: 13.07.2022.
 */
public class LoginResponse {
    private String status;
    private String startGameUrl;
    private long balance;
    private String token;
    private String localId;

    public LoginResponse() {
    }

    public LoginResponse(String status, String startGameUrl, long balance, String token, String localId) {
        this.status = status;
        this.startGameUrl = startGameUrl;
        this.balance = balance;
        this.token = token;
        this.localId = localId;
    }

    public String getStatus() {
        return status;
    }

    public String getStartGameUrl() {
        return startGameUrl;
    }

    public long getBalance() {
        return balance;
    }

    public boolean isSuccess() {
        return "ok".equalsIgnoreCase(status) || "successful".equalsIgnoreCase(status);
    }

    public String getToken() {
        return token;
    }

    public String getLocalId() {
        return localId;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("LoginResponse [");
        sb.append("status='").append(status).append('\'');
        sb.append(", startGameUrl='").append(startGameUrl).append('\'');
        sb.append(", balance=").append(balance);
        sb.append(", token='").append(token).append('\'');
        sb.append(", localId='").append(localId).append('\'');
        sb.append(']');
        return sb.toString();
    }
}
