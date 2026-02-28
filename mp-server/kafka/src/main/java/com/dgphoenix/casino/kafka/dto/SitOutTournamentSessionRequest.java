package com.abs.casino.kafka.dto;

public class SitOutTournamentSessionRequest implements KafkaRequest {
    private long accountId;
    private String sessionId;
    private long gameSessionId;
    private long tournamentId;
    private long balance;
    private MQDataDto data;
    private RoundInfoResultDto roundInfo;
    private long roundId;

    public SitOutTournamentSessionRequest() {}

    public SitOutTournamentSessionRequest(long accountId,
            String sessionId,
            long gameSessionId,
            long tournamentId,
            long balance,
            MQDataDto data,
            RoundInfoResultDto roundInfo,
            long roundId) {
        this.accountId = accountId;
        this.sessionId = sessionId;
        this.gameSessionId = gameSessionId;
        this.tournamentId = tournamentId;
        this.balance = balance;
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

    public long getTournamentId() {
        return tournamentId;
    }

    public long getBalance() {
        return balance;
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

    public void setTournamentId(long tournamentId) {
        this.tournamentId = tournamentId;
    }

    public void setBalance(long balance) {
        this.balance = balance;
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
