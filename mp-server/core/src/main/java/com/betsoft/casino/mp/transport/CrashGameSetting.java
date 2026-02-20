package com.betsoft.casino.mp.transport;

import com.betsoft.casino.mp.model.ICrashGameSetting;

import java.util.StringJoiner;

/**
 * User: flsh
 * Date: 19.04.2022.
 */
public class CrashGameSetting implements ICrashGameSetting {
    private final long bankId;
    private final String currencyCode;
    private int maxRoomPlayers;
    private double maxMultiplier;
    private long maxPlayerProfitInRound;
    private long totalPlayersProfitInRound;
    private long minStake;
    private long maxStake;
    private boolean sendRealBetWin;

    public CrashGameSetting(long bankId, String currencyCode, int maxRoomPlayers, double maxMultiplier, long maxPlayerProfitInRound,
                            long totalPlayersProfitInRound, long minStake, long maxStake, boolean sendRealBetWin) {
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

    @Override
    public long getBankId() {
        return bankId;
    }

    @Override
    public String getCurrencyCode() {
        return currencyCode;
    }

    @Override
    public int getMaxRoomPlayers() {
        return maxRoomPlayers;
    }

    @Override
    public double getMaxMultiplier() {
        return maxMultiplier;
    }

    @Override
    public void setMaxRoomPlayers(int maxRoomPlayers) {
        this.maxRoomPlayers = maxRoomPlayers;
    }
    @Override
    public long getMaxPlayerProfitInRound() {
        return maxPlayerProfitInRound;
    }

    @Override
    public long getTotalPlayersProfitInRound() {
        return totalPlayersProfitInRound;
    }

    @Override
    public void setMaxMultiplier(double maxMultiplier) {
        this.maxMultiplier = maxMultiplier;
    }

    @Override
    public void setMaxPlayerProfitInRound(long maxPlayerProfitInRound) {
        this.maxPlayerProfitInRound = maxPlayerProfitInRound;
    }

    @Override
    public void setTotalPlayersProfitInRound(long totalPlayersProfitInRound) {
        this.totalPlayersProfitInRound = totalPlayersProfitInRound;
    }

    @Override
    public long getMinStake() {
        return minStake;
    }

    @Override
    public void setMinStake(long minStake) {
        this.minStake = minStake;
    }

    @Override
    public long getMaxStake() {
        return maxStake;
    }

    @Override
    public void setMaxStake(long maxStake) {
        this.maxStake = maxStake;
    }

    @Override
    public boolean isSendRealBetWin() {
        return sendRealBetWin;
    }

    @Override
    public void setSendRealBetWin(boolean sendRealBetWin) {
        this.sendRealBetWin = sendRealBetWin;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", CrashGameSetting.class.getSimpleName() + "[", "]")
                .add("bankId=" + bankId)
                .add("currencyCode='" + currencyCode + "'")
                .add("maxRoomPlayers=" + maxRoomPlayers)
                .add("maxMultiplier=" + maxMultiplier)
                .add("maxPlayerProfitInRound=" + maxPlayerProfitInRound)
                .add("totalPlayersProfitInRound=" + totalPlayersProfitInRound)
                .add("minStake=" + minStake)
                .add("maxStake=" + maxStake)
                .add("sendRealBetWin=" + sendRealBetWin)
                .toString();
    }
}
