package com.dgphoenix.casino.common.promo;

import com.dgphoenix.casino.common.currency.CurrencyRate;
import com.dgphoenix.casino.common.currency.ICurrencyRateManager;
import com.dgphoenix.casino.common.exception.CommonException;
import junit.framework.Assert;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;

/**
 * User: flsh
 * Date: 19.09.2019.
 */
public class MaxPerformanceTournamentTest {
    private static final ICurrencyRateManager currencyRateManager = new ICurrencyRateManager() {
        @Override
        public Collection<CurrencyRate> getCurrentRates() {
            return Collections.singleton(new CurrencyRate("EUR", "USD", 0.8, System.currentTimeMillis()));
        }

        @Override
        public double getRateToBaseCurrency(String sourceCurrency) throws CommonException {
            return 0.8;
        }

        @Override
        public double convert(double value, String sourceCurrency, String destinationCurrency) throws CommonException {
            return value;
        }
    };

    private static final ICampaignStatisticsProvider statProvider = new ICampaignStatisticsProvider() {
        @Override
        public double getAverageBet() {
            return 20;
        }

        @Override
        public long getMinBet() {
            return 1;
        }

        @Override
        public long getMaxBet() {
            return 100;
        }

        @Override
        public int getMaxExposure() {
            return 5000;
        }

        @Override
        public double getHighestWinPerSingleBet() {
            return 0;
        }

        @Override
        public long getBetSum() {
            return 1000;
        }

        @Override
        public long getWinSum() {
            return 900;
        }
    };

    @Test
    public void testZeroBet() throws CommonException {
        DesiredPrize prize = new DesiredPrize();
        prize.setQualifiedBetsCount(0);
        prize.setQualifiedBetSum(0);
        prize.setQualifiedWinSum(300);
        long score = getScore(prize);
        Assert.assertEquals(0, score);
    }

    @Test
    public void testZeroWin() throws CommonException {
        DesiredPrize prize = new DesiredPrize();
        prize.setQualifiedBetsCount(1);
        prize.setQualifiedBetSum(1);
        prize.setQualifiedWinSum(0);
        long score = getScore(prize);
        Assert.assertEquals(1, score);
    }

    @Test
    public void testZeroWinZeroBet() throws CommonException {
        DesiredPrize prize = new DesiredPrize();
        prize.setQualifiedBetsCount(0);
        prize.setQualifiedBetSum(0);
        prize.setQualifiedWinSum(0);
        long score = getScore(prize);
        System.out.println(score);
        Assert.assertEquals(0, score);
    }

    @Test
    public void testScoreForLooser() throws CommonException {
        DesiredPrize prize = new DesiredPrize();
        prize.setQualifiedBetsCount(10);
        prize.setQualifiedBetSum(400);
        prize.setQualifiedWinSum(300);
        long score = getScore(prize);
        Assert.assertEquals(408, score);
    }

    @Test
    public void testScoreForWinner() throws CommonException {
        DesiredPrize prize = new DesiredPrize();
        prize.setQualifiedBetsCount(10);
        prize.setQualifiedBetSum(400);
        prize.setQualifiedWinSum(500);
        long score = getScore(prize);
        Assert.assertEquals(400, score);
    }

    @Test
    public void testScoreForTwoLooserWithSameBet() throws CommonException {
        DesiredPrize prize = new DesiredPrize();
        prize.setQualifiedBetsCount(10);
        prize.setQualifiedBetSum(400);
        prize.setQualifiedWinSum(300);
        long score1 = getScore(prize);
        prize.setQualifiedWinSum(0);
        long score2 = getScore(prize);
        Assert.assertEquals(score1, score2);
    }

    @Test
    public void testScoreForTwoLooserWithDifferentBet() throws CommonException {
        DesiredPrize prize = new DesiredPrize();
        prize.setQualifiedBetsCount(10);
        prize.setQualifiedBetSum(400);
        prize.setQualifiedWinSum(300);
        long score1 = getScore(prize);
        prize.setQualifiedBetSum(500);
        long score2 = getScore(prize);
        Assert.assertTrue(score1 < score2);
    }

    @Test
    public void testScoreForTwoWinnersWithDifferentBet() throws CommonException {
        DesiredPrize prize = new DesiredPrize();
        prize.setQualifiedBetsCount(10);
        prize.setQualifiedBetSum(400);
        prize.setQualifiedWinSum(800);
        long score1 = getScore(prize);
        prize.setQualifiedBetSum(500);
        long score2 = getScore(prize);
        Assert.assertTrue(score1 < score2);
    }

    @Test
    public void testScoreForTwoWinnersWithSameBet() throws CommonException {
        DesiredPrize prize = new DesiredPrize();
        prize.setQualifiedBetsCount(10);
        prize.setQualifiedBetSum(400);
        prize.setQualifiedWinSum(800);
        long score1 = getScore(prize);
        prize.setQualifiedWinSum(900);
        long score2 = getScore(prize);
        Assert.assertTrue(score1 > score2);
    }

    private long getScore(DesiredPrize prize) throws CommonException {
        return TournamentObjective.MAX_PERFORMANCE.getScore(prize, currencyRateManager, "EUR", "EUR", statProvider, null, null, null, 1, null);
    }
}
