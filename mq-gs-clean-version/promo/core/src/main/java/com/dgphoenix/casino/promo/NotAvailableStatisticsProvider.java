package com.dgphoenix.casino.promo;

import com.dgphoenix.casino.common.promo.ICampaignStatisticsProvider;

/**
 * @author <a href="mailto:fateyev@dgphoenix.com">Anton Fateyev</a>
 * @since 6/9/22
 */
public class NotAvailableStatisticsProvider implements ICampaignStatisticsProvider {
    @Override
    public double getAverageBet() {
        return 1;
    }

    @Override
    public long getMinBet() {
        return 1;
    }

    @Override
    public long getMaxBet() {
        return 2;
    }

    @Override
    public int getMaxExposure() {
        return 1;
    }

    @Override
    public double getHighestWinPerSingleBet() {
        return 0;
    }

    @Override
    public long getBetSum() {
        return 0;
    }

    @Override
    public long getWinSum() {
        return 0;
    }
}
