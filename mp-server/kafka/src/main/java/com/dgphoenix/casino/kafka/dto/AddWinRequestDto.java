package com.dgphoenix.casino.kafka.dto;

import java.util.Map;

public class AddWinRequestDto implements KafkaRequest {
    private String sessionId;
    private long gameSessionId;
    private long cents;
    private long returnedBet;
    private long accountId;
    private RoundInfoResultDto roundInfo;
    private Map<Long, Double> contributions;
    private long gsRoundId;
    private long gsRoomId;
    private boolean sitOut;

    public AddWinRequestDto() {}

    public AddWinRequestDto(String sessionId,
            long gameSessionId,
            long cents,
            long returnedBet,
            long accountId,
            RoundInfoResultDto roundInfo,
            Map<Long, Double> contributions,
            long gsRoundId,
            long gsRoomId,
            boolean sitOut) {
        super();
        this.sessionId = sessionId;
        this.gameSessionId = gameSessionId;
        this.cents = cents;
        this.returnedBet = returnedBet;
        this.accountId = accountId;
        this.roundInfo = roundInfo;
        this.contributions = contributions;
        this.gsRoundId = gsRoundId;
        this.gsRoomId = gsRoomId;
        this.sitOut = sitOut;
    }

    public String getSessionId() {
        return sessionId;
    }

    public long getGameSessionId() {
        return gameSessionId;
    }

    public long getCents() {
        return cents;
    }

    public long getReturnedBet() {
        return returnedBet;
    }

    public long getAccountId() {
        return accountId;
    }

    public RoundInfoResultDto getRoundInfo() {
        return roundInfo;
    }

    public Map<Long, Double> getContributions() {
        return contributions;
    }

    public long getGsRoundId() {
        return gsRoundId;
    }

    public long getGsRoomId() {
        return gsRoomId;
    }

    public boolean isSitOut() {
        return sitOut;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public void setGameSessionId(long gameSessionId) {
        this.gameSessionId = gameSessionId;
    }

    public void setCents(long cents) {
        this.cents = cents;
    }

    public void setReturnedBet(long returnedBet) {
        this.returnedBet = returnedBet;
    }

    public void setAccountId(long accountId) {
        this.accountId = accountId;
    }

    public void setRoundInfo(RoundInfoResultDto roundInfo) {
        this.roundInfo = roundInfo;
    }

    public void setContributions(java.util.Map<Long, Double> contributions) {
        this.contributions = contributions;
    }

    public void setGsRoundId(long gsRoundId) {
        this.gsRoundId = gsRoundId;
    }

    public void setGsRoomId(long gsRoomId) {
        this.gsRoomId = gsRoomId;
    }

    public void setSitOut(boolean sitOut) {
        this.sitOut = sitOut;
    }

    @Override
    public String toString() {
        return "AddWinRequestDto [sessionId=" + sessionId + ", gameSessionId=" + gameSessionId
                + ", cents=" + cents + ", returnedBet=" + returnedBet + ", accountId=" + accountId
                + ", roundInfo=" + roundInfo + ", contributions=" + contributions + ", gsRoundId="
                + gsRoundId + ", gsRoomId=" + gsRoomId + ", sitOut=" + sitOut + "]";
    }
}
