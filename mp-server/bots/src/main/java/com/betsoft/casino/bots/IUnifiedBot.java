package com.betsoft.casino.bots;

public interface IUnifiedBot extends ILobbyBot, IRoomBot {

    void setCurrentMultiplier(double currentMultiplier);
    void incrementAstronaut();

    int getAstronautsCount();

    @Override
    default boolean isUsualActionBot() {
        return false;
    }

}
