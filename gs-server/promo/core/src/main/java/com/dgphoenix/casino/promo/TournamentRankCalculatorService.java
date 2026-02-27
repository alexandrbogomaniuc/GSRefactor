package com.abs.casino.promo;

import com.dgphoenix.casino.cassandra.CassandraPersistenceManager;
import com.dgphoenix.casino.common.cache.data.account.AccountInfo;
import com.dgphoenix.casino.common.currency.ICurrencyRateManager;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.promo.*;
import com.dgphoenix.casino.common.transactiondata.ITransactionData;
import com.abs.casino.promo.persisters.CassandraMaxBalanceTournamentPersister;
import org.springframework.stereotype.Service;

@Service
public class TournamentRankCalculatorService {
    private static final NotAvailableStatisticsProvider NA_STATISTICS_PROVIDER = new NotAvailableStatisticsProvider();
    private final CassandraMaxBalanceTournamentPersister maxBalanceTournamentPersister;
    private final ICurrencyRateManager currencyRateManager;

    public TournamentRankCalculatorService(CassandraPersistenceManager persistenceManager, ICurrencyRateManager currencyRateManager) {
        this.maxBalanceTournamentPersister = persistenceManager.getPersister(CassandraMaxBalanceTournamentPersister.class);
        this.currencyRateManager = currencyRateManager;
    }

    public void calculate(IParticipantEvent event, PromoCampaignMember member, IPrize prize, DesiredPrize desiredPrize, IPromoCampaign campaign,
                          AccountInfo account, ITransactionData transactionData) throws CommonException {
        calculateOld(event, member, desiredPrize, campaign, account, transactionData);
    }

    private void calculateOld(IParticipantEvent event, PromoCampaignMember member, DesiredPrize desiredPrize, IPromoCampaign campaign,
                              AccountInfo account, ITransactionData transactionData) throws CommonException {
        MaxBalanceTournamentPlayerDetails playerDetails = maxBalanceTournamentPersister.getForAccount(account.getId(), campaign.getId());
        TournamentMemberRank previousRank = getPreviousRank(event);
        TournamentObjective objective = ((ITournamentPromoTemplate) campaign.getTemplate()).getObjective();
        long score = objective.getScore(desiredPrize, currencyRateManager, account.getCurrency().getCode(), campaign.getBaseCurrency(),
                getStatisticsProvider(event, member), previousRank, playerDetails, event, getScoreMultiplier(campaign), null);
        String nickname = getNickname(campaign, member, playerDetails);
        TournamentMemberRank currentRank = new TournamentMemberRank(campaign.getId(), score, member.getAccountId(),
                account.getBankId(), account.getExternalId(), nickname, desiredPrize.getTotalQualifiedBetsCount(),
                desiredPrize.getTotalQualifiedBetSum(), desiredPrize.getTotalQualifiedWinSum(), 0,
                System.currentTimeMillis(), member.getEnterTime());
        if (previousRank != null) {
            currentRank.setRoundStats(previousRank.getRoundStats());
        }
        TournamentMemberRanks ranks = TournamentRanksExtractor.extractRanksFromTD(transactionData);
        ranks.addRank(currentRank);
    }

    private int getScoreMultiplier(IPromoCampaign campaign) {
        return 1;
    }

    private TournamentMemberRank getPreviousRank(IParticipantEvent event) {
        return ((AbstractParticipantEvent) event).getTournamentMemberRank();
    }

    private ICampaignStatisticsProvider getStatisticsProvider(IParticipantEvent event, PromoCampaignMember member) {
        ICampaignStatisticsProvider provider = event instanceof ICampaignStatisticsProvider
                ? (ICampaignStatisticsProvider) event : NA_STATISTICS_PROVIDER;
        return new BetWinSumStatisticsProviderWrapper(provider, member.getTotalBetSum(), member.getTotalWinSum());
    }

    private String getNickname(IPromoCampaign campaign, PromoCampaignMember member, MaxBalanceTournamentPlayerDetails playerDetails) {
        String nickname;
        if (campaign.isNetworkPromoCampaign() || campaign instanceof NetworkPromoEvent) {
            nickname = playerDetails.getNickname();
        } else {
            nickname = member.getDisplayName();
        }
        return nickname;
    }

    static class BetWinSumStatisticsProviderWrapper implements ICampaignStatisticsProvider {
        private final ICampaignStatisticsProvider provider;
        private final long betSum;
        private final long winSum;

        public BetWinSumStatisticsProviderWrapper(ICampaignStatisticsProvider provider, long betSum, long winSum) {
            this.provider = provider;
            this.betSum = betSum;
            this.winSum = winSum;
        }

        @Override
        public double getAverageBet() {
            return provider.getAverageBet();
        }

        @Override
        public long getMinBet() {
            return provider.getMinBet();
        }

        @Override
        public long getMaxBet() {
            return provider.getMaxBet();
        }

        @Override
        public int getMaxExposure() {
            return provider.getMaxExposure();
        }

        @Override
        public double getHighestWinPerSingleBet() {
            return provider.getHighestWinPerSingleBet();
        }

        @Override
        public long getBetSum() {
            return betSum;
        }

        @Override
        public long getWinSum() {
            return winSum;
        }

        @Override
        public String toString() {
            return "BetWinSumStatisticsProviderWrapper [" + "provider=" + provider +
                    ", betSum=" + betSum +
                    ", winSum=" + winSum +
                    ']';
        }
    }
}
