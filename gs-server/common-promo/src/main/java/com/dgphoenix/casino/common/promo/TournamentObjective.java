package com.abs.casino.common.promo;

import com.abs.casino.common.currency.ICurrencyRateManager;
import com.abs.casino.common.currency.IHistoricalCurrencyRateManager;
import com.abs.casino.common.exception.CommonException;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Set;

/**
 * User: flsh
 * Date: 11.01.17.
 */
public enum TournamentObjective {
    MAX_PERFORMANCE {
        @Override
        public Set<SignificantEventType> getSignificantEvents() {
            return mqEndRoundSignificantEvents;
        }

        @Override
        public long getScore(DesiredPrize desiredPrize, ICurrencyRateManager currencyRateManager, String playerCurrency,
                             String baseCurrency, ICampaignStatisticsProvider stat,
                             TournamentMemberRank prevMemberRank,
                             TournamentPlayerDetails details, IParticipantEvent event,
                             int scoreMultiplier, Long exchangeRateDate) throws CommonException {
            //score = (total bets) x factor1 x factor2
            //factor1 = 1/(1 + win/max_exposure), when win >= 0 ( player won )
            //factor1 = 1, when win < 0 ( player lost )
            //max_exposure is set by some heuristic rule.
            //factor2 = 1 + (average bet / average bet per round for all players) / (max possible bet per round/min possible bet per round)
            double betSumInBaseCurrency = currencyRateManager
                    .convert(desiredPrize.getQualifiedBetSum(), playerCurrency, baseCurrency);
            double winSumInBaseCurrency = currencyRateManager
                    .convert(desiredPrize.getQualifiedWinSum(), playerCurrency, baseCurrency);
            double payout = winSumInBaseCurrency - betSumInBaseCurrency;
            double factor1 = 1;
            if (payout > 0) {
                int maxExposure = stat.getMaxExposure() <= 0 ? 1 : stat.getMaxExposure();
                factor1 = 1 / (1 + payout / maxExposure);
            }
            int qualifiedBetsCount = desiredPrize.getQualifiedBetsCount() <= 0 ?
                    1 : desiredPrize.getQualifiedBetsCount();
            double averageBet = betSumInBaseCurrency <= 0 ? 0 : betSumInBaseCurrency / qualifiedBetsCount;
            double minMaxBetRatio = (double) stat.getMaxBet() / stat.getMinBet();
            double factor2 = 1 + (averageBet / stat.getAverageBet() / minMaxBetRatio);
            double score = betSumInBaseCurrency * factor1 * factor2;
            return Math.round(score * scoreMultiplier);
        }
    },
    HIGHEST_WIN {
        @Override
        public Set<SignificantEventType> getSignificantEvents() {
            return mqEndRoundSignificantEvents;
        }

        @Override
        public long getScore(DesiredPrize desiredPrize, ICurrencyRateManager currencyRateManager, String playerCurrency,
                             String baseCurrency, ICampaignStatisticsProvider stat,
                             TournamentMemberRank prevMemberRank, TournamentPlayerDetails details, IParticipantEvent event,
                             int scoreMultiplier, Long exchangeRateDate) {
            long roundScore = Math.round(stat.getHighestWinPerSingleBet() * TournamentObjective.HIGHEST_WIN_MULTIPLIER);
            return prevMemberRank == null ? roundScore : Math.max(roundScore, prevMemberRank.getScore());
        }
    },
    CURRENT_TOURNAMENT_BALANCE {
        @Override
        public Set<SignificantEventType> getSignificantEvents() {
            return mqEndRoundSignificantEvents;
        }

        @Override
        public long getScore(DesiredPrize desiredPrize, ICurrencyRateManager currencyRateManager, String playerCurrency,
                             String baseCurrency, ICampaignStatisticsProvider stat, TournamentMemberRank prevMemberRank,
                             TournamentPlayerDetails details, IParticipantEvent event, int scoreMultiplier, Long exchangeRateDate)
                throws CommonException {
            long score = 0;
            if (details instanceof MaxBalanceTournamentPlayerDetails) {
                MaxBalanceTournamentPlayerDetails maxBalanceDetails = (MaxBalanceTournamentPlayerDetails) details;
                score = (long) currencyRateManager.convert(maxBalanceDetails.getCurrentBalance(), playerCurrency,
                        baseCurrency);
            }
            return score * scoreMultiplier;
        }
    },
    TOURNAMENT_MAX_BET_SUM {
        @Override
        public Set<SignificantEventType> getSignificantEvents() {
            return mqEndRoundSignificantEvents;
        }

        @Override
        public long getScore(DesiredPrize desiredPrize, ICurrencyRateManager currencyRateManager, String playerCurrency,
                             String baseCurrency, ICampaignStatisticsProvider stat, TournamentMemberRank prevMemberRank,
                             TournamentPlayerDetails details, IParticipantEvent event, int scoreMultiplier, Long exchangeRateDate) {
            long score = 0;
            if (details instanceof MaxBalanceTournamentPlayerDetails) {
                score = ((MaxBalanceTournamentPlayerDetails) details).getBetAmount();
            }
            return score * scoreMultiplier;
        }
    },
    MQ_NETWORK_TOURNAMENT {
        @Override
        public Set<SignificantEventType> getSignificantEvents() {
            return mqEndRoundSignificantEvents;
        }

        @Override
        public long getScore(DesiredPrize desiredPrize, ICurrencyRateManager currencyRateManager, String playerCurrency,
                             String baseCurrency, ICampaignStatisticsProvider stat, TournamentMemberRank prevMemberRank,
                             TournamentPlayerDetails details, IParticipantEvent event, int scoreMultiplier, Long exchangeRateDate)
                throws CommonException {
            //at this moment global network tournament score (and rating) not supported
            return 0;
        }
    },
    TOTAL_BET_SUM {
        @Override
        public Set<SignificantEventType> getSignificantEvents() {
            return betSignificantEvents;
        }

        @Override
        public long getScore(DesiredPrize desiredPrize, ICurrencyRateManager currencyRateManager, String playerCurrency,
                             String baseCurrency, ICampaignStatisticsProvider stat, TournamentMemberRank prevMemberRank,
                             TournamentPlayerDetails details, IParticipantEvent event, int scoreMultiplier, Long exchangeRateDate)
                throws CommonException {
            double betSumCurrency = convert(currencyRateManager, playerCurrency, baseCurrency, exchangeRateDate, stat.getBetSum());
            return Math.round(betSumCurrency * scoreMultiplier);
        }
    },
    TOTAL_WIN_SUM {
        @Override
        public Set<SignificantEventType> getSignificantEvents() {
            return winSignificantEvents;
        }

        @Override
        public long getScore(DesiredPrize desiredPrize, ICurrencyRateManager currencyRateManager, String playerCurrency,
                             String baseCurrency, ICampaignStatisticsProvider stat, TournamentMemberRank prevMemberRank,
                             TournamentPlayerDetails details, IParticipantEvent event, int scoreMultiplier, Long exchangeRateDate)
                throws CommonException {
            double winSumCurrency = convert(currencyRateManager, playerCurrency, baseCurrency, exchangeRateDate, stat.getWinSum());
            return Math.round(winSumCurrency * scoreMultiplier);

        }
    };
    public static final long HIGHEST_WIN_MULTIPLIER = 10000;
    public static final BigDecimal BD_HIGHEST_WIN_MULTIPLIER = BigDecimal.valueOf(HIGHEST_WIN_MULTIPLIER);
    private static final Set<SignificantEventType> betSignificantEvents;
    private static final Set<SignificantEventType> winSignificantEvents;
    private static final Set<SignificantEventType> mqEndRoundSignificantEvents;
    private static final Set<SignificantEventType> endRoundSignificantEvents;

    static {
        betSignificantEvents = Collections.singleton(SignificantEventType.BET);
        winSignificantEvents = Collections.singleton(SignificantEventType.WIN);
        mqEndRoundSignificantEvents = Collections.singleton(SignificantEventType.MQ_END_ROUND);
        endRoundSignificantEvents = Collections.singleton(SignificantEventType.END_ROUND);
    }

    public abstract Set<SignificantEventType> getSignificantEvents();
    public abstract long getScore(DesiredPrize desiredPrize, ICurrencyRateManager currencyRateManager,
                                  String playerCurrency, String baseCurrency, ICampaignStatisticsProvider stat,
                                  TournamentMemberRank prevMemberRank, TournamentPlayerDetails details, IParticipantEvent event,
                                  int scoreMultiplier, Long exchangeRateDate)
            throws CommonException;

    private static double convert(ICurrencyRateManager currencyRateManager, String playerCurrency,
                           String baseCurrency, Long exchangeRateDate, long amount) throws CommonException {
        if (currencyRateManager instanceof IHistoricalCurrencyRateManager) {
            IHistoricalCurrencyRateManager historicalCurrencyRateManager = (IHistoricalCurrencyRateManager) currencyRateManager;
            return historicalCurrencyRateManager.convert(amount, exchangeRateDate, playerCurrency, baseCurrency);
        } else {
            return currencyRateManager.convert(amount, playerCurrency, baseCurrency);
        }
    }
}
