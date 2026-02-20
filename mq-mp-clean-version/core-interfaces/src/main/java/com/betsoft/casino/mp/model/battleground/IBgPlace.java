package com.betsoft.casino.mp.model.battleground;

/**
 * User: flsh
 * Date: 17.07.2021.
 */
public interface IBgPlace {
    long getAccountId();

    long getWin();

    int getRank();

    void setRank(int rank);

    long getBetsSum();

    long getWinSum();

    long getGameSessionId();

    long getGameScore();

    double getEjectPoint();

    void setEjectPoint(double ejectPoint);
}
