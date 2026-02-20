package com.dgphoenix.casino.common.mp;

public class LeaderboardResultWrapper {

    private String bankId;
    private long leaderboardId;
    private long transactionId;
    private String hash;
    private LeaderboardResult result;

    public LeaderboardResultWrapper(String bankId, long leaderboardId, long transactionId, String hash, LeaderboardResult result) {
        this.bankId = bankId;
        this.leaderboardId = leaderboardId;
        this.transactionId = transactionId;
        this.hash = hash;
        this.result = result;
    }

    public String getBankId() {
        return bankId;
    }

    public long getLeaderboardId() {
        return leaderboardId;
    }

    public long getTransactionId() {
        return transactionId;
    }

    public String getHash() {
        return hash;
    }

    public LeaderboardResult getResult() {
        return result;
    }

    @Override
    public String toString() {
        return "LeaderboardResultWrapper{" +
                "bankId=" + bankId +
                ", leaderboardId=" + leaderboardId +
                ", transactionId=" + transactionId +
                ", hash='" + hash + '\'' +
                ", result=" + result +
                '}';
    }
}
