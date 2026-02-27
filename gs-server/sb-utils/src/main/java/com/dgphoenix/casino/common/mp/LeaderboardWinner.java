package com.abs.casino.common.mp;

public class LeaderboardWinner {
    private String bankId;
    private int place;
    private long score;
    private long winInCents;
    private String accountId;
    private String playerCurrency;
    private long winInPlayerCurrencyCents;
    private String transactionId;

    public LeaderboardWinner(String bankId, int place, long score, long winInCents, String accountId,
                             String playerCurrency, long winInPlayerCurrencyCents, String transactionId) {
        this.bankId = bankId;
        this.place = place;
        this.score = score;
        this.winInCents = winInCents;
        this.accountId = accountId;
        this.playerCurrency = playerCurrency;
        this.winInPlayerCurrencyCents = winInPlayerCurrencyCents;
        this.transactionId = transactionId;
    }

    public String getBankId() {
        return bankId;
    }

    public int getPlace() {
        return place;
    }

    public long getScore() {
        return score;
    }


    public long getWinInCents() {
        return winInCents;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getPlayerCurrency() {
        return playerCurrency;
    }

    public void setPlayerCurrency(String playerCurrency) {
        this.playerCurrency = playerCurrency;
    }

    public long getWinInPlayerCurrencyCents() {
        return winInPlayerCurrencyCents;
    }

    public void setWinInPlayerCurrencyCents(long winInPlayerCurrencyCents) {
        this.winInPlayerCurrencyCents = winInPlayerCurrencyCents;
    }

    public String getTransactionId() {
        return transactionId;
    }

    @Override
    public String toString() {
        return "LeaderboardWinner{" +
                "bankId=" + bankId +
                ", place=" + place +
                ", score=" + score +
                ", winInCents='" + winInCents + '\'' +
                ", accountId='" + accountId + '\'' +
                ", playerCurrency='" + playerCurrency + '\'' +
                ", winInPlayerCurrencyCents='" + winInPlayerCurrencyCents + '\'' +
                ", transactionId='" + transactionId + '\'' +
                '}';
    }
}
