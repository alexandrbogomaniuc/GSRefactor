package com.dgphoenix.casino.kafka.dto;

public class SitInResponseDto extends BasicKafkaResponse {
    private long gameSessionId;
    private long amount;
    private long balance;
    private long playerRoundId;

    public SitInResponseDto() {}

    public SitInResponseDto(long gameSessionId,
            long amount,
            long balance,
            long playerRoundId,
            boolean success,
            int errorCode,
            String errorDetails) {
        super(success, errorCode, errorDetails);
        this.gameSessionId = gameSessionId;
        this.amount = amount;
        this.balance = balance;
        this.playerRoundId = playerRoundId;
    }

    public SitInResponseDto(boolean success, int errorCode, String errorDetails) {
        super(success, errorCode, errorDetails);
        this.gameSessionId = -1;
        this.amount = -1;
        this.balance = -1;
        this.playerRoundId = -1;
    }

    public long getGameSessionId() {
        return gameSessionId;
    }

    public long getAmount() {
        return amount;
    }

    public long getBalance() {
        return balance;
    }

    public long getPlayerRoundId() {
        return playerRoundId;
    }

    public void setGameSessionId(long gameSessionId) {
        this.gameSessionId = gameSessionId;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public void setBalance(long balance) {
        this.balance = balance;
    }

    public void setPlayerRoundId(long playerRoundId) {
        this.playerRoundId = playerRoundId;
    }

}
