package com.abs.casino.kafka.dto;

public class AddWinResultDto extends BasicKafkaResponse {
    private boolean playerOffline;
    private long balance;

    public AddWinResultDto() {
        super();
    }

    public AddWinResultDto(boolean success, int errorCode, String errorDetails) {
        super(success, errorCode, errorDetails);
    }

    public AddWinResultDto(boolean playerOffline,
            long balance,
            boolean success,
            int errorCode,
            String errorDetails) {
        super(success, errorCode, errorDetails);
        this.playerOffline = playerOffline;
        this.balance = balance;
    }

    public boolean isPlayerOffline() {
        return playerOffline;
    }

    public long getBalance() {
        return balance;
    }

    public void setPlayerOffline(boolean playerOffline) {
        this.playerOffline = playerOffline;
    }

    public void setBalance(long balance) {
        this.balance = balance;
    }

    @Override
    public String toString() {
        return "AddWinResultDto [playerOffline=" + playerOffline + ", balance=" + balance + "]";
    }

}
