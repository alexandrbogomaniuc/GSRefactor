package com.dgphoenix.casino.promo;

import com.dgphoenix.casino.cassandra.CassandraPersistenceManager;
import com.dgphoenix.casino.cassandra.DistributedLockManager;
import com.dgphoenix.casino.common.SessionHelper;
import com.dgphoenix.casino.common.api.IAccountInfoPersister;
import com.dgphoenix.casino.common.cache.BankInfoCache;
import com.dgphoenix.casino.common.cache.BaseGameCache;
import com.dgphoenix.casino.common.cache.BaseGameInfoTemplateCache;
import com.dgphoenix.casino.common.cache.CurrencyCache;
import com.dgphoenix.casino.common.cache.data.account.AccountInfo;
import com.dgphoenix.casino.common.cache.data.account.IAccountInfo;
import com.dgphoenix.casino.common.cache.data.bank.BankInfo;
import com.dgphoenix.casino.common.cache.data.bank.Coin;
import com.dgphoenix.casino.common.cache.data.currency.Currency;
import com.dgphoenix.casino.common.cache.data.currency.ICurrency;
import com.dgphoenix.casino.common.cache.data.game.BaseGameInfoTemplate;
import com.dgphoenix.casino.common.cache.data.game.IBaseGameInfo;
import com.dgphoenix.casino.common.config.HostConfiguration;
import com.dgphoenix.casino.common.currency.ICurrencyRateManager;
import com.dgphoenix.casino.common.exception.CannotLockException;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.exception.PromoCreationException;
import com.dgphoenix.casino.common.lock.LockingInfo;
import com.dgphoenix.casino.common.promo.*;
import com.dgphoenix.casino.common.promo.feed.tournament.SummaryTournamentFeedEntry;
import com.dgphoenix.casino.common.util.DatePeriod;
import com.dgphoenix.casino.common.util.IdGenerator;
import com.dgphoenix.casino.common.util.Pair;
import com.dgphoenix.casino.common.web.statistics.StatisticsManager;
import com.dgphoenix.casino.promo.exception.UnsupportedCurrencyException;
import com.dgphoenix.casino.promo.persisters.*;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.AtomicDouble;
import com.google.common.util.concurrent.UncheckedExecutionException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static com.dgphoenix.casino.common.util.string.StringUtils.isTrimmedEmpty;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: flsh
 * Date: 17.11.16.
 */
public class PromoCampaignManager implements IPromoCampaignManager {
    public static final int MAX_EXPLOSURE = 5000;
    private static final Logger LOG = LogManager.getLogger(PromoCampaignManager.class);
    public static final String LOCK_ID = "PROMO_";

    private final List<IPromoCampaignsObserver> promoObservers = Lists.newCopyOnWriteArrayList();
    private final CassandraPersistenceManager persistenceManager;
    private final CassandraSupportedPromoPlatformsPersister supportedPromoPlatformsPersister;
    private final IPrizeWonHandlersFactory wonHandlersFactory;
    private final CassandraPromoCampaignStatisticsPersister promoCampaignStatisticsPersister;
    private final CassandraTournamentRankPersister tournamentRankPersister;
    private final CassandraSummaryTournamentPromoFeedPersister summaryTournamentPromoFeedPersister;
    private volatile boolean initialized = false;
    private final LoadingCache<Long, IPromoCampaign> campaignsCache = CacheBuilder.newBuilder()
            .expireAfterAccess(5, TimeUnit.MINUTES)
            .concurrencyLevel(8)
            .build(new CacheLoader<Long, IPromoCampaign>() {
                @Override
                public IPromoCampaign load(@Nonnull Long campaignId) throws Exception {
                    IPromoCampaign promoCampaign = getPromoCampaignPersister().get(campaignId);
                    if (promoCampaign == null) { //may be network promo
                        promoCampaign = getPromoCampaignPersister().getNetworkPromoEvent(campaignId);
                    }
                    checkNotNull(promoCampaign, "Campaign not found for id = %s", campaignId);
                    return promoCampaign;
                }
            });

    //key - campaignId, value = betsCount, betSum
    private final Map<Long, Pair<Integer, Double>> savedAverageBetPair = new ConcurrentHashMap<>();
    private final Map<Long, Pair<AtomicInteger, AtomicDouble>> unsavedAverageBetPair = new ConcurrentHashMap<>();
    private final long gameServerId;
    private final ICurrencyRateManager currencyRatesManager;
    private final IAccountInfoPersister accountPersister;
    private final boolean isProduction;
    private final IKafkaRequestMultiPlayer kafkaRequestMultiPlayer;
    private final ScheduledExecutorService executorService;

    private ScheduledFuture<?> promoCampaignTask;
    private ScheduledFuture<?> saveUnsavedAndRefreshTask;
    private ScheduledFuture<?> tournamentRankUpdaterTask;
    private final PromoPredefinedUsersPersister promoPredefinedUsersPersister;
    private final IPlayerAliasService playerAliasService;
    private final CassandraUnsendedPromoWinInfoPersister unsendedWinInfoPersister;
    private final IPromoCountryRestrictionService promoCountryRestrictionService;
    private final CassandraPromoWinPersister promoWinPersister;

    public PromoCampaignManager(CassandraPersistenceManager persistenceManager,
                                IPrizeWonHandlersFactory prizeWonHandlersFactory,
                                ICurrencyRateManager currencyRatesManager,
                                IAccountInfoPersister accountPersister,
                                HostConfiguration hostConfiguration,
                                IKafkaRequestMultiPlayer kafkaRequestMultiPlayer,
                                ScheduledExecutorService executorService,
                                IPlayerAliasService playerAliasService,
                                IPromoCountryRestrictionService promoCountryRestrictionService) {
        this.persistenceManager = persistenceManager;
        this.supportedPromoPlatformsPersister = persistenceManager
                .getPersister(CassandraSupportedPromoPlatformsPersister.class);
        this.wonHandlersFactory = prizeWonHandlersFactory;
        this.promoCampaignStatisticsPersister = persistenceManager.
                getPersister(CassandraPromoCampaignStatisticsPersister.class);
        this.tournamentRankPersister = persistenceManager.getPersister(CassandraTournamentRankPersister.class);
        this.summaryTournamentPromoFeedPersister = persistenceManager.
                getPersister(CassandraSummaryTournamentPromoFeedPersister.class);
        this.gameServerId = hostConfiguration.getServerId();
        this.currencyRatesManager = currencyRatesManager;
        this.accountPersister = accountPersister;
        this.isProduction = hostConfiguration.isProductionCluster();
        this.kafkaRequestMultiPlayer = kafkaRequestMultiPlayer;
        this.executorService = executorService;
        this.promoPredefinedUsersPersister = persistenceManager.getPersister(PromoPredefinedUsersPersister.class);
        this.playerAliasService = playerAliasService;
        this.unsendedWinInfoPersister = persistenceManager.getPersister(CassandraUnsendedPromoWinInfoPersister.class);
        this.promoCountryRestrictionService = promoCountryRestrictionService;
        this.promoWinPersister = persistenceManager.getPersister(CassandraPromoWinPersister.class);
    }

    @PostConstruct
    public void init() {
        if (!initialized) {
            LOG.info("init started");
            PromoCampaignProcessor promoCampaignProcessor = new PromoCampaignProcessor();
            promoCampaignTask = executorService.scheduleAtFixedRate(promoCampaignProcessor, 60, 10, TimeUnit.SECONDS);
            saveUnsavedAndRefreshTask = executorService.scheduleAtFixedRate(() -> {
                try {
                    LOG.debug("Start task");
                    saveUnsavedAndRefresh();
                } catch (Exception e) {
                    LOG.error("Cannot save unsaved", e);
                }
            }, 10, 10, TimeUnit.SECONDS);
            if (isProduction) {
                tournamentRankUpdaterTask = executorService.scheduleAtFixedRate(new MqTournamentRankUpdater(), 1, 12, TimeUnit.HOURS);
            } else {
                tournamentRankUpdaterTask = executorService.scheduleAtFixedRate(new MqTournamentRankUpdater(), 10, 10, TimeUnit.MINUTES);
            }
            initialized = true;
        }
    }

    @PreDestroy
    public void shutdown() {
        if (initialized) {
            LOG.info("shutdown started");
            try {
                saveUnsavedAndRefresh();
            } catch (Exception e) {
                LOG.error("Cannot save unsaved", e);
            }
            initialized = false;
            promoCampaignTask.cancel(true);
            saveUnsavedAndRefreshTask.cancel(true);
            tournamentRankUpdaterTask.cancel(true);
            LOG.info("shutdown completed");
        } else {
            LOG.warn("already shutdown");
        }
    }

    @Override
    public void createSummaryTournamentFeed(long id, String feedUrl, String bankName, long startDate, long endDate,
                                            TournamentObjective type, long tournamentId) {
        summaryTournamentPromoFeedPersister.create(id, feedUrl, bankName, startDate, endDate, type, tournamentId);
    }

    @Override
    public Map<String, List<SummaryTournamentFeedEntry>> getFeedEntriesForTournament(long tournamentId) {
        return summaryTournamentPromoFeedPersister.getAllFeedEntriesForTournament(tournamentId);
    }

    public Double getAverageBet(long campaignId) {
        Pair<Integer, Double> savedPair = savedAverageBetPair.get(campaignId);
        if (savedPair == null) {
            savedPair = promoCampaignStatisticsPersister.getAverageBetPair(campaignId);
            if (savedPair != null) {
                savedAverageBetPair.put(campaignId, savedPair);
            }
        }
        Pair<AtomicInteger, AtomicDouble> unsavedPair = unsavedAverageBetPair.get(campaignId);
        int betsCount = 0;
        double betsSum = 0;
        if (savedPair != null) {
            betsCount += savedPair.getKey();
            betsSum += savedPair.getValue();
        }
        if (unsavedPair != null) {
            betsCount += unsavedPair.getKey().get();
            betsSum += unsavedPair.getValue().get();
        }
        return betsCount <= 0 || betsSum <= 0 ? 1 : betsSum / betsCount;
    }

    public void incrementAverageBet(long campaignId, int betsCount, double betSum) {
        Pair<AtomicInteger, AtomicDouble> unsavedPair = unsavedAverageBetPair.get(campaignId);
        if (unsavedPair == null) {
            Pair<AtomicInteger, AtomicDouble> newPair = new Pair<>(new AtomicInteger(0), new AtomicDouble(0));
            unsavedPair = unsavedAverageBetPair.putIfAbsent(campaignId, newPair);
            if (unsavedPair == null) {
                unsavedPair = newPair;
            }
        }
        unsavedPair.getKey().addAndGet(betsCount);
        unsavedPair.getValue().addAndGet(betSum);
    }

    private void refreshSavedAverageBet(long campaignId) {
        Pair<Integer, Double> averageBetPair = promoCampaignStatisticsPersister.getAverageBetPair(campaignId);
        if (averageBetPair != null) { //very rare, only if saved with not strong consistency
            savedAverageBetPair.put(campaignId, averageBetPair);
        }
    }

    private synchronized void saveUnsavedAndRefresh() {
        for (Map.Entry<Long, Pair<AtomicInteger, AtomicDouble>> entry : unsavedAverageBetPair.entrySet()) {
            Long campaignId = entry.getKey();
            AtomicInteger roundsCount = entry.getValue().getKey();
            AtomicDouble betSum = entry.getValue().getValue();
            int roundsCountDelta = roundsCount.get();
            double betSumDelta = betSum.get();
            if (roundsCountDelta < 0 || betSumDelta < 0) {
                LOG.warn("saveUnsaved: found illegal values for campaignId={}, roundsCountDelta={}, betSumDelta={}",
                        campaignId, roundsCountDelta, betSumDelta);
            }
            if (roundsCountDelta > 0 || betSumDelta > 0) {
                LOG.debug("saveUnsavedAndRefresh: campaignId={}, roundsCountDelta={}, betSumDelta={}", campaignId,
                        roundsCountDelta, betSumDelta);
                promoCampaignStatisticsPersister.increment(campaignId, (int) gameServerId, roundsCountDelta, betSumDelta);
                roundsCount.addAndGet(-roundsCountDelta);
                betSum.addAndGet(-betSumDelta);
            }
        }
        for (Map.Entry<Long, Pair<Integer, Double>> entry : savedAverageBetPair.entrySet()) {
            Long campaignId = entry.getKey();
            refreshSavedAverageBet(campaignId);
        }
    }

    @Override
    public IPromoCampaign create(IPromoTemplate<?,?> template, String name, EnterType enterType, DatePeriod period,
                                 Set<Long> bankIds, Set<Long> gameIds,
                                 String baseCurrency, Map<String, String> localizedStrings,
                                 Map<Long, String> promoDetailURLs, PlayerIdentificationType playerIdentificationType,
                                 ISupportedPlatform supportedPlatform) throws PromoCreationException {
        long now = System.currentTimeMillis();
        checkIncomingData(period, baseCurrency, playerIdentificationType);

        //paranoid check
        if (template instanceof TournamentPromoTemplate) {
            TournamentPromoTemplate<?> tournamentTemplate = (TournamentPromoTemplate) template;
            if (tournamentTemplate.getObjective() == TournamentObjective.MAX_PERFORMANCE) {
                Set<TournamentPrize> prizes = tournamentTemplate.getPrizePool();
                checkNotNull(prizes, "prize pool can not be null");
                for (TournamentPrize prize : prizes) {
                    checkArgument(prize.getEventQualifier() instanceof MaxPerformanceEventQualifier,
                            "prize event qualifier is not MaxPerformanceEventQualifier");
                }
            }
        }
        if (template instanceof MaxBalanceTournamentPromoTemplate) {
            checkStartBalance((MaxBalanceTournamentPromoTemplate) template, bankIds, gameIds, baseCurrency);
        }
        setPrizeIds(template);
        long campaignId = IdGenerator.getInstance().getNext(PromoCampaign.class);
        PromoCampaign campaign = new PromoCampaign(campaignId, template, name, enterType, period, Status.READY,
                bankIds, gameIds, baseCurrency, promoDetailURLs, playerIdentificationType);
        LOG.debug("create: {}", campaign);
        if (localizedStrings != null) {
            getLocalizationsPersister().persistPromoLocalizations(campaign.getId(), localizedStrings);
        }
        supportedPromoPlatformsPersister.persist(campaignId, supportedPlatform);
        getPromoCampaignPersister().persist(campaign);
        StatisticsManager.getInstance().updateRequestStatistics("PromoCampaignManager: create",
                System.currentTimeMillis() - now);
        notifyCampaignCreated(campaignId);
        return campaign;
    }

    @SuppressWarnings("rawtypes")
    private void checkStartBalance(MaxBalanceTournamentPromoTemplate template, Set<Long> bankIds, Set<Long> gameIds,
                                   String baseCurrency) throws PromoCreationException {
        long buyInAmount = template.getBuyInAmount();
        BaseGameCache baseGameCache = BaseGameCache.getInstance();
        for (Long bankId : bankIds) {
            for (Long gameId : gameIds) {
                IBaseGameInfo gameInfo = baseGameCache.getGameInfo(bankId, gameId, baseCurrency);
                if (gameInfo == null) {
                    gameInfo = baseGameCache.getGameInfo(bankId, gameId, (String) null);
                }
                if (gameInfo == null || !gameInfo.isEnabled()) {
                    LOG.error("Bank:{} has not contain game with id:{}", bankId, gameId);
                    throw new PromoCreationException("Bank:" + bankId + " has not contain game with id:" + gameId);
                }
                Coin minCoin = (Coin) Collections.min(gameInfo.getCoins());
                if (minCoin.getValue() > buyInAmount) {
                    LOG.error("startBalance cannot be less than the minimum coin value for " +
                            "game with bankId:{} gameId:{}", bankId, gameId);
                    throw new PromoCreationException("startBalance cannot be less than the minimum coin value for " +
                            "game with bankId:" + bankId + " gameId:" + gameId);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public NetworkPromoEvent createNetworkEvent(long eventId, IPromoTemplate<?,?> template, String name, EnterType enterType,
                                                DatePeriod period, Set<Long> bankIds, Set<Long> gameIds,
                                                String baseCurrency, Map<Long, String> promoDetailURLs,
                                                PlayerIdentificationType playerIdentificationType,
                                                ISupportedPlatform supportedPlatform, long networkPromoCampaignId)
            throws CommonException {
        long now = System.currentTimeMillis();
        checkIncomingData(period, baseCurrency, playerIdentificationType);
        setPrizeIds(template);
        INetworkPromoEventTemplate<IPrize, ? extends INetworkPromoEventTemplate> networkPromoEventTemplate = (INetworkPromoEventTemplate<IPrize, ? extends INetworkPromoEventTemplate>) template;
        NetworkPromoEvent campaign = new NetworkPromoEvent(eventId, networkPromoCampaignId, networkPromoEventTemplate,
                name, enterType, period, Status.READY, bankIds, gameIds, baseCurrency, promoDetailURLs,
                playerIdentificationType);
        LOG.debug("createNetworkEvent: {}", campaign);
        supportedPromoPlatformsPersister.persist(eventId, supportedPlatform);
        StatisticsManager.getInstance().updateRequestStatistics("PromoCampaignManager: createNetworkEvent",
                System.currentTimeMillis() - now);
        return campaign;
    }

    @Override
    public void saveNetworkPromoCampaign(INetworkPromoCampaign<NetworkPromoEvent> networkPromoCampaign) {
        getPromoCampaignPersister().persist((IPromoCampaign) networkPromoCampaign);
        notifyCampaignChanged(((IPromoCampaign) networkPromoCampaign).getId());
    }

    private void setPrizeIds(IPromoTemplate<?,?> template) {
        Set<? extends IPrize> prizePool = template.getPrizePool();
        for (IPrize prize : prizePool) {
            prize.setId(IdGenerator.getInstance().getNext(IPrize.class));
        }
    }

    private void checkIncomingData(DatePeriod period, String baseCurrency, PlayerIdentificationType playerIdentificationType) throws PromoCreationException {
        try {
            checkArgument(period.getStartDate().getTime() <= period.getEndDate().getTime(), "StartDate after endDate");
            checkArgument(period.getEndDate().getTime() > System.currentTimeMillis(), "EndDate already come");
            checkArgument(!isTrimmedEmpty(baseCurrency), "baseCurrency not defined");
            Currency currency = CurrencyCache.getInstance().get(baseCurrency);
            if (currency == null) {
                throw new UnsupportedCurrencyException(baseCurrency);
            }
            checkNotNull(playerIdentificationType, "playerIdentificationType can not be null");
        } catch (IllegalArgumentException e) {
            throw new PromoCreationException(e);
        }
    }

    @Override
    public void cancel(long campaignId, String cancelReason) throws CommonException {
        IPromoCampaign campaign = getPromoCampaignPersister().get(campaignId);
        if (campaign == null) { // may be network promo event
            campaign = getPromoCampaignPersister().getNetworkPromoEvent(campaignId);
        }
        if (campaign == null) {
            LOG.error("Unknown promo campaign, id={}", campaignId);
            throw new CommonException("Unknown promo campaign, id=" + campaignId);
        }
        Status status = campaign.getStatus();
        if (Status.STARTED.equals(status) || Status.READY.equals(status)) {
            if (campaign.isNetworkPromoCampaign()) {
                NetworkPromoCampaign networkPromoCampaign = (NetworkPromoCampaign) campaign;
                Set<NetworkPromoEvent> events = networkPromoCampaign.getEvents();
                for (NetworkPromoEvent event : events) {
                    Status eventStatus = event.getStatus();
                    if (Status.STARTED.equals(eventStatus) || Status.READY.equals(eventStatus)) {
                        changeStatus(event, eventStatus, Status.CANCELLED);
                    }
                }
            }
            changeStatus(campaign, status, Status.CANCELLED);
        } else if (!Status.CANCELLED.equals(status)) { //finished and qualification case
            LOG.error("Promo campaign cannot be canceled because already finished, campaign={}", campaign);
            throw new CommonException("Promo campaign cannot be canceled because already finished");
        }
    }

    private void sendTournamentEndedToMq(IPromoCampaign campaign, Status oldStatus, Status newStatus) {
        if (campaign.getTemplate().getPromoType().isScoreCounting()
                && (newStatus == Status.CANCELLED || newStatus == Status.QUALIFICATION || newStatus == Status.FINISHED)
                && (oldStatus == Status.STARTED || oldStatus == Status.READY)) {
            long campaignId = campaign.getId();
            try {
                kafkaRequestMultiPlayer.sendTournamentEnded(campaignId, oldStatus.name(), newStatus.name());
            } catch (Exception e) {
                LOG.warn("Tournament ended notification was not sent to MQ, campaignId={}", campaignId);
            }
        }
    }

    @Override
    public Set<IPromoCampaign> getTournamentsForMultiplayerGames(Long bankId, Long gameId, Status status,
                                                                 IAccountInfo accountInfo) throws CommonException {
        BaseGameInfoTemplate template = BaseGameInfoTemplateCache.getInstance().getBaseGameInfoTemplateById(gameId);
        if (template == null || !template.isMultiplayerGame()) {
            return Collections.emptySet();
        }
        Set<IPromoCampaign> allCampaigns = getPromoCampaigns(bankId, gameId, status, accountInfo);
        Set<IPromoCampaign> tournamentCampaigns = new HashSet<>();
        for (IPromoCampaign campaign : allCampaigns) {
            if (isMultiplayerSupportedPromo(campaign)) {
                tournamentCampaigns.add(campaign);
            }
        }
        return tournamentCampaigns;
    }

    @Override
    public Set<IPromoCampaign> getTournamentsForMultiplayerGames(long bankId, String currency) throws CommonException {
        Set<Long> gameIds = getEnabledMultiplayerGames(bankId, "");
        Set<IPromoCampaign> allCampaigns = getPromoCampaignPersister().getPromos(bankId, gameIds);
        Set<IPromoCampaign> tournamentCampaigns = new HashSet<>();
        for (IPromoCampaign campaign : allCampaigns) {
            if (isMultiplayerSupportedPromo(campaign)) {
                tournamentCampaigns.add(campaign);
            }
        }
        return tournamentCampaigns;
    }

    @Override
    public NetworkPromoCampaign getNetworkPromoCampaign(long promoCampaignId) throws CommonException {
        return (NetworkPromoCampaign) getPromoCampaignPersister().get(promoCampaignId);
    }

    private Set<Long> getEnabledMultiplayerGames(long bankId, String currency) {
        Set<Long> gameIds = new HashSet<>();
        for (Long gameId : BaseGameInfoTemplateCache.getInstance().getMultiplayerGames()) {
            IBaseGameInfo bgi = BaseGameCache.getInstance().getGameInfo(bankId, gameId, currency);
            if (bgi != null && bgi.isEnabled()) {
                gameIds.add(gameId);
            }
        }
        return gameIds;
    }

    private boolean isMultiplayerSupportedPromo(IPromoCampaign campaign) {
        if (campaign.getTemplate().getPromoType().isTournamentLogic() &&
                campaign.getTemplate() instanceof TournamentPromoTemplate) {
            TournamentPromoTemplate<?> tournamentPromoTemplate = (TournamentPromoTemplate) campaign.getTemplate();
            TournamentObjective objective = tournamentPromoTemplate.getObjective();
            return objective == TournamentObjective.MAX_PERFORMANCE
                    || objective == TournamentObjective.HIGHEST_WIN
                    || objective == TournamentObjective.CURRENT_TOURNAMENT_BALANCE
                    || objective == TournamentObjective.TOURNAMENT_MAX_BET_SUM;
        }
        return false;
    }

    public NetworkPromoCampaign getNetworkPromoCampaignByBank(long bankId) {
        Set<Long> gameIds = getEnabledMultiplayerGames(bankId, "");
        Set<IPromoCampaign> allCampaigns = getPromoCampaignPersister().getPromos(bankId, gameIds);
        return allCampaigns.stream()
                .filter(IPromoCampaign::isNetworkPromoCampaign)
                .filter(campaign ->
                        campaign.getActionPeriod().isDateBetween(new Date()) && campaign.getStatus() == Status.STARTED)
                .map(campaign -> (NetworkPromoCampaign) campaign)
                .min((campaign1, campaign2) -> {
                    long firstStartDate = campaign1.getActionPeriod().getStartDate().getTime();
                    long secondStartDate = campaign2.getActionPeriod().getStartDate().getTime();
                    return (int) (firstStartDate - secondStartDate);
                })
                .orElse(null);
    }

    @Override
    public Set<IPromoCampaign> getPromoCampaigns(Long bankId, Long gameId, Status status,
                                                 IAccountInfo accountInfo) throws CommonException {
        if (accountInfo != null && isFakeCurrency(accountInfo.getCurrency())) {
            return Collections.emptySet();
        }
        return getPromoCampaignPersister().getPromos(bankId, gameId, status);
    }

    private CassandraPromoCampaignPersister getPromoCampaignPersister() {
        return persistenceManager.getPersister(CassandraPromoCampaignPersister.class);
    }

    private CassandraPromoCampaignMembersPersister getPromoCampaignMembersPersister() {
        return persistenceManager.getPersister(CassandraPromoCampaignMembersPersister.class);
    }

    private CassandraLocalizationsPersister getLocalizationsPersister() {
        return persistenceManager.getPersister(CassandraLocalizationsPersister.class);
    }

    public DistributedLockManager getLockManager() {
        return persistenceManager.getPersister(DistributedLockManager.class);
    }

    @Override
    public Set<IPromoCampaign> getActive(long bankId, long gameId, Map<String, String> startGameParams,
                                         Long clientTypeId, IAccountInfo accountInfo) throws CommonException {
        if (accountInfo != null && isFakeCurrency(accountInfo.getCurrency())) {
            return Collections.emptySet();
        }
        Set<IPromoCampaign> active = getPromoCampaignPersister().getStartedForBankAndGame(bankId, gameId);
        Set<IPromoCampaign> playerActive = new HashSet<>();
        for (IPromoCampaign campaign : active) {
            if (campaign.isActual(gameId) && isPlayerIncludedInPromo(campaign, accountInfo)) {
                ISupportedPlatform supportedPlatform = supportedPromoPlatformsPersister
                        .getSupportedPlatform(campaign.getId());
                if (supportedPlatform.isPlatformSupported(clientTypeId)) {
                    if (EnterType.AUTO.equals(campaign.getEnterType())) {
                        playerActive.add(campaign);
                    } else {
                        throw new CommonException("Unsupported EnterType:" + campaign.getEnterType());
                    }
                }
            }
        }
        return playerActive;
    }

    @Override
    public Set<IPromoCampaign> getActive(long bankId) {
        return getPromoCampaignPersister().getStarted(bankId);
    }

    @Override
    public IPromoCampaign getPromoCampaign(long campaignId) {
        try {
            return campaignsCache.get(campaignId);
        } catch (UncheckedExecutionException | ExecutionException e) {
            LOG.warn("Error during retrieving promoCampaign", e);
            return null;
        }
    }

    @Override
    public IPromoCampaign getPromoCampaignFromPersister(long campaignId) {
        return getPromoCampaignPersister().get(campaignId);
    }

    @Override
    public Pair<IPromoCampaign, PromoCampaignMember> registerPlayer(long promoCampaignId, IAccountInfo account,
                                                                    long gameSessionId, long gameId)
            throws CommonException {
        long now = System.currentTimeMillis();
        LOG.debug("registerPlayer: promoCampaign={}, accountId={}, gameSessionId={}, gameId={}",
                promoCampaignId, account.getId(), gameSessionId, gameId);
        IPromoCampaign campaign = getPromoCampaign(promoCampaignId);
        if (campaign == null || !(campaign.isActual(gameId) || campaign.showNotifications(gameId))) {
            LOG.warn("Campaign is null or not actual. Id = {}, campaign {}", promoCampaignId, campaign);
            return null;
        }
        PromoCampaignMember member = SessionHelper.getInstance().getTransactionData().
                getPromoMember(promoCampaignId);
        if (member == null) {
            PromoCampaignMemberInfos memberInfos = SessionHelper.getInstance().getTransactionData().
                    getPromoMemberInfos();
            if (memberInfos == null) {
                memberInfos = new PromoCampaignMemberInfos();
                SessionHelper.getInstance().getTransactionData().setPromoMemberInfos(memberInfos);
            }
            member = getOrCreatePromoMember(account, campaign);
            LOG.debug("registerPlayer: created member={}", member);
            memberInfos.add(member);
        }
        member.addGameSessionId(gameSessionId);
        StatisticsManager.getInstance().updateRequestStatistics("PromoCampaignManager: registerPlayer",
                System.currentTimeMillis() - now);
        return new Pair<>(campaign, member);
    }

    @Override
    public Map<IPromoCampaign, PromoCampaignMember> registerPlayerInPromos(Collection<Long> promoCampaignIds,
                                                                           IAccountInfo account, long gameSessionId,
                                                                           long gameId) throws CommonException {
        long now = System.currentTimeMillis();
        Map<IPromoCampaign, PromoCampaignMember> registeredCampaigns = new HashMap<>();
        for (Long promoCampaignId : promoCampaignIds) {
            IPromoCampaign promoCampaign = getPromoCampaign(promoCampaignId);
            if (promoCampaign != null && promoCampaign.getTemplate() instanceof MaxBalanceTournamentPromoTemplate) {
                MaxBalanceTournamentPromoTemplate template =
                        (MaxBalanceTournamentPromoTemplate) promoCampaign.getTemplate();
                long endTime = promoCampaign.getActionPeriod().getEndDate().getTime();
                if (System.currentTimeMillis() + template.getCutOffTime() > endTime) {
                    continue;
                }
            }
            Pair<IPromoCampaign, PromoCampaignMember> campaignAndMember = registerPlayer(promoCampaignId, account,
                    gameSessionId, gameId);
            if (campaignAndMember != null) {
                registeredCampaigns.put(campaignAndMember.getKey(), campaignAndMember.getValue());
            }
        }
        StatisticsManager.getInstance().updateRequestStatistics("PromoCampaignManager: enterPlayerInPromos",
                System.currentTimeMillis() - now);
        return registeredCampaigns;
    }

    @Override
    public void finalizePromoForGameSession(long accountId, long gameSessionId) throws CommonException {
        PromoCampaignMemberInfos memberInfos = SessionHelper.getInstance().getTransactionData().getPromoMemberInfos();
        if (memberInfos != null) {
            long now = System.currentTimeMillis();
            Map<Long, PromoCampaignMember> members = memberInfos.getPromoMembers();
            CassandraPromoCampaignMembersPersister persister = getPromoCampaignMembersPersister();
            for (Map.Entry<Long, PromoCampaignMember> entry : members.entrySet()) {
                PromoCampaignMember member = entry.getValue();
                LOG.debug("finalizePromoForGameSession: {}", member);
                member.resetWebSocketSupport();
                member.resetLastEnteredServerId();
                persister.update(member);
            }
            SessionHelper.getInstance().getTransactionData().setPromoMemberInfos(null);
            StatisticsManager.getInstance().updateRequestStatistics("PromoCampaignManager: finalizePromoForGameSession",
                    System.currentTimeMillis() - now);
        }
    }

    @Override
    public void savePromoCampaignMember(PromoCampaignMember member) {
        getPromoCampaignMembersPersister().update(member);
    }

    @Override
    public PromoCampaignMember getPromoCampaignMember(long promoCampaignId, long accountId) {
        return SessionHelper.getInstance().getTransactionData().getPromoMember(promoCampaignId);
    }

    @Override
    public Set<PromoCampaignMember> getPromoCampaignMembers(long promoCampaignId) {
        return getPromoCampaignMembersPersister().getPromoCampaignMembers(promoCampaignId);
    }

    @Override
    public Set<PromoCampaignMember> getAllBankPromoCampaignMembers(long bankId) {
        long now = System.currentTimeMillis();
        Set<Long> promoIds = getPromoCampaignPersister().getPromoIdsByBank(bankId);
        Set<PromoCampaignMember> bankPromoCampaignsMembers = new HashSet<>();
        for (Long promoCampaignId : promoIds) {
            bankPromoCampaignsMembers.
                    addAll(getPromoCampaignMembersPersister().getPromoCampaignMembers(promoCampaignId));
        }
        StatisticsManager.getInstance().updateRequestStatistics("PromoCampaignManager: getAllBankPromoCampaignMembers",
                System.currentTimeMillis() - now);
        return bankPromoCampaignsMembers;
    }

    @Override
    public IPrizeWonHandlersFactory getWonHandlersFactory() throws CommonException {
        return wonHandlersFactory;
    }

    @Override
    public void invalidateCachedCampaign(long campaignId) {
        LOG.debug("Invalidate cached campaign {}", campaignId);
        campaignsCache.invalidate(campaignId);
    }

    @Override
    public void processPrizeWonFromExternalSide(long campaignId, long prizeId, long gameId, long amount, String currency)
            throws CommonException {
        LOG.debug("processPrizeWonFromExternalSide: campaignId={}, prizeId={}", campaignId, prizeId);
        long now = System.currentTimeMillis();
        String lock = getLockId(campaignId);
        LockingInfo lockInfo = null;
        try {
            lockInfo = getLockManager().lock(lock, 3000L);
            IPromoCampaign campaign = getPromoCampaign(campaignId);
            if (campaign == null || campaign.getStatus() != Status.STARTED
                    || !campaign.getActionPeriod().isDateBetween(new Date())) {
                LOG.debug("Campaign is not actual, return. campaign={}", campaign);
                return;
            }
            IPrize prize = campaign.getPrize(prizeId);
            if (prize == null) {
                throw new CommonException("Prize not found, prizeId=" + prizeId);
            }
            prize.incrementTotalAwardedCount();
            saveCampaign(campaign);
            campaignsCache.invalidate(campaignId);
            notifyCampaignChanged(campaignId);
        } finally {
            if (lockInfo != null) {
                getLockManager().unlock(lockInfo);
            }
        }
        StatisticsManager.getInstance().updateRequestStatistics("PromoCampaignManager: processPrizeWonFromExternalSide",
                System.currentTimeMillis() - now);
    }

    @Override
    public boolean qualifyConcurrentPrize(IPromoCampaign campaign, IConcurrentPromoTemplate<?,?> template,
                                          IPrize campaignPrize, PromoCampaignMember member, DesiredPrize desiredPrize,
                                          ICurrencyRateManager currencyRateManager,
                                          String playerCurrency) throws CommonException {
        Pair<Boolean, LockingInfo> pair = null;
        try {
            pair = qualifyConcurrentPrizeWithoutUnlock(campaign, template, campaignPrize, member, desiredPrize,
                    currencyRateManager, playerCurrency);
        } finally {
            if (pair != null && pair.getValue() != null) {
                getLockManager().unlock(pair.getValue());
            }
        }
        return pair != null && pair.getKey() != null && pair.getKey();
    }

    public Pair<Boolean, LockingInfo> qualifyConcurrentPrizeWithoutUnlock(IPromoCampaign campaign, IConcurrentPromoTemplate<?,?> template,
                                                                          IPrize campaignPrize, PromoCampaignMember member, DesiredPrize desiredPrize,
                                                                          ICurrencyRateManager currencyRateManager,
                                                                          String playerCurrency) throws CommonException {
        checkArgument(campaign.getTemplate() == template, "Template must belong to campaign, campaign = %s", campaign);
        long now = System.currentTimeMillis();
        long campaignId = campaign.getId();
        long prizeId = campaignPrize.getId();
        String lock = getLockId(campaignId);
        boolean qualified = template.qualifyPrize(campaignPrize, member, desiredPrize, currencyRateManager,
                campaign.getBaseCurrency(), playerCurrency);
        LockingInfo lockInfo = null;
        if (qualified) {
            LOG.debug("qualifyConcurrentPrize: prize qualified, campaignPrize={}, desiredPrize={}", campaignPrize, desiredPrize);
            try {
                lockInfo = getLockManager().lock(lock, 3000L);
                IPromoCampaign actualCampaign;
                if (campaign instanceof NetworkPromoEvent) {
                    NetworkPromoEvent event = (NetworkPromoEvent) campaign;
                    actualCampaign = getNetworkPromoEvent(event.getParentPromoCampaignId(), event.getId());
                } else {
                    actualCampaign = getPromoCampaignPersister().get(campaignId);
                }
                if (actualCampaign == null) {
                    throw new CommonException("Campaign not found, id=" + campaignId);
                }
                IConcurrentPromoTemplate<?,?> actualTemplate = (IConcurrentPromoTemplate<?,?>) actualCampaign.getTemplate();
                IPrize actualPrize = actualCampaign.getPrize(prizeId);
                //need double-check after lock
                qualified = actualTemplate.qualifyPrize(actualPrize, member, desiredPrize, currencyRateManager, campaign.getBaseCurrency(),
                        playerCurrency);
                if (qualified) {
                    boolean prizeWon = actualTemplate.processQualifiedConcurrentPrize(prizeId, desiredPrize);
                    if (prizeWon) {
                        LOG.info("qualifyConcurrentPrize: prizeWon, prizeId = {}, actualCampaign.id={}, member.id={}, member.desiredPrizes.size={}, " +
                                        ", member.awardedPrizes.size={}, actualPrize={}", prizeId, actualCampaign.getId(), member.getAccountId(),
                                member.getDesiredPrizes().size(), member.getAwardedPrizes().size(), actualPrize);
                        saveCampaign(actualCampaign);
                        campaignsCache.invalidate(campaignId);
                        notifyCampaignChanged(campaignId);
                    } else {
                        qualified = false;
                    }
                }
            } catch (Exception e) {
                qualified = false;
                if (e instanceof CannotLockException) {
                    LOG.warn("qualifyConcurrentPrize: failed, cannot lock, accountId={}", member.getAccountId());
                } else {
                    LOG.error("qualifyConcurrentPrize failed, accountId={}", member.getAccountId(), e);
                }
            } finally {
                if (lockInfo != null && !qualified) {
                    getLockManager().unlock(lockInfo);
                    lockInfo = null;
                }
            }
        }
        StatisticsManager.getInstance().updateRequestStatistics("PromoCampaignManager: qualifyConcurrentPrize",
                System.currentTimeMillis() - now);
        return new Pair<>(qualified, lockInfo);
    }

    private NetworkPromoEvent getNetworkPromoEvent(long networkPromoId, long eventId) {
        NetworkPromoCampaign networkPromo = (NetworkPromoCampaign) getPromoCampaignPersister().
                get(networkPromoId);
        NetworkPromoEvent result = null;
        Set<NetworkPromoEvent> events = networkPromo.getEvents();
        for (NetworkPromoEvent promoEvent : events) {
            if (promoEvent.getId() == eventId) {
                result = promoEvent;
                break;
            }
        }
        return result;
    }

    private void saveCampaign(IPromoCampaign campaign) throws CommonException {
        if (campaign instanceof NetworkPromoEvent) {
            NetworkPromoEvent event = (NetworkPromoEvent) campaign;
            NetworkPromoCampaign networkPromoCampaign = getNetworkPromoCampaign(event.getParentPromoCampaignId());
            Set<NetworkPromoEvent> events = networkPromoCampaign.getEvents();
            events.removeIf(networkPromoEvent -> networkPromoEvent.getId() == event.getId());
            networkPromoCampaign.addEvent(event);
            getPromoCampaignPersister().persist(networkPromoCampaign);
        } else {
            getPromoCampaignPersister().persist(campaign);
        }
    }

    public void changeStatus(IPromoCampaign changedCampaign, Status oldStatus, Status newStatus) throws CommonException {
        long now = System.currentTimeMillis();
        String lock = getLockId(changedCampaign.getId());
        LOG.debug("changeStatus: campaignId={}, oldStatus={}, newStatus={}", changedCampaign.getId(), oldStatus,
                newStatus);
        LockingInfo lockInfo = null;
        try {
            lockInfo = getLockManager().lock(lock, 10000L);
            IPromoCampaign campaign;
            if (changedCampaign instanceof NetworkPromoEvent) {
                NetworkPromoEvent event = (NetworkPromoEvent) changedCampaign;
                campaign = getNetworkPromoEvent(event.getParentPromoCampaignId(), event.getId());
            } else {
                campaign = getPromoCampaignPersister().get(changedCampaign.getId());
            }
            if (campaign == null) {
                throw new CommonException("PromoCampaign not found, id=" + changedCampaign.getId());
            }
            sendTournamentEndedToMq(campaign, oldStatus, newStatus);
            Status currentStatus = campaign.getStatus();
            if (currentStatus.equals(oldStatus)) {
                campaign.setStatus(newStatus);
                saveCampaign(campaign);
                if (campaign instanceof NetworkPromoEvent) {
                    NetworkPromoEvent event = (NetworkPromoEvent) campaign;
                    campaignsCache.invalidate(event.getParentPromoCampaignId());
                } else {
                    campaignsCache.invalidate(changedCampaign.getId());
                }
                notifyCampaignStatusChanged(changedCampaign.getId(), oldStatus, newStatus);
            } else {
                if (currentStatus.equals(newStatus)) {
                    LOG.info("changeStatus: status already changed, just return campaignId={}, currentStatus={}",
                            changedCampaign.getId(), currentStatus);
                } else {
                    if (currentStatus.ordinal() < newStatus.ordinal()) {
                        throw new CommonException("Incorrect status change flow, campaignId=" +
                                changedCampaign.getId() + ", current=" + currentStatus + ", newStatus=" + newStatus);
                    } else {
                        LOG.warn("Suspicious status change, campaignId={}, current={}, newStatus={}",
                                changedCampaign.getId(), currentStatus, newStatus);
                    }
                }
            }
        } finally {
            if (lockInfo != null) {
                getLockManager().unlock(lockInfo);
            }
        }
        StatisticsManager.getInstance().updateRequestStatistics("PromoCampaignManager: changeStatus",
                System.currentTimeMillis() - now);
    }

    private void qualifyPromo(IPromoCampaign qualifiedCampaign) throws CommonException {
        long now = System.currentTimeMillis();
        String lock = getLockId(qualifiedCampaign.getId());
        LOG.debug("qualifyPromo: campaignId={}", qualifiedCampaign.getId());
        LockingInfo lockInfo = null;
        try {
            lockInfo = getLockManager().lock(lock, 10000L);
            IPromoCampaign campaign;
            if (qualifiedCampaign instanceof NetworkPromoEvent) {
                NetworkPromoEvent event = (NetworkPromoEvent) qualifiedCampaign;
                campaign = getNetworkPromoEvent(event.getParentPromoCampaignId(), event.getId());
            } else {
                campaign = getPromoCampaignPersister().get(qualifiedCampaign.getId());
            }
            if (campaign == null) {
                throw new CommonException("PromoCampaign not found, id=" + qualifiedCampaign.getId());
            }
            if (campaign.getStatus().equals(Status.QUALIFICATION)) {
                updateRanksForMQTournament(campaign);
                campaign.setStatus(Status.FINISHED);
                saveCampaign(campaign);
                if (qualifiedCampaign instanceof NetworkPromoEvent) {
                    NetworkPromoEvent event = (NetworkPromoEvent) qualifiedCampaign;
                    campaignsCache.invalidate(event.getParentPromoCampaignId());
                } else {
                    campaignsCache.invalidate(campaign.getId());
                }
                notifyCampaignStatusChanged(campaign.getId(), Status.QUALIFICATION, Status.FINISHED);
            } else {
                LOG.error("qualifyPromo: incorrect status, stop qualification, campaign={}", campaign);
            }
        } finally {
            if (lockInfo != null) {
                getLockManager().unlock(lockInfo);
            }
        }
        StatisticsManager.getInstance().updateRequestStatistics("PromoCampaignManager: qualifyPromo",
                System.currentTimeMillis() - now);
    }

    @Override
    public void registerPromoCampaignsObserver(IPromoCampaignsObserver promoCampaignsObserver) {
        promoObservers.add(promoCampaignsObserver);
    }

    private void notifyCampaignCreated(long campaignId) {
        for (IPromoCampaignsObserver observer : promoObservers) {
            observer.notifyCampaignCreated(campaignId);
        }
    }

    private void notifyCampaignChanged(long campaignId) {
        for (IPromoCampaignsObserver observer : promoObservers) {
            observer.notifyCampaignChanged(campaignId);
        }
    }

    private void notifyCampaignStatusChanged(long campaignId, Status oldStatus, Status newStatus) {
        for (IPromoCampaignsObserver observer : promoObservers) {
            observer.notifyCampaignStatusChanged(campaignId, oldStatus, newStatus);
        }
    }

    private List<Long> getCoins(long bankId, long gameId, Currency currency) {
        BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(bankId);
        IBaseGameInfo info = BaseGameCache.getInstance().getGameInfoById(bankInfo.getId(), gameId, currency);
        List<Coin> coins = null;
        if (info != null) {
            coins = info.getCoins();
        }
        if (coins == null || coins.isEmpty()) {
            coins = bankInfo.getCoins();
        }
        List<Long> result = new ArrayList<>();
        for (Coin coin : coins) {
            result.add(coin.getValue());
        }
        return result;
    }

    public void updateRanksForMQTournament(IPromoCampaign campaign) {
        if (!initialized) {
            return;
        }
        if (campaign.getTemplate() instanceof TournamentPromoTemplate) {
            TournamentPromoTemplate<?> template = (TournamentPromoTemplate) campaign.getTemplate();
            if (template.getObjective() == TournamentObjective.MAX_PERFORMANCE) {
                List<TournamentMemberRank> ranks = tournamentRankPersister.getByCampaign(campaign.getId());
                final Double averageBet = getAverageBet(campaign.getId());
                LOG.info("MqTournamentRankUpdater process tournament={}", campaign);
                int processedCount = 0;
                for (TournamentMemberRank rank : ranks) {
                    try {
                        DesiredPrize prize = new DesiredPrize();
                        prize.setQualifiedBetSum(rank.getBetSum());
                        prize.setQualifiedWinSum(rank.getWinSum());
                        prize.setQualifiedBetsCount((int) rank.getRoundsCount());
                        AccountInfo accountInfo = accountPersister.getById(rank.getAccountId());
                        if (accountInfo == null) {
                            LOG.error("MqTournamentRankUpdater: account not found={}", rank);
                            continue;
                        }
                        Long gameId = campaign.getGameIds().iterator().next();
                        List<Long> coins = getCoins(accountInfo.getBankId(), gameId, accountInfo.getCurrency());
                        final long minBet = Collections.min(coins);
                        final long maxBet = Collections.max(coins);
                        ICampaignStatisticsProvider stat = new ICampaignStatisticsProvider() {
                            @Override
                            public double getAverageBet() {
                                return averageBet == null ? 1 : averageBet;
                            }

                            @Override
                            public long getMinBet() {
                                return minBet;
                            }

                            @Override
                            public long getMaxBet() {
                                return maxBet;
                            }

                            @Override
                            public int getMaxExposure() {
                                return MAX_EXPLOSURE;
                            }

                            @Override
                            public double getHighestWinPerSingleBet() {
                                return 0;
                            }

                            @Override
                            public long getBetSum() {
                                return rank.getBetSum();
                            }

                            @Override
                            public long getWinSum() {
                                return rank.getWinSum();
                            }
                        };
                        long score = TournamentObjective.MAX_PERFORMANCE.getScore(prize, currencyRatesManager,
                                accountInfo.getCurrency().getCode(), campaign.getBaseCurrency(), stat, null, null, null, 1, null);
                        rank.setScore(score);
                        tournamentRankPersister.persist(rank);
                        processedCount++;
                    } catch (CommonException e) {
                        LOG.error("Cannot update rank: {}", rank, e);
                    }
                }
                LOG.info("MqTournamentRankUpdater processed={}", processedCount);
            }
        }
    }

    @Override
    public boolean isPlayerIncludedInPromo(IPromoCampaign campaign, IAccountInfo accountInfo) {
        return true;
    }

    @Override
    public PromoCampaignMember getOrCreatePromoMember(IAccountInfo account, IPromoCampaign campaign) throws CommonException {
        CassandraPromoCampaignMembersPersister membersPersister = getPromoCampaignMembersPersister();
        PromoCampaignMember campaignMember = membersPersister.getPromoMember(account.getId(), campaign.getId());
        if (campaignMember == null) {
            String displayName = playerAliasService.generateAlias(account, campaign);
            campaignMember = new PromoCampaignMember(account.getId(), account.getSystemId(), displayName,
                    campaign.getId(), System.currentTimeMillis());
            @SuppressWarnings("unchecked")
            List<DesiredPrize> desiredPrizes = campaign.getTemplate().createDesiredPrizes(campaign.getActionPeriod());
            campaignMember.setDesiredPrizes(desiredPrizes);
            membersPersister.create(campaignMember);
        }
        return campaignMember;
    }

    private class MqTournamentRankUpdater implements Runnable {

        @Override
        public void run() {
            LOG.info("MqTournamentRankUpdater started");
            Set<IPromoCampaign> startedCampaigns = getPromoCampaignPersister().getByStatus(Status.STARTED);
            for (IPromoCampaign campaign : startedCampaigns) {
                updateRanksForMQTournament(campaign);
                if (campaign.isNetworkPromoCampaign()) {
                    NetworkPromoCampaign networkPromoCampaign = (NetworkPromoCampaign) campaign;
                    Set<NetworkPromoEvent> events = networkPromoCampaign.getEvents();
                    for (NetworkPromoEvent event : events) {
                        if (event.getStatus() == Status.STARTED) {
                            updateRanksForMQTournament(event);
                        }
                    }
                }
            }
            LOG.info("MqTournamentRankUpdater finished");
        }
    }

    private class PromoCampaignProcessor implements Runnable {
        @Override
        public void run() {
            if (!initialized) {
                return;
            }
            long now = System.currentTimeMillis();
            String lock = LOCK_ID + PromoCampaignProcessor.class.getSimpleName();
            LockingInfo lockInfo = null;
            try {
                lockInfo = getLockManager().lock(lock, 5000L);
                Set<IPromoCampaign> readyCampaigns = getPromoCampaignPersister().getByStatus(Status.READY);
                for (IPromoCampaign readyCampaign : readyCampaigns) {
                    if (!initialized) {
                        return;
                    }
                    checkAndProcessReadyCampaign(readyCampaign);
                    if (readyCampaign.isNetworkPromoCampaign()) {
                        NetworkPromoCampaign networkPromoCampaign = (NetworkPromoCampaign) readyCampaign;
                        Set<NetworkPromoEvent> events = networkPromoCampaign.getEvents();
                        for (NetworkPromoEvent event : events) {
                            checkAndProcessReadyCampaign(event);
                        }
                    }
                }
                Set<IPromoCampaign> startedCampaigns = getPromoCampaignPersister().getByStatus(Status.STARTED);
                for (IPromoCampaign startedCampaign : startedCampaigns) {
                    if (!initialized) {
                        return;
                    }
                    processStartedCampaign(startedCampaign);
                    if (startedCampaign.isNetworkPromoCampaign()) {
                        NetworkPromoCampaign networkPromoCampaign = (NetworkPromoCampaign) startedCampaign;
                        Set<NetworkPromoEvent> events = networkPromoCampaign.getEvents();
                        for (NetworkPromoEvent event : events) {
                            if (event.getStatus() == Status.READY) {
                                checkAndProcessReadyCampaign(event);
                            } else if (event.getStatus() == Status.STARTED) {
                                processStartedCampaign(event);
                            } else if (event.getStatus() == Status.QUALIFICATION) {
                                LOG.debug("PromoCampaignProcessor: process qualified: {}", event);
                                try {
                                    qualifyPromo(event);
                                } catch (CommonException e) {
                                    LOG.error("Cannot qualify network promo event: {}", event, e);
                                }
                            }
                        }
                    }
                }
                Set<IPromoCampaign> readyForQualifyCampaigns = getPromoCampaignPersister().
                        getByStatus(Status.QUALIFICATION);
                for (IPromoCampaign qualifiedCampaign : readyForQualifyCampaigns) {
                    if (!initialized) {
                        return;
                    }
                    LOG.debug("PromoCampaignProcessor: process qualified: {}", qualifiedCampaign);
                    try {
                        qualifyPromo(qualifiedCampaign);
                    } catch (CommonException e) {
                        LOG.error("Cannot qualify promo: {}", qualifiedCampaign, e);
                    }
                }
            } catch (CannotLockException e) {
                LOG.warn("PromoCampaignProcessor cannot obtain main lock (exit): {}", e.getMessage());
            } catch (Throwable e) {
                LOG.error("PromoCampaignProcessor unexpected error", e);
            } finally {
                if (lockInfo != null) {
                    getLockManager().unlock(lockInfo);
                }
            }
            StatisticsManager.getInstance().updateRequestStatistics("PromoCampaignProcessor: run",
                    System.currentTimeMillis() - now);
        }

        private void checkAndProcessReadyCampaign(IPromoCampaign readyCampaign) {
            LOG.debug("checkAndProcessReadyCampaign: process ready: {}", readyCampaign);
            Date startDate = readyCampaign.getActionPeriod().getStartDate();
            if (System.currentTimeMillis() >= startDate.getTime()) {
                try {
                    changeStatus(readyCampaign, Status.READY, Status.STARTED);
                } catch (CommonException e) {
                    LOG.error("Cannot start promo: {}", readyCampaign, e);
                }
            }
        }

        private void processStartedCampaign(IPromoCampaign startedCampaign) throws CommonException {
            LOG.debug("processStartedCampaign: process started: {}", startedCampaign);
            Date endDate = startedCampaign.getActionPeriod().getEndDate();
            if (System.currentTimeMillis() >= endDate.getTime()) {
                try {
                    changeStatus(startedCampaign, Status.STARTED, Status.QUALIFICATION);
                } catch (CommonException e) {
                    LOG.error("Cannot qualify promo: {}", startedCampaign, e);
                }
            }
        }
    }

    private boolean isFakeCurrency(ICurrency gameSessionCurrency) {
        String currencyCode = gameSessionCurrency.getCode();
        try {
            currencyRatesManager.convert(1.0, currencyCode, ICurrencyRateManager.DEFAULT_CURRENCY);
            return false;
        } catch (CommonException e) {
            LOG.warn("Player will not participate promo due to fake currency = {}", gameSessionCurrency, e);
            return true;
        }
    }

    private String getLockId(long campaignId) {
        return LOCK_ID + campaignId;
    }
}
