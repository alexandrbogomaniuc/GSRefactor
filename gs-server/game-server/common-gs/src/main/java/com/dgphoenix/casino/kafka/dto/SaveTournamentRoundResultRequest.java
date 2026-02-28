package com.abs.casino.kafka.dto;

import com.abs.casino.kafka.dto.RoundInfoResultDto;

import com.dgphoenix.casino.kafka.dto.MQDataDto;

import com.dgphoenix.casino.kafka.dto.KafkaRequest;

public class SaveTournamentRoundResultRequest implements KafkaRequest {
    private long accountId;
    private String sessionId;
    private long gameSessionId;
    private long tournamentId;
    private long balance;
    private MQDataDto data;
    private RoundInfoResultDto result;
    private long roundId;

    public SaveTournamentRoundResultRequest() {}

    public SaveTournamentRoundResultRequest(long accountId,
            String sessionId,
            long gameSessionId,
            long tournamentId,
            long balance,
            MQDataDto data,
            RoundInfoResultDto result,
            long roundId) {
        super();
        this.accountId = accountId;
        this.sessionId = sessionId;
        this.gameSessionId = gameSessionId;
        this.tournamentId = tournamentId;
        this.balance = balance;
        this.data = data;
        this.result = result;
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

    public RoundInfoResultDto getResult() {
        return result;
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

    public void setResult(RoundInfoResultDto result) {
        this.result = result;
    }

    public void setRoundId(long roundId) {
        this.roundId = roundId;
    }
}
