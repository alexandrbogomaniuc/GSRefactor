package com.dgphoenix.casino.kafka.dto;

public class SaveCashBonusRoundResultRequest implements KafkaRequest {
    private long accountId;
    private String sessionId;
    private long gameSessionId;
    private long bonusId;
    private long balance;
    private long betSum;
    private MQDataDto data;
    private RoundInfoResultDto roundInfo;
    private long roundId;

    public SaveCashBonusRoundResultRequest() {}

    public SaveCashBonusRoundResultRequest(long accountId,
            String sessionId,
            long gameSessionId,
            long bonusId,
            long balance,
            long betSum,
            MQDataDto data,
            RoundInfoResultDto roundInfo,
            long roundId) {
        super();
        this.accountId = accountId;
        this.sessionId = sessionId;
        this.gameSessionId = gameSessionId;
        this.bonusId = bonusId;
        this.balance = balance;
        this.betSum = betSum;
        this.data = data;
        this.roundInfo = roundInfo;
        this.roundId = roundId;
    }

    public long getAccountId() {
        return accountId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public long getGameSessionId() {
        return gameSessionId;
    }

    public long getBonusId() {
        return bonusId;
    }

    public long getBalance() {
        return balance;
    }

    public long getBetSum() {
        return betSum;
    }

    public MQDataDto getData() {
        return data;
    }

    public RoundInfoResultDto getRoundInfo() {
        return roundInfo;
    }

    public long getRoundId() {
        return roundId;
    }

    public void setAccountId(long accountId) {
        this.accountId = accountId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public void setGameSessionId(long gameSessionId) {
        this.gameSessionId = gameSessionId;
    }

    public void setBonusId(long bonusId) {
        this.bonusId = bonusId;
    }

    public void setBalance(long balance) {
        this.balance = balance;
    }

    public void setBetSum(long betSum) {
        this.betSum = betSum;
    }

    public void setData(MQDataDto data) {
        this.data = data;
    }

    public void setRoundInfo(RoundInfoResultDto roundInfo) {
        this.roundInfo = roundInfo;
    }

    public void setRoundId(long roundId) {
        this.roundId = roundId;
    }
}
