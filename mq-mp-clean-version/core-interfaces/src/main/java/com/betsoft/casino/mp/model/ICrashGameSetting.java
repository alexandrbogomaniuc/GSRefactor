package com.betsoft.casino.mp.model;

/**
 * User: flsh
 * Date: 19.04.2022.
 */
public interface ICrashGameSetting {
    long getBankId();

    String getCurrencyCode();

    int getMaxRoomPlayers();

    void setMaxRoomPlayers(int maxRoomPlayers);
    double getMaxMultiplier();

    long getMaxPlayerProfitInRound();

    long getTotalPlayersProfitInRound();

    void setMaxMultiplier(double maxMultiplier);

    void setMaxPlayerProfitInRound(long maxPlayerProfitInRound);

    void setTotalPlayersProfitInRound(long totalPlayersProfitInRound);

    long getMinStake();

    void setMinStake(long minStake);

    long getMaxStake();

    void setMaxStake(long maxStake);

    boolean isSendRealBetWin();

    void setSendRealBetWin(boolean sendRealBetWin);
}
