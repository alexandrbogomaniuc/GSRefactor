package com.dgphoenix.casino.common.promo;

import com.abs.casino.common.cache.data.account.IAccountInfo;
import com.abs.casino.common.currency.ICurrencyRateManager;
import com.dgphoenix.casino.common.exception.CommonException;
import com.abs.casino.common.promo.feed.tournament.SummaryTournamentFeedEntry;
import com.abs.casino.common.util.DatePeriod;
import com.dgphoenix.casino.common.util.Pair;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * User: flsh
 * Date: 17.11.16.
 */
public interface IPromoCampaignManager {
    //localizedStrings: key in form keyname.lang, value is localized string,
    // for example:
    //              title.ru=xxxxx
    //              big_prize_msg.ru=yyyyy
    //              title.de=ddddd
    //              big_prize_msg.de=zzzzz
    //localizedStrings may be null, empty, or not contains all supported localizations
    IPromoCampaign create(IPromoTemplate<?, ?> template, String name, EnterType enterType, DatePeriod period,
                          Set<Long> bankIds, Set<Long> gameIds, String baseCurrency,
                          Map<String, String> localizedStrings, Map<Long, String> promoDetailURLs,
                          PlayerIdentificationType playerIdentificationType,
                          ISupportedPlatform supportedPlatform) throws CommonException;

    NetworkPromoEvent createNetworkEvent(long eventId, IPromoTemplate<?, ?> template, String name, EnterType enterType,
                                         DatePeriod period, Set<Long> bankIds, Set<Long> gameIds,
                                         String baseCurrency, Map<Long, String> promoDetailURLs,
                                         PlayerIdentificationType playerIdentificationType,
                                         ISupportedPlatform supportedPlatform, long networkPromoCampaignId)
            throws CommonException;

    void createSummaryTournamentFeed(long id, String feedUrl, String bankName, long startDate, long endDate,
                TournamentObjective type, long tournamentId);

    Map<String, List<SummaryTournamentFeedEntry>> getFeedEntriesForTournament(long tournamentId);

    void saveNetworkPromoCampaign(INetworkPromoCampaign<NetworkPromoEvent> networkPromoCampaign);

    void cancel(long campaignId, String cancelReason) throws CommonException;

    Set<IPromoCampaign> getTournamentsForMultiplayerGames(Long bankId, Long gameId, Status status,
                                                          IAccountInfo accountInfo) throws CommonException;

    Set<IPromoCampaign> getTournamentsForMultiplayerGames(long bankId, String currency) throws CommonException;

    NetworkPromoCampaign getNetworkPromoCampaign(long bankId) throws CommonException;

    Set<IPromoCampaign> getPromoCampaigns(Long bankId, Long gameId, Status status,
                                          IAccountInfo accountInfo) throws CommonException;

    Set<IPromoCampaign> getActive(long bankId, long gameId, Map<String, String> startGameParams,
                                  Long clientTypeId, IAccountInfo accountInfo) throws CommonException;

    Set<IPromoCampaign> getActive(long bankId);

    IPromoCampaign getPromoCampaign(long campaignId);

    IPromoCampaign getPromoCampaignFromPersister(long campaignId);

    Pair<IPromoCampaign, PromoCampaignMember> registerPlayer(long promoCampaignId, IAccountInfo account,
                                                             long gameSessionId, long gameId) throws CommonException;

    Map<IPromoCampaign, PromoCampaignMember> registerPlayerInPromos(Collection<Long> promoCampaignIds,
                                                                    IAccountInfo account, long gameSessionId,
                                                                    long gameId) throws CommonException;

    void finalizePromoForGameSession(long accountId, long gameSessionId) throws CommonException;

    void savePromoCampaignMember(PromoCampaignMember member);

    PromoCampaignMember getPromoCampaignMember(long promoCampaignId, long accountId);

    Set<PromoCampaignMember> getPromoCampaignMembers(long promoCampaignId);

    Set<PromoCampaignMember> getAllBankPromoCampaignMembers(long bankId);

    IPrizeWonHandlersFactory getWonHandlersFactory() throws CommonException;

    void invalidateCachedCampaign(long campaignId);

    void processPrizeWonFromExternalSide(long campaignId, long prizeId, long gameId, long amount, String currency)
            throws CommonException;

    //implementation must be silent, if cannot lock just log and return false;
    boolean qualifyConcurrentPrize(IPromoCampaign campaign, IConcurrentPromoTemplate<?, ?> template, IPrize campaignPrize,
                                   PromoCampaignMember member, DesiredPrize desiredPrize,
                                   ICurrencyRateManager currencyRateManager,
                                   String playerCurrency) throws CommonException;

    void registerPromoCampaignsObserver(IPromoCampaignsObserver promoCampaignsObserver);


    boolean isPlayerIncludedInPromo(IPromoCampaign campaign, IAccountInfo accountInfo);

    PromoCampaignMember getOrCreatePromoMember(IAccountInfo account, IPromoCampaign campaign) throws CommonException;
}
