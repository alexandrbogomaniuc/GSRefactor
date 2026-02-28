package com.abs.casino.kafka.dto;

public class CrashGameSettingDto {
    private long bankId;
    private String currencyCode;
    private int maxRoomPlayers;
    private int maxMultiplier;
    private long maxPlayerProfitInRound;
    private long totalPlayersProfitInRound;
    private long minStake;
    private long maxStake;
    private boolean sendRealBetWin;

    public CrashGameSettingDto() {}

    public CrashGameSettingDto(long bankId,
            String currencyCode,
            int maxRoomPlayers,
            int maxMultiplier,
            long maxPlayerProfitInRound,
            long totalPlayersProfitInRound,
            long minStake,
            long maxStake,
            boolean sendRealBetWin) {
        this.bankId = bankId;
        this.currencyCode = currencyCode;
        this.maxRoomPlayers = maxRoomPlayers;
        this.maxMultiplier = maxMultiplier;
        this.maxPlayerProfitInRound = maxPlayerProfitInRound;
        this.totalPlayersProfitInRound = totalPlayersProfitInRound;
        this.minStake = minStake;
        this.maxStake = maxStake;
        this.sendRealBetWin = sendRealBetWin;
    }

    public long getBankId() {
        return bankId;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public int getMaxRoomPlayers() {
        return maxRoomPlayers;
    }

    public int getMaxMultiplier() {
        return maxMultiplier;
    }

    public long getMaxPlayerProfitInRound() {
        return maxPlayerProfitInRound;
    }

    public long getTotalPlayersProfitInRound() {
        return totalPlayersProfitInRound;
    }

    public long getMinStake() {
        return minStake;
    }

    public long getMaxStake() {
        return maxStake;
    }

    public boolean isSendRealBetWin() {
        return sendRealBetWin;
    }

    public void setBankId(long bankId) {
        this.bankId = bankId;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public void setMaxRoomPlayers(int maxRoomPlayers) {
        this.maxRoomPlayers = maxRoomPlayers;
    }

    public void setMaxMultiplier(int maxMultiplier) {
        this.maxMultiplier = maxMultiplier;
    }

    public void setMaxPlayerProfitInRound(long maxPlayerProfitInRound) {
        this.maxPlayerProfitInRound = maxPlayerProfitInRound;
    }

    public void setTotalPlayersProfitInRound(long totalPlayersProfitInRound) {
        this.totalPlayersProfitInRound = totalPlayersProfitInRound;
    }

    public void setMinStake(long minStake) {
        this.minStake = minStake;
    }

    public void setMaxStake(long maxStake) {
        this.maxStake = maxStake;
    }

    public void setSendRealBetWin(boolean sendRealBetWin) {
        this.sendRealBetWin = sendRealBetWin;
    }
}
