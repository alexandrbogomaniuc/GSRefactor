package com.betsoft.casino.mp.model;

public class SitInResult implements ISitInResult {
    private long gameSessionId;
    private long balance;
    private long buyInAmount;
    private long playerRoundId;

    public SitInResult(long gameSessionId, long balance, long buyInAmount, long playerRoundId) {
        this.gameSessionId = gameSessionId;
        this.balance = balance;
        this.buyInAmount = buyInAmount;
        this.playerRoundId = playerRoundId;
    }

    @Override
    public long getGameSessionId() {
        return gameSessionId;
    }

    @Override
    public void setGameSessionId(long gameSessionId) {
        this.gameSessionId = gameSessionId;
    }

    @Override
    public long getBuyInAmount() {
        return buyInAmount;
    }

    @Override
    public void setBuyInAmount(long buyInAmount) {
        this.buyInAmount = buyInAmount;
    }

    @Override
    public long getBalance() {
        return balance;
    }

    @Override
    public void setBalance(long balance) {
        this.balance = balance;
    }

    @Override
    public long getPlayerRoundId() {
        return playerRoundId;
    }

    @Override
    public void setPlayerRoundId(long playerRoundId) {
        this.playerRoundId = playerRoundId;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SitInResult [");
        sb.append("gameSessionId=").append(gameSessionId);
        sb.append(", balance=").append(balance);
        sb.append(", buyInAmount=").append(buyInAmount);
        sb.append(", playerRoundId=").append(playerRoundId);
        sb.append(']');
        return sb.toString();
    }
}
