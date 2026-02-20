package com.betsoft.casino.mp.model;

import com.betsoft.casino.utils.ITransportObject;

import java.util.List;

/**
 * User: flsh
 * Date: 25.05.2020.
 */
public interface IRoundResult<SEAT extends ITransportSeat, WEAPON_SUR extends IWeaponSurplus, LEVEL extends ILevelInfo>
        extends ITransportObject {
    double getWinAmount();
    void setWinAmount(double winAmount);

    double getWinRebuyAmount();
    void setWinRebuyAmount(double winRebuyAmount);

    long getBalance();

    long getCurrentScore();

    long getTotalScore();

    int getHitCount();

    int getMissCount();

    int getNextMapId();

    List<SEAT> getSeats();

    int getEnemiesKilledCount();

    long getWinAmountInCredits();

    long getUnusedBulletsCount();

    double getUnusedBulletsMoney();

    double getTotalBuyInMoney();

    long getXpPrev();

    List<WEAPON_SUR> getWeaponSurplus();

    LEVEL getBeforeRound();

    LEVEL getAfterRound();

    long getTotalKillsXP();

    int getTotalTreasuresCount();

    long getTotalTreasuresXP();
    long getSurplusHvBonus();
    int getQuestsCompletedCount();
    long getQuestsPayouts();
    long getRoundId();
    int getBulletsFired();
    void setCrashMultiplier(Double crashMultiplier);
    Double getCrashMultiplier();

    double getRealWinAmount();
    void setRealWinAmount(double realWinAmount);

    IRoundResult copy();
}
