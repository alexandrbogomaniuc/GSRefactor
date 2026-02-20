package com.dgphoenix.casino.common.promo;

/**
 * User: flsh
 * Date: 19.09.2019.
 */
public interface ICampaignStatisticsProvider {
    double getAverageBet();
    long getMinBet();
    long getMaxBet();
    int getMaxExposure();
    //this is highest win for single bet/shot divided to stake/coin
    double getHighestWinPerSingleBet();
    long getBetSum();
    long getWinSum();
}
