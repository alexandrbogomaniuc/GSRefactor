/**
 * Created by ANGeL
 * Date: Oct 16, 2008
 * Time: 3:37:30 PM
 */
package com.dgphoenix.casino.gs;

import com.dgphoenix.casino.GeoIp;
import com.dgphoenix.casino.account.AccountManager;
import com.dgphoenix.casino.cassandra.AccountDistributedLockManager;
import com.dgphoenix.casino.cassandra.CassandraPersistenceManager;
import com.dgphoenix.casino.cassandra.DistributedLockManager;
import com.dgphoenix.casino.cassandra.persist.*;
import com.dgphoenix.casino.cassandra.persist.mp.BattlegroundHistoryPersister;
import com.dgphoenix.casino.cassandra.persist.mp.RoundKPIInfoPersister;
import com.dgphoenix.casino.common.DomainSession;
import com.dgphoenix.casino.common.SessionHelper;
import com.dgphoenix.casino.common.api.ICommonManager;
import com.dgphoenix.casino.common.cache.*;
import com.dgphoenix.casino.common.cache.data.account.AccountInfo;
import com.dgphoenix.casino.common.cache.data.account.LasthandInfo;
import com.dgphoenix.casino.common.cache.data.bank.BankInfo;
import com.dgphoenix.casino.common.cache.data.bonus.Bonus;
import com.dgphoenix.casino.common.cache.data.bonus.BonusStatus;
import com.dgphoenix.casino.common.cache.data.bonus.BonusSystemType;
import com.dgphoenix.casino.common.cache.data.bonus.FRBonus;
import com.dgphoenix.casino.common.cache.data.game.*;
import com.dgphoenix.casino.common.cache.data.payment.IWallet;
import com.abs.casino.common.cache.data.payment.frb.IFRBonusWin;
import com.dgphoenix.casino.common.cache.data.server.ServerInfo;
import com.dgphoenix.casino.common.cache.data.session.ClientType;
import com.dgphoenix.casino.common.cache.data.session.GameSession;
import com.abs.casino.common.cache.data.session.SessionConstants;
import com.dgphoenix.casino.common.cache.data.session.SessionInfo;
import com.dgphoenix.casino.common.currency.ICurrencyRateManager;
import com.dgphoenix.casino.common.engine.tracker.DelayedExecutor;
import com.dgphoenix.casino.common.exception.*;
import com.dgphoenix.casino.common.games.*;
import com.dgphoenix.casino.common.promo.IPromoCampaignManager;
import com.dgphoenix.casino.common.promo.PromoCampaignMemberInfos;
import com.dgphoenix.casino.common.remotecall.PersistableCall;
import com.dgphoenix.casino.common.util.*;
import com.dgphoenix.casino.common.util.hardware.HardwareConfigurationManager;
import com.dgphoenix.casino.common.util.logkit.LogUtils;
import com.dgphoenix.casino.common.util.string.StringUtils;
import com.dgphoenix.casino.common.util.system.Metric;
import com.dgphoenix.casino.common.util.web.HttpClientConnection;
import com.dgphoenix.casino.common.web.statistics.StatisticsManager;
import com.dgphoenix.casino.gs.managers.ICloseGameProcessor;
import com.dgphoenix.casino.gs.managers.dblink.DBLinkCache;
import com.dgphoenix.casino.gs.managers.dblink.FRBonusDBLink;
import com.dgphoenix.casino.gs.managers.dblink.IDBLink;
import com.dgphoenix.casino.gs.managers.game.engine.GameEngineManager;
import com.dgphoenix.casino.gs.managers.game.engine.IGameEngine;
import com.dgphoenix.casino.gs.managers.payment.IStartGameProcessor;
import com.dgphoenix.casino.gs.managers.payment.bonus.BonusManager;
import com.dgphoenix.casino.gs.managers.payment.bonus.FRBonusManager;
import com.dgphoenix.casino.gs.managers.payment.bonus.FRBonusWinRequestFactory;
import com.dgphoenix.casino.gs.managers.payment.bonus.tracker.BonusTracker;
import com.dgphoenix.casino.gs.managers.payment.bonus.tracker.FRBonusNotificationTracker;
import com.dgphoenix.casino.gs.managers.payment.bonus.tracker.FRBonusWinTracker;
import com.dgphoenix.casino.gs.managers.payment.currency.CurrencyRatesManager;
import com.dgphoenix.casino.gs.managers.payment.transfer.processor.EmptyStartGameProcessor;
import com.dgphoenix.casino.gs.managers.payment.transfer.tracker.PaymentTransactionTracker;
import com.dgphoenix.casino.gs.managers.payment.wallet.WalletPersister;
import com.dgphoenix.casino.gs.managers.payment.wallet.WalletProtocolFactory;
import com.dgphoenix.casino.gs.managers.payment.wallet.tracker.WalletTracker;
import com.dgphoenix.casino.gs.persistance.GameSessionPersister;
import com.dgphoenix.casino.gs.persistance.LasthandPersister;
import com.dgphoenix.casino.gs.persistance.PlayerSessionPersister;
import com.dgphoenix.casino.gs.persistance.remotecall.RemoteCallHelper;
import com.dgphoenix.casino.gs.singlegames.tools.cbservtools.GameSessionStateListenersFactory;
import com.dgphoenix.casino.init.ShutdownFilter;
import com.dgphoenix.casino.leaderboard.LeaderboardWinTracker;
import com.dgphoenix.casino.promo.persisters.CassandraMaxBalanceTournamentPersister;
import com.dgphoenix.casino.services.PlayerBetHistoryService;
import com.dgphoenix.casino.services.bonus.ForbiddenGamesForBonusProvider;
import com.dgphoenix.casino.sm.tracker.logout.LogoutTracker;
import com.dgphoenix.casino.system.MetricsManager;
import com.dgphoenix.casino.system.configuration.GameServerConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationContext;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.dgphoenix.casino.gs.singlegames.tools.cbservtools.IGameSessionStateListener.State;
import static com.google.common.base.Preconditions.checkNotNull;

public class GameServer implements IGameServer, ICommonManager {
    private static final Logger LOG = LogManager.getLogger(com.dgphoenix.casino.gs.GameServer.class);
    private static final com.dgphoenix.casino.gs.GameServer instance = new com.dgphoenix.casino.gs.GameServer();
    public static final long SERVER_INFO_UPDATE_INTERVAL = 5000;

    private final GameServerConfiguration gameServerConfiguration;
    private final int serverId;
    private final ServerInfo serverInfo;
    private volatile boolean initialized;

    private boolean servletContextInitialized;
    private String host;

    private String closeGameProcessorClassName;
    private IStartGameProcessor emptyStartGameProcessor = new EmptyStartGameProcessor();

    private ConcurrentMap<Long, IStartGameProcessor> startGameProcessorMap = new ConcurrentHashMap<>();
    private Map<Long, ICloseGameProcessor> closeGameProcessorMap = new ConcurrentHashMap<>();

    private final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(2);
    private ShutdownFilter shutdownFilter;

    private ICurrencyRateManager currencyConvertor;
    private IPromoCampaignManager promoCampaignManager;
    private CassandraHostCdnPersister hostCdnPersister;
    private CassandraTransactionDataPersister transactionDataPersister;
    private CassandraExtendedAccountInfoPersister extendedAccountInfoPersister;
    private CassandraServerInfoPersister serverInfoPersister;
    private DistributedLockManager distributedLockManager;
    private AccountDistributedLockManager accountDistributedLockManager;
    private CassandraGameSessionExtendedPropertiesPersister gameSessionExtendedPropertiesPersister;
    private RoundKPIInfoPersister roundKPIInfoPersister;
    private CassandraMaxBalanceTournamentPersister maxBalanceTournamentPersister;
    private BattlegroundHistoryPersister battlegroundHistoryPersister;
    private GeoIp geoIp;
    private ForbiddenGamesForBonusProvider forbiddenGamesForBonusProvider;
    private final PlayerBetHistoryService playerBetHistoryService;

    private GameServer() {
        ApplicationContext applicationContext = ApplicationContextHelper.getApplicationContext();
        gameServerConfiguration = applicationContext
                .getBean("gameServerConfiguration", GameServerConfiguration.class);
        serverId = gameServerConfiguration.getServerId();
        serverInfo = applicationContext.getBean("loadBalancerCache", LoadBalancerCache.class)
                .getServerInfoById(serverId);
        checkNotNull(serverInfo, "ServerInfo for this server must not be null");
        promoCampaignManager = applicationContext.getBean("promoCampaignManager", IPromoCampaignManager.class);
        checkNotNull(serverInfo, "ServerInfo for this server must not be null");
        geoIp = applicationContext.getBean("geoIp", GeoIp.class);
        forbiddenGamesForBonusProvider = applicationContext.getBean("forbiddenGamesForBonusProvider", ForbiddenGamesForBonusProvider.class);
        playerBetHistoryService = applicationContext.getBean("playerBetHistoryService", PlayerBetHistoryService.class);
    }

    public static com.dgphoenix.casino.gs.GameServer getInstance() {
        return instance;
    }

    public void registerShutdownFilter(ShutdownFilter shutdownFilter) {
        this.shutdownFilter = shutdownFilter;
    }

    public ShutdownFilter getShutdownFilter() {
        return shutdownFilter;
    }

    /**
     * Init persisters in system on start game server.
     */
    private void initPersisters() {
        CassandraPersistenceManager persistenceManager = ApplicationContextHelper.getApplicationContext()
                .getBean("persistenceManager", CassandraPersistenceManager.class);
        currencyConvertor = ApplicationContextHelper.getApplicationContext()
                .getBean("currencyRatesManager", CurrencyRatesManager.class);
        hostCdnPersister = persistenceManager.getPersister(CassandraHostCdnPersister.class);
        extendedAccountInfoPersister = persistenceManager.getPersister(CassandraExtendedAccountInfoPersister.class);
        transactionDataPersister = persistenceManager.getPersister(CassandraTransactionDataPersister.class);
        serverInfoPersister = persistenceManager.getPersister(CassandraServerInfoPersister.class);
        distributedLockManager = persistenceManager.getPersister(DistributedLockManager.class);
        accountDistributedLockManager = persistenceManager.getPersister(AccountDistributedLockManager.class);
        gameSessionExtendedPropertiesPersister = persistenceManager.getPersister(CassandraGameSessionExtendedPropertiesPersister.class);
        maxBalanceTournamentPersister = persistenceManager.getPersister(CassandraMaxBalanceTournamentPersister.class);
        roundKPIInfoPersister = persistenceManager.getPersister(RoundKPIInfoPersister.class);
        battlegroundHistoryPersister = persistenceManager.getPersister(BattlegroundHistoryPersister.class);
    }

    /**
     * Init game server on start game server. (init persisters, start trackers, register metrics, ...)
     * @throws CommonException if any unexpected error occur
     */
    public void init() throws CommonException {
        if (!initialized) {
            LOG.info("init start");
            initPersisters();
            try {
                StartGameHelpers.getInstance().init(new IHelperCreator() {
                    @Override
                    public IStartGameHelper create(boolean old, long gameId, String servletName, String title,
                                                   String swfLocation,
                                                   String additionalParams, IDelegatedStartGameHelper delegatedHelper) {
                        return new NewTranslationGameHelper(gameId, servletName, title, swfLocation,
                                additionalParams, delegatedHelper, hostCdnPersister);
                    }
                });
            } catch (Throwable t) {
                LOG.error("Cannot initialize StartGameHelpers", t);
            }
            WalletTracker.getInstance().startup();
            PaymentTransactionTracker.getInstance().startup();
            FRBonusWinTracker.getInstance().startup();
            FRBonusNotificationTracker.getInstance().startup();
            BonusTracker.getInstance().startup();
            LeaderboardWinTracker.getInstance().startup();

            executor.scheduleWithFixedDelay(new com.dgphoenix.casino.gs.GameServer.RemoteCallUpdaterTask(), SERVER_INFO_UPDATE_INTERVAL,
                    SERVER_INFO_UPDATE_INTERVAL, TimeUnit.MILLISECONDS);
            LongIdGeneratorFactory.getInstance().addGenerator(IdGenerator.getInstance());
            initialized = true;

            registerMetrics();
            LOG.info("init end");
        }
    }

    /**
     * Registers metrics for statistics of work game server
     */
    private void registerMetrics() {
        MetricsManager.getInstance().register(Metric.CPU,
                () -> (long) HardwareConfigurationManager.getInstance().getCPUPercent());
        MetricsManager.getInstance().register(Metric.MEMORY,
                () -> HardwareConfigurationManager.getInstance().getUsedMemory());
        MetricsManager.getInstance().register(Metric.DB_LINK_CACHE_SIZE,
                () -> DBLinkCache.getInstance().size());
        MetricsManager.getInstance().register(Metric.BASE_GAME_CACHE_SIZE,
                () -> BaseGameCache.getInstance().size());
        MetricsManager.getInstance().register(Metric.EXTERNAL_GAME_IDS_CACHE_SIZE,
                () -> ExternalGameIdsCache.getInstance().size());
        MetricsManager.getInstance().register(Metric.HTTP_CLIENT_CONNECTIONS_COUNT,
                HttpClientConnection::getConnectionsInPool);
        MetricsManager.getInstance().register(Metric.BONUS_TRACKER_TASK_COUNT,
                () -> BonusTracker.getInstance().getTaskCount());
        MetricsManager.getInstance().register(Metric.FR_BONUS_WIN_TRACKER_TASK_COUNT,
                () -> FRBonusWinTracker.getInstance().getTaskCount());
        MetricsManager.getInstance().register(Metric.FR_BONUS_NOTIFY_TRACKER_TASK_COUNT,
                () -> FRBonusNotificationTracker.getInstance().getTaskCount());
        MetricsManager.getInstance().register(Metric.LOGOUT_TRACKER_TASK_COUNT,
                () -> LogoutTracker.getInstance().getTaskCount());
        MetricsManager.getInstance().register(Metric.PAYMENT_TRANSACTION_TRACKER_TASK_COUNT,
                () -> PaymentTransactionTracker.getInstance().getTaskCount());
        MetricsManager.getInstance().register(Metric.WALLET_TRACKER_TASK_COUNT,
                () -> WalletTracker.getInstance().getTaskCount());
        MetricsManager.getInstance().register(Metric.TRANSACTION_DATA_PERSISTER_CACHE_SIZE,
                () -> transactionDataPersister.getCacheSize());
        MetricsManager.getInstance().register(Metric.DELAYED_EXECUTOR_TASK_COUNT,
                () -> DelayedExecutor.getInstance().getTaskCount());
        if (ShutdownFilter.getActiveThreadsCount() != null) {
            MetricsManager.getInstance().register(Metric.SHUTDOWN_FILTER_ACTIVE_THREADS_COUNT, () -> {
                Integer count = ShutdownFilter.getActiveThreadsCount();
                return count == null ? 0 : count;
            });
        } else {
            LOG.warn("Web server not configured for loading active threads count via JMX");
        }
        MetricsManager.getInstance().register(Metric.DOMAIN_SESSION_ACTIVE_TRANSACTIONS_COUNT,
                DomainSession::getActiveTransactionsCount);
        MetricsManager.getInstance().register(Metric.ACCOUNT_DISTRIBUTED_LOCK_MANAGER_LOCAL_LOCKS_COUNT,
                () -> accountDistributedLockManager.getLocalLocksCacheSize());
        MetricsManager.getInstance().register(Metric.ACCOUNT_DISTRIBUTED_LOCK_MANAGER_SERVER_LOCKS_COUNT,
                () -> accountDistributedLockManager.getServerLocksCacheSize());
        MetricsManager.getInstance().register(Metric.DISTRIBUTED_LOCK_MANAGER_LOCAL_LOCKS_COUNT,
                () -> distributedLockManager.getLocalLocksCacheSize());
        MetricsManager.getInstance().register(Metric.DISTRIBUTED_LOCK_MANAGER_SERVER_LOCKS_COUNT,
                () -> distributedLockManager.getServerLocksCacheSize());
    }

    @Override
    public ILongIdGenerator getIdGenerator() {
        return IdGenerator.getInstance();
    }

    public IIntegerIdGenerator getIntegerIdGenerator() {
        return IntegerIdGenerator.getInstance();
    }

    public void initServerConfiguration() {
        String className = gameServerConfiguration.getCloseGameProcessorClassName();
        this.closeGameProcessorClassName = StringUtils.isTrimmedEmpty(className) ? null : className.trim();
    }

    /**
     * Launches server info updater periodically task
     */
    public void dsoStarted() {
        host = "games" + gameServerConfiguration.getGsDomain();
        executor.scheduleWithFixedDelay(new com.dgphoenix.casino.gs.GameServer.ServerInfoUpdaterTask(), SERVER_INFO_UPDATE_INTERVAL,
                SERVER_INFO_UPDATE_INTERVAL, TimeUnit.MILLISECONDS);
    }

    public void destroy() throws CommonException {
        if (initialized) {
            LOG.info("destroy START");
            initialized = false;
            WalletTracker.getInstance().shutdown();
            PaymentTransactionTracker.getInstance().shutdown();
            FRBonusWinTracker.getInstance().shutdown();
            FRBonusNotificationTracker.getInstance().shutdown();
            BonusTracker.getInstance().shutdown();
            LeaderboardWinTracker.getInstance().shutdown();
            ExecutorUtils.shutdownService(getClass().getSimpleName(), executor, 5000);
            DBLinkCache.getInstance().shutdown();

            LOG.info("destroy DONE");
        }
    }

    @Override
    public int getServerId() {
        return serverId;
    }

    @Override
    public String getHost() {
        return host != null ? host : getServerInfo().getHost();
    }

    @Override
    public ServerInfo getServerInfo() {
        return serverInfo;
    }

    @Override
    public boolean isInitialized() {
        return initialized;
    }

    public boolean isServletContextInitialized() {
        return servletContextInitialized;
    }

    public void setServletContextInitialized(boolean flag) {
        servletContextInitialized = flag;
    }

    private void checkInitialized() throws CommonException {
        if (!initialized) {
            throw new CommonException("Not initialized");
        }
    }

    @Override
    public Long startGame(SessionInfo sessionInfo, IBaseGameInfo gameInfo,
                          GameMode mode, Long bonusId, String lang, AccountInfo accountInfo)
            throws CommonException {
        checkMaintenanceMode(mode, lang, accountInfo, gameInfo.getId());
        return startGame(sessionInfo, gameInfo, null, mode, bonusId, lang, accountInfo);
    }

    /**
     * Starts game from start game actions or MQ service handler.
     * @param sessionInfo player session info
     * @param gameInfo game info
     * @param gameSessionId gameSessionId
     * @param mode mode (REAL|FREE|BONUS)
     * @param bonusId bonusId
     * @param lang language
     * @param _accountInfo account info of player
     * @return Long new game session id after start of game.
     * @throws CommonException  if any unexpected error occur
     */
    @Override
    public Long startGame(SessionInfo sessionInfo, IBaseGameInfo gameInfo,
                          Long gameSessionId, GameMode mode, Long bonusId,
                          String lang, AccountInfo _accountInfo) throws CommonException {
        long now = System.currentTimeMillis();
        checkInitialized();
        boolean externallySynchronized = _accountInfo != null;
        long accountId = sessionInfo.getAccountId();
        AccountInfo accountInfo = _accountInfo != null ? _accountInfo : AccountManager.getInstance().
                getAccountInfo(accountId);
        if (accountInfo == null) {
            throw new CommonException("account is null");
        }

        String currencyCode = accountInfo.getCurrency().getCode();
        BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(accountInfo.getBankId());

        if (!bankInfo.isCurrencyCodeAllowed(currencyCode)) {
            throw new CommonException(" Currency is not allowed, code =" + currencyCode + ", bankId="
                    + accountInfo.getBankId());
        }

        Long unclosedGameSessionId = sessionInfo.getGameSessionId();
        GameSession gameSession = unclosedGameSessionId == null ? null :
                GameSessionPersister.getInstance().getGameSession(unclosedGameSessionId);
        long newGameSessionId;
        IDBLink dbLink;
        long now1 = System.currentTimeMillis();
        if (externallySynchronized) {
            if (gameSession != null) {
                closeOnlineGame_impl(accountInfo, sessionInfo, gameSession, false, true,
                        bankInfo, false);
                StatisticsManager.getInstance().updateRequestStatistics("GameServer: closeOnlineGame_impl",
                        System.currentTimeMillis() - now1, accountId);
            }
            dbLink = startGame_impl(accountInfo, sessionInfo, gameInfo, gameSessionId, mode, bonusId, lang, false);
        } else {
            long endBalance = accountInfo.getBalance();
            if (gameSession != null) {
                closeOnlineGame_impl(accountInfo, sessionInfo, gameSession, false, false,
                        bankInfo, false);
                StatisticsManager.getInstance().updateRequestStatistics("GameServer: closeOnlineGame_impl",
                        System.currentTimeMillis() - now1, accountId);
            }
            dbLink = startGame_impl(accountInfo, sessionInfo, gameInfo, gameSessionId, mode, bonusId, lang, false);
        }
        newGameSessionId = dbLink.getGameSessionId();

        try {
            GameSession newGameSession = GameSessionPersister.getInstance().getGameSession(newGameSessionId);
            IStartGameProcessor startGameProcessor = getStartGameProcessor(accountInfo.getBankId());
            startGameProcessor.process(newGameSession, accountInfo, sessionInfo);
        } catch (Exception e) {
            LOG.error("GS::startGameProcessor cannot process gamestart routine:", e);
            if (gameSession != null) {
                closeOnlineGame_impl(accountInfo, sessionInfo, gameSession, false, false,
                        bankInfo, false);
            }
            throw new CommonException(e);
        }

        StatisticsManager.getInstance().updateRequestStatistics("GameServer: startGame",
                System.currentTimeMillis() - now, accountId);
        return newGameSessionId;
    }

    /**
     * Checks if the game is in maintenance mode. It is not possible to start in maintenance mode for real users.
     * @param mode game mode
     * @param lang language
     * @param accountInfo account info
     * @param gameId gameId
     * @throws MaintenanceModeException exception if real player try to start game in maintenance mode
     */
    @Override
    public void checkMaintenanceMode(GameMode mode, String lang, AccountInfo accountInfo,
                                     long gameId) throws MaintenanceModeException {
        long bankId = accountInfo.getBankId();
        IBaseGameInfo defCurrencyBgi = BaseGameCache.getInstance().getGameInfo(bankId, gameId, "");
        if (defCurrencyBgi == null) {
            LOG.warn("Default currency BaseGameInfo is null! bankId={}, gameId={}", bankId, gameId);
        } else if (defCurrencyBgi.isMaintenanceMode() && !GameMode.FREE.equals(mode) && !accountInfo.isTestUser()) {
            BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(bankId);
            StartParameters parameters = new StartParameters(gameId, bankInfo.getExternalBankId(),
                    accountInfo.getSubCasinoId(), lang);
            throw new MaintenanceModeException("MaintenanceModeError: AccountId=" + accountInfo.getId() +
                    ", not allowed to play gameId=" + gameId, parameters);
        }
    }

    /**
     * Returns migration map for process migration data from one to other server.
     * @param bankInfo bank info
     * @return {@code BidirectionalMultivalueMap} migration map
     */
    protected BidirectionalMultivalueMap<Long, Long> getMigrationMap(BankInfo bankInfo) {
        BidirectionalMultivalueMap<Long, Long> gamesMap = bankInfo.getGameMigrationConfigMap();
        return gamesMap == null || gamesMap.isEmpty() ? null : gamesMap;
    }

    /**
     * Returns the gameId of the game to which old games should be migrated.
     * @param bankId bankId
     * @param gameId gameId
     * @return Long gameId
     */
    public Long getNewGame(long bankId, Long gameId) {
        BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(bankId);
        BidirectionalMultivalueMap<Long, Long> gamesMap = getMigrationMap(bankInfo);
        if (gamesMap == null) {
            return null;
        }
        Long newGameId = gamesMap.get(gameId);
        if (newGameId != null) { //allow start oldGame only if have unfinished round
            // check newGeme, may exist
            IBaseGameInfo newGameInfo = BaseGameCache.getInstance().getGameInfoShared(bankId, newGameId,
                    bankInfo.getDefaultCurrency());
            if (newGameInfo != null) {
                if (newGameInfo.isEnabled()) {
                    return newGameId;
                } else {
                    LOG.warn("getNewGame: new game not enable newGameId={}, gameId={}, bankId={}",
                            newGameId, gameId, bankId);
                }
            } else {
                LOG.warn("getNewGame: new game not exist newGameId={}, gameId={}, bankId={}",
                        newGameId, gameId, bankId);
            }
        }
        return null;
    }

    /**
     * Returns a list of old gameId that should migrate to the new gameId.
     * @param bankId bankId
     * @param gameId gameId
     * @return {@code Set<Long>} gameIds of old games
     */
    public Set<Long> getOldGames(long bankId, long gameId) {
        BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(bankId);
        BidirectionalMultivalueMap<Long, Long> gamesMap = getMigrationMap(bankInfo);
        if (gamesMap == null) {
            return null;
        }
        Set<Long> oldGames = gamesMap.getKeysForValue(gameId);
        if (oldGames == null || oldGames.isEmpty()) {
            return null;
        }
        return oldGames;
    }

    /**
     * Internal method start of game.
     * @param accountInfo account info
     * @param sessionInfo player session info
     * @param gameInfo game Info
     * @param gameSessionId gameSessionId
     * @param mode mode (REAL|FREE|BONUS)
     * @param bonusId bonusId
     * @param lang language
     * @param restart true if needed restart
     * @return {@code IDBLink} result of start game will be {@code IDBLink} object dblink with data of started game.
     * @throws CommonException if any unexpected error occur
     */
    private IDBLink startGame_impl(AccountInfo accountInfo, SessionInfo sessionInfo, IBaseGameInfo gameInfo,
                                   Long gameSessionId, GameMode mode, Long bonusId, String lang, boolean restart)
            throws CommonException {
        long now = System.currentTimeMillis();

        if (gameInfo.getId() == 209 && GameMode.BONUS == mode)
            throw new CommonException("CDR can't play in bonus mode");

        if (sessionInfo == null) {
            throw new CommonException("player is not logged in");
        }

        if (accountInfo.isLocked()) {
            throw new AccountLockedException("account is locked");
        }

        if (GameMode.REAL == mode && accountInfo.isGuest()) {
            throw new CommonException("free mode account can't play real mode");
        }

        if (GameMode.BONUS == mode && accountInfo.isGuest()) {
            throw new CommonException("free mode account can't play bonus mode");
        }

        if (GameMode.BONUS == mode && bonusId == null) {
            throw new CommonException("bonusId can't be NULL in BONUS mode");
        }
        long accountId = accountInfo.getId();
        int bankId = accountInfo.getBankId();

        Set<Long> forbiddenGamesForBonus = forbiddenGamesForBonusProvider.getGames(bankId);
        if (GameMode.BONUS == mode && forbiddenGamesForBonus.contains(gameInfo.getId())) {
            throw new CommonException("This game can't play in bonus mode");
        }

        IDBLink dbLink;
        long gameId = gameInfo.getId();
        IBaseGameInfo gameForStart = gameInfo;
        StatisticsManager.getInstance().updateRequestStatistics("GameServer: startGame_impl 1",
                System.currentTimeMillis() - now, accountId);

        long now1 = System.currentTimeMillis();
        try {
            switch (gameInfo.getGameType()) {
                case SP:
                case MP:
                    LOG.debug("startGame_impl: try migration game case: gameId=" + gameId +
                            ", bonusId=" + bonusId + ", mode=" + mode);
                    LasthandInfo lasthandInfo = null;
                    if (mode == GameMode.BONUS) {
                        lasthandInfo = LasthandPersister.getInstance().loadIntoTransactionData(
                                accountId, gameId, bonusId, BonusSystemType.ORDINARY_SYSTEM);
                    } else if (mode == GameMode.REAL) {
                        if (bonusId != null) {
                            lasthandInfo = LasthandPersister.getInstance().loadIntoTransactionData(accountId, gameId,
                                    bonusId, BonusSystemType.FRB_SYSTEM); // FRB
                        } else {
                            lasthandInfo = LasthandPersister.getInstance().loadIntoTransactionData(accountId,
                                    gameId, null, null);
                        }
                    }
                    String lastHand = (lasthandInfo == null) ? "" : lasthandInfo.getLasthandData();
                    dbLink = startSingleGame(accountInfo, sessionInfo, gameForStart, bankId, lastHand,
                            gameSessionId, mode, bonusId, lang, restart);
                    break;
                default:
                    throw new CommonException("gameType:" + gameInfo.getGameType() + " is not supported");
            }

            StatisticsManager.getInstance().updateRequestStatistics("GameServer: startGame_impl 2",
                    System.currentTimeMillis() - now1, accountId);
            now1 = System.currentTimeMillis();
            sessionInfo.setGameSessionId(dbLink.getGameSessionId());
            if (WalletProtocolFactory.getInstance().isWalletBank(bankId) && mode.equals(
                    GameMode.REAL) && !dbLink.isFRBGame()) {
                WalletProtocolFactory.getInstance().interceptCreateWallet(
                        accountInfo, bankId, dbLink.getGameSessionId(), (int) gameId,
                        mode, sessionInfo.getClientType());
            } else if (mode.equals(GameMode.REAL) && dbLink.isFRBGame()) {
                FRBonusWinRequestFactory.getInstance().interceptCreateFRBonusWin(accountInfo, bankId,
                        dbLink.getGameSessionId(), gameId);
                FRBonusDBLink frbonusDBLink = (FRBonusDBLink) dbLink;
                IFRBonusWin frbonusWin = frbonusDBLink.getFrbonusWin();
                if (frbonusWin == null) {
                    LOG.error("FRBonusWin not found, account=" + accountInfo);
                    throw new CommonException("FRBonusWin not found");
                }
                updateDateOfFirstUse(frbonusDBLink);
            }

            StatisticsManager.getInstance().updateRequestStatistics("GameServer: startGame_impl 3",
                    System.currentTimeMillis() - now1, accountId);

        } catch (CommonException e) {
            StatisticsManager.getInstance().updateRequestStatistics("GameServer: startGame_impl error",
                    System.currentTimeMillis() - now, accountId);
            throw e;
        } catch (Throwable e) {
            StatisticsManager.getInstance().updateRequestStatistics("GameServer: startGame_impl error",
                    System.currentTimeMillis() - now, accountId);
            throw new CommonException(e);
        }
        GameSessionStateListenersFactory.getInstance().getGameSessionStateListener(bankId).listen(State.START, dbLink.getGameSession(), bankId);
        if (LOG.isInfoEnabled()) {
            LOG.info("startGame accountId:" + accountId + " gameId:"
                    + gameId + " gameSessionId:" + dbLink.getGameSessionId() + " sessionId:"
                    + sessionInfo.getSessionId() + " started");
        }
        StatisticsManager.getInstance().updateRequestStatistics("GS::startGame_impl",
                System.currentTimeMillis() - now, accountId);
        return dbLink;
    }

    private void updateDateOfFirstUse(FRBonusDBLink frbonusDBLink) {
        FRBonus bonus = FRBonusManager.getInstance().getById(frbonusDBLink.getBonusId());
        if (bonus != null && bonus.getDateOfFirstUse() == null) {
            bonus.setDateOfFirstUse(System.currentTimeMillis());
        }
    }

    /**
     * Starts single game with creation of dblink.
     * @param accountInfo account info of player
     * @param sessionInfo session info of player
     * @param gameInfo gameInfo
     * @param bankId bankId
     * @param lastHand lasthand. It is data of unfinished rounds in game. Actually for usual games only.
     * @param gameSessionId gameSessionId
     * @param mode mode (REAL|FREE|BONUS)
     * @param bonusId bonusId
     * @param lang language
     * @param restart true if needed restart
     * @return {@code IDBLink} result of start game will be {@code IDBLink} object dblink with data of started game.
     * @throws CommonException if any unexpected error occur
     */
    private IDBLink startSingleGame(AccountInfo accountInfo, SessionInfo sessionInfo, IBaseGameInfo gameInfo,
                                    long bankId, String lastHand, Long gameSessionId, GameMode mode, Long bonusId,
                                    String lang, boolean restart)
            throws CommonException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("startSingleGame accountId:" + accountInfo.getId()
                    + " gameId=" + gameInfo.getId() + " mode=" + mode + " lastHand:"
                    + lastHand);
        }

        BaseGameInfoTemplateCache.getInstance().assertGameIsEnabled(gameInfo.getId());

        IGameEngine ge = GameEngineManager.getInstance().getGameEngine(bankId, gameInfo.getId());
        if (ge == null) {
            throw new CommonException("GameEngine not found for gameId=" + gameInfo.getId());
        }
        return ge.registerAccount(accountInfo, mode, lastHand, gameSessionId, bonusId, sessionInfo,
                gameInfo, lang, restart);
    }

    /**
     * Restarts game session with recreation of dblink. Can be needed if dblink moved to other game server.
     * @param sessionInfo player session info
     * @param gameSession game session
     * @return {@code IDBLink} new dblink
     * @throws CommonException if any unexpected error occur
     */
    @Override
    public IDBLink restartGame(SessionInfo sessionInfo, GameSession gameSession)
            throws CommonException {
        checkInitialized();

        LOG.info("restartGame[sessionInfo, gameSession] for player, sessionInfo:"
                + sessionInfo + " gameSession:" + gameSession);

        if (sessionInfo == null || gameSession == null) {
            throw new CommonException("SessionInfo or gameSession is null, can't restart game");
        }

        long accountId = sessionInfo.getAccountId();
        AccountInfo accountInfo = AccountManager.getInstance().getAccountInfo(accountId, false);
        if (accountInfo == null) {
            throw new CommonException("account is null, can't restart game");
        }

        IBaseGameInfo gameInfo = BaseGameCache.getInstance().getGameInfoById(accountInfo.getBankId(), gameSession.getGameId(),
                accountInfo.getCurrencyFraction() == null ? accountInfo.getCurrency() : accountInfo.getCurrencyFraction());
        if (gameInfo == null) {
            LOG.error(LogUtils.markException("gameInfo is null, can't restart game"));
            throw new CommonException("SessionInfo or gameSession is null, can't restart game");
        }
        GameMode mode = gameSession.isBonusGameSession() ? GameMode.BONUS :
                gameSession.isRealMoney() ? GameMode.REAL : GameMode.FREE;
        return startGame_impl(accountInfo, sessionInfo, gameInfo, gameSession.getId(), mode,
                gameSession.isFRBonusGameSession() ? gameSession.getFrbonusId() : gameSession.getBonusId(),
                gameSession.getLang(), true);
    }

    /**
     * Close online game of player.
     * @param gameSessionId gameSessionId
     * @param limitsChanged true, if limits for game session reached.
     * @param serverId serverId
     * @param sessionInfo player session info
     * @param sitOut true, if needed to make sit out for MQ games.
     * @throws CommonException if any unexpected error occur
     */
    @Override
    public void closeOnlineGame(long gameSessionId, boolean limitsChanged,
                                long serverId, SessionInfo sessionInfo, boolean sitOut) throws CommonException {
        checkInitialized();
        LOG.info("closeOnlineGame[by gameSessionId] gameSessionId:" + gameSessionId
                + " limitsChange:" + limitsChanged + " serverId:" + serverId);
        closeOnlineGame(gameSessionId, limitsChanged, sessionInfo, sitOut);
    }

    /**
     * Close online game of player.
     * @param gameSessionId gameSessionId
     * @param limitsChanged true, if limits for game session reached.
     * @param _sessionInfo player session info
     * @param sitOut true, if needed to make sit out for MQ games.
     * @throws CommonException if any unexpected error occur
     */
    private void closeOnlineGame(long gameSessionId, boolean limitsChanged, SessionInfo _sessionInfo, boolean sitOut)
            throws CommonException {
        GameSession gameSession = GameSessionPersister.getInstance().getGameSession(gameSessionId);
        if (gameSession == null) {
            LOG.warn("closeOnlineGame[by gameSessionId] gameSession is NULL gameSessionId:"
                    + gameSessionId + " skipping because of NONE gameSession");
            return;
        }
        SessionInfo sessionInfo = _sessionInfo != null ? _sessionInfo :
                PlayerSessionPersister.getInstance().getSessionInfo();
        if (sessionInfo == null) {
            LOG.error("closeOnlineGame[by gameSessionId] sessionInfo is NULL gameSessionId:"
                    + gameSessionId);
            return;
        }

        long accountId = gameSession.getAccountId();
        AccountInfo accountInfo = AccountManager.getInstance().getAccountInfo(accountId, false);

        if (accountInfo == null) {
            LOG.error("closeOnlineGame[by gameSessionId] accountInfo is NULL "
                    + accountId);
            return;
        }
        long endBalance = accountInfo.getBalance();
        BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(accountInfo.getBankId());
        closeOnlineGame_impl(accountInfo, sessionInfo, gameSession, limitsChanged, false, bankInfo,
                sitOut);
    }

    /**
     * Close online game of player.
     * @param accountInfo accountInfo of player
     * @param sessionInfo player session info
     * @param gameSession game session of player
     * @param limitsChanged true, if limits for game session reached.
     * @param sitOut true, if needed to make sit out for MQ games.
     * @throws CommonException if any unexpected error occur
     */
    @Override
    public void closeOnlineGame(AccountInfo accountInfo, SessionInfo sessionInfo, GameSession gameSession,
                                boolean limitsChanged, boolean sitOut) throws CommonException {
        if (accountInfo == null) {
            throw new CommonException("account not found");
        }
        if (gameSession == null) {
            LOG.error("closeOnlineGame[by gameSession] closing online game skipped because gameSession is null, " +
                    "accountInfo:{}", accountInfo);
            return;
        }
        IBaseGameInfo gameInfo = BaseGameCache.getInstance().getGameInfoById(accountInfo.getBankId(),
                gameSession.getGameId(), accountInfo.getCurrency());
        if (gameInfo == null) {
            LOG.error("closeOnlineGame[by gameSession] closing online game skipped because BaseGameInfo was removed: "
                    + "gameSession={}, accountInfo={}", gameSession, accountInfo);
            return;
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("closeOnlineGame[by gameSession] sessionInfo:{},  gameSession:{} limitsChanged:{}, sitOut={}",
                    sessionInfo, gameSession, limitsChanged, sitOut);
        }
        BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(accountInfo.getBankId());
        closeOnlineGame_impl(accountInfo, sessionInfo, gameSession, limitsChanged, true, bankInfo,
                sitOut);
    }

    /**
     * Checks whether the old game session should be closed first if trying to start another game.
     * @param oldGameSession old game session
     * @param bankInfo bank info
     * @param newGameId new gameId from start game request
     * @return true if needed to close old game session
     */
    @Override
    public boolean needCloseMultiplayerGame(GameSession oldGameSession, BankInfo bankInfo, long newGameId) {
        return isMultiplayerGame(oldGameSession) && oldGameSession.getGameId() != newGameId;
    }

    /**
     * Determines by session whether the game is multiplayer
     * @param gameSession game session
     * @return true, if game is multiplayer (MQ games)
     */
    @Override
    public boolean isMultiplayerGame(GameSession gameSession) {
        if (gameSession == null) {
            return false;
        }
        long gameId = gameSession.getGameId();
        BaseGameInfoTemplate template = BaseGameInfoTemplateCache.getInstance().getBaseGameInfoTemplateById(gameId);
        return template.isMultiplayerGame();
    }

    /**
     *
     * @param accountInfo accountInfo of player
     * @param sessionInfo  player session info
     * @param gameSession game session of player
     * @param limitsChanged true, if limits for game session reached.
     * @param internalFinishClose not used
     * @param bankInfo not used
     * @param sitOut  true, if needed to make sit out for MQ games.
     * @throws CommonException if any unexpected error occur
     */
    private void closeOnlineGame_impl(AccountInfo accountInfo, SessionInfo sessionInfo, GameSession gameSession,
                                      boolean limitsChanged, boolean internalFinishClose,
                                      BankInfo bankInfo, boolean sitOut) throws CommonException {
        int bankId = accountInfo.getBankId();
        boolean walletBank = WalletProtocolFactory.getInstance().isWalletBank(bankId);
        long gameId = gameSession.getGameId();
        BaseGameInfoTemplate template = BaseGameInfoTemplateCache.getInstance().getBaseGameInfoTemplateById(gameId);
        if (!sitOut && template.isMultiplayerGame() && !gameSession.isFRBonusGameSession() && !walletBank) {
            throw new CannotCloseMultiplayerGameException("The game is multiplayer. Closing multiplayer games " +
                    "is allowed only during sitOut",
                    sessionInfo.getSessionId(), gameSession.getId());
        }

        long now = System.currentTimeMillis();
        long accountId = accountInfo.getId();
        closeSingleGame(accountInfo, gameSession, limitsChanged, sessionInfo);

        StatisticsManager.getInstance().updateRequestStatistics("GameServer: closeOnlineGame_impl 1",
                System.currentTimeMillis() - now, accountId);
        now = System.currentTimeMillis();

        long gameSessionId = gameSession.getId();
        boolean realMode = gameSession.isRealMoney();
        long endBalance = accountInfo.getBalance();

        if (walletBank && !gameSession.isBonusGameSession()
                && !gameSession.isFRBonusGameSession()) {
            final IWallet wallet = WalletPersister.getInstance().getWallet(accountInfo.getId());
            WalletProtocolFactory.getInstance().interceptDestroyWallet(accountInfo, bankId, gameSessionId, (int) gameId,
                    realMode ? GameMode.REAL : GameMode.FREE, sessionInfo, wallet);
        } else {
            if (gameSession.isBonusGameSession()) {
                endBalance = closeBonusSession(accountInfo, gameSession);
            } else if (gameSession.isFRBonusGameSession()) {
                FRBonusWinRequestFactory.getInstance().interceptDestroyFRBonusWin(accountInfo, bankId,
                        gameSessionId, gameId,
                        sessionInfo);
                closeFRBonusSession(accountInfo, gameSession);
                if (!walletBank) {
                    closeGameProcess(accountInfo, gameSession, bankId);
                }
            } else {
                if (realMode) {
                    // PaymentManager.getInstance().transferIfNeeded(gameSession,
                    // accountInfo, sessionInfo.getClientType());
                }

                closeGameProcess(accountInfo, gameSession, bankId);
            }
        }
        StatisticsManager.getInstance().updateRequestStatistics("GameServer: closeOnlineGame_impl 2",
                System.currentTimeMillis() - now, accountId);
        now = System.currentTimeMillis();
        GameSessionPersister.getInstance().transferGameSession(gameSession, bankId);
        if (sessionInfo != null) {  //sync closeGameSession
            int lastPlayedMode = sessionInfo.getLastPlayedMode();
            if (lastPlayedMode == SessionConstants.NO_MODE) {
                lastPlayedMode = gameSession.isBonusGameSession() ? SessionConstants.BONUSMODE :
                        realMode ? SessionConstants.REALMODE : SessionConstants.FREEMODE;
            } else {
                boolean changeModeToMixed = (lastPlayedMode == SessionConstants.REALMODE ||
                        lastPlayedMode == SessionConstants.BONUSMODE)
                        && !realMode || lastPlayedMode == SessionConstants.FREEMODE && realMode;
                if (changeModeToMixed) {
                    lastPlayedMode = SessionConstants.MIXED;
                }
            }
            sessionInfo.fireGameSessionClosed(lastPlayedMode);
            if (walletBank) {
                closeGameProcess(accountInfo, gameSession, bankId);
            }

            PromoCampaignMemberInfos promoMemberInfos = SessionHelper.getInstance().getTransactionData()
                    .getPromoMemberInfos();
            if (promoMemberInfos != null && promoMemberInfos.hasAnyWebSocketSupport()) {
                RemoteCallHelper.getInstance().notifySessionClosed(sessionInfo.getSessionId());
            }
        }
        StatisticsManager.getInstance().updateRequestStatistics("GameServer: closeOnlineGame_impl 3",
                System.currentTimeMillis() - now, accountId);
        now = System.currentTimeMillis();

        GameSessionStateListenersFactory.getInstance().getGameSessionStateListener(bankId).
                listen(State.START, gameSession, bankId);

        if (gameSession.hasPromoCampaign()) {
            IPromoCampaignManager campaignManager = GameServerComponentsHelper.getPromoCampaignManager();
            campaignManager.finalizePromoForGameSession(accountId, gameSessionId);
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("closeOnlineGames accountId:" + accountId + " unclosedGameSession:" + gameSession
                    + " successfully closed");
        }
        StatisticsManager.getInstance().updateRequestStatistics("GameServer: closeOnlineGame_impl 5",
                System.currentTimeMillis() - now, accountId);
    }

    private void closeGameProcess(AccountInfo accountInfo, GameSession gameSession, long bankId)
            throws CommonException {
        ICloseGameProcessor closeGameProcessor = getCloseGameProcessor(bankId);
        if (closeGameProcessor != null) {
            try {
                closeGameProcessor.process(gameSession, accountInfo,
                        gameSession.getClientType() != null ? gameSession.getClientType() : ClientType.FLASH);
            } catch (Exception e) {
                LOG.error("closeOnlineGame closegameprocessor exception:", e);
            }
        }
    }

    /**
     * Close FRB session (Free bonus rounds)
     * @param accountInfo accountInfo of player
     * @param gameSession game session
     * @throws CommonException if any unexpected error occur
     */
    private void closeFRBonusSession(AccountInfo accountInfo, GameSession gameSession) throws CommonException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("closeFRBonusSession account: " + accountInfo + ": gameSession:" + gameSession);
        }
        FRBonus frBonus = SessionHelper.getInstance().getTransactionData().getFrBonus();
        if (LOG.isDebugEnabled()) {
            LOG.debug("closeFRBonusSession frBonus=" + frBonus);
        }
        if (frBonus != null) {
            FRBonusManager frBonusManager = FRBonusManager.getInstance();
            if (frBonus.getStatus().equals(BonusStatus.ACTIVE)) {
                if (frBonus.getRoundsLeft() == 0) {
                    frBonusManager.closeBonus(frBonus);
                } else {
                    frBonusManager.flush(frBonus);
                }
            } else {
                frBonusManager.flush(frBonus);
            }
        }
    }

    /**
     * Close Cash bonus session
     * @param accountInfo accountInfo of player
     * @param gameSession game session
     * @throws CommonException if any unexpected error occur
     */
    private long closeBonusSession(AccountInfo accountInfo, GameSession gameSession) throws CommonException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("closeBonusSession account: " + accountInfo + ": gameSession:" + gameSession);
        }
        long endBalance = 0;
        Bonus bonus = SessionHelper.getInstance().getTransactionData().getBonus();
        if (LOG.isDebugEnabled()) {
            LOG.debug("closeBonusSession bonus=" + bonus);
        }
        if (bonus != null) {
            endBalance = bonus.getBalance();
            gameSession.setEndBonusBalance(endBalance);
            if (bonus.isReadyToRelease() && bonus.getStatus().equals(BonusStatus.ACTIVE)) {
                bonus.setLastGameSessionId(gameSession.getId());
                BonusManager.getInstance().releaseBonus(bonus);
            }
            boolean bonusShouldBeLost = BonusManager.getInstance().isBonusShouldBeLost(bonus, accountInfo);
            if (bonusShouldBeLost) {
                BonusManager.getInstance().lostBonus(bonus);
            }
            BaseGameInfo gameInfo = BaseGameInfoTemplateCache.getInstance().getDefaultGameInfo(gameSession.getGameId());
            if (bonus.getStatus() == BonusStatus.RELEASED && gameInfo.getGroup() != GameGroup.ACTION_GAMES) {
                //after release, bonus balance may be changed, if set bonus.maxWinLimit
                accountInfo.incrementBalance(bonus.getBalance(), false);
            }
            BonusManager.getInstance().save(bonus);
            SessionHelper.getInstance().getTransactionData().setBonus(null);
        }
        return endBalance;
    }


    private void closeSingleGame(AccountInfo accountInfo, GameSession gameSession,
                                 boolean limitsChanged, SessionInfo sessionInfo) throws CommonException {
        IGameEngine ge = GameEngineManager.getInstance().getGameEngine(
                accountInfo.getBankId(), gameSession.getGameId());
        ge.closeGameSession(gameSession, limitsChanged, sessionInfo);
    }

    private ICloseGameProcessor getCloseGameProcessor(long bankId) throws CommonException {
        ICloseGameProcessor processor = closeGameProcessorMap.get(bankId);
        if (processor == null) {
            String processorClass = BankInfoCache.getInstance().getBankInfo(bankId).getCloseGameProcessorClass();
            if (processorClass == null) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("No special close game processor has been found for bank:" + bankId + ". Using default.");
                }
                processorClass = closeGameProcessorClassName;
            }
            if (!StringUtils.isTrimmedEmpty(processorClass)) {
                try {
                    processor = (ICloseGameProcessor) ReflectionUtils.forNameWithCompatibilityAliases(processorClass.trim()).newInstance();
                    closeGameProcessorMap.put(bankId, processor);
                } catch (Exception e) {
                    LOG.error("getCloseGameProcessor cannot create new instance of CloseGameProcessor:", e);
                    throw new CommonException(e);
                }
            }
        }
        return processor;
    }

    private IStartGameProcessor getStartGameProcessor(long bankId) throws CommonException {
        IStartGameProcessor processor = startGameProcessorMap.get(bankId);
        if (processor == null) {
            String processorClass = BankInfoCache.getInstance().getBankInfo(bankId).getStartGameProcessorClass();
            if (processorClass != null) {
                try {
                    processor = (IStartGameProcessor) ReflectionUtils.forNameWithCompatibilityAliases(processorClass.trim()).newInstance();
                } catch (Exception e) {
                    LOG.error("GS::getStartGameProcessor cannot create_DMI new instance of StartGameProcessor:", e);
                    throw new CommonException(e);
                }
            } else {
                processor = emptyStartGameProcessor;
            }
            startGameProcessorMap.put(bankId, processor);
        }
        return processor;
    }

    private class ServerInfoUpdaterTask implements Runnable {

        @Override
        public void run() {
            try {
                if (initialized) {
                    serverInfo.touch(NtpTimeProvider.getInstance().getTime());
                    serverInfoPersister.persist(serverInfo);
                } else {
                    LOG.warn("Cannot sync local changes, not initialized");
                }
            } catch (Throwable e) {
                LOG.error("cannot flush local ServerInfo updates to shared cache", e);
            }
        }
    }

    private class RemoteCallUpdaterTask implements Runnable {
        private final CassandraRemoteCallPersister remoteCallPersister;

        public RemoteCallUpdaterTask() {
            CassandraPersistenceManager persistenceManager = ApplicationContextHelper.getApplicationContext()
                    .getBean("persistenceManager", CassandraPersistenceManager.class);
            remoteCallPersister = persistenceManager.getPersister(CassandraRemoteCallPersister.class);
        }

        @Override
        public void run() {
            try {
                if (!initialized) {
                    return;
                }
                List<PersistableCall> remoteCalls = remoteCallPersister.getRemoteCalls(serverId);
                for (PersistableCall remoteCall : remoteCalls) {
                    LOG.info("remoteCall {}", remoteCall);
                    try {
                        remoteCall.call();
                        remoteCallPersister.delete(remoteCall.getServerId(), remoteCall.getId());
                    } catch (CommonException e) {
                        LOG.error("cannot execute remoteCall: {}", remoteCall, e);
                    }
                    if (!initialized) {
                        return;
                    }
                }
            } catch (Throwable e) {
                LOG.error("cannot execute RemoteCallUpdaterTask", e);
            }
        }
    }
}
