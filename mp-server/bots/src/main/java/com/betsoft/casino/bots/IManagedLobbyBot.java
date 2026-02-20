package com.betsoft.casino.bots;

import com.betsoft.casino.bots.mqb.BotStatuses;

import static com.betsoft.casino.mp.model.bots.BotConfigInfo.DEFAULT_RATE;

public interface IManagedLobbyBot extends ILobbyBot{
    long getRoomId();

    IRoomBot getRoomBot();

    String getSessionId();

    void setSelectedBuyIn(Long buyIn);

    String getNickname();

    String getToken();

    BotStatuses getStatus();

    void sitOut();

    void confirmNextRoundPlay(long nextRoundId);

    default boolean isExpired() {
        return false;
    }

    default long getExpiresAt() { return Long.MAX_VALUE; }

    default double getShootsRate() {
        return DEFAULT_RATE;
    }

    default double getBulletsRate() {
        return DEFAULT_RATE;
    }
}
