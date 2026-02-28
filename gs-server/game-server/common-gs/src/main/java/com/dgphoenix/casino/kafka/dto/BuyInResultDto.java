package com.abs.casino.kafka.dto;

import com.dgphoenix.casino.kafka.dto.BasicKafkaResponse;

public class BuyInResultDto extends BasicKafkaResponse {
    private long amount;
    private long balance;
    private long playerRoundId;
    private long gameSessionId;

    public BuyInResultDto() {}

    public BuyInResultDto(boolean success, int statusCode, String reasonPhrases) {
        super(success, statusCode, reasonPhrases);
    }

    public BuyInResultDto(long amount,
            long balance,
            long playerRoundId,
            long gameSessionId,
            boolean success,
            int statusCode,
            String reasonPhrases) {
        super(success, statusCode, reasonPhrases);
        this.amount = amount;
        this.balance = balance;
        this.playerRoundId = playerRoundId;
        this.gameSessionId = gameSessionId;
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

    public long getGameSessionId() {
        return gameSessionId;
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

    public void setGameSessionId(long gameSessionId) {
        this.gameSessionId = gameSessionId;
    }

}
