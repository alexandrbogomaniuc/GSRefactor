package com.abs.casino.gs;

import com.dgphoenix.casino.GeoIp;
import com.dgphoenix.casino.account.AccountManager;
import com.dgphoenix.casino.cassandra.AccountDistributedLockManager;
import com.dgphoenix.casino.cassandra.CassandraPersistenceManager;
import com.dgphoenix.casino.cassandra.IRemoteUnlocker;
import com.dgphoenix.casino.cassandra.persist.CassandraAccountInfoPersister;
import com.dgphoenix.casino.cassandra.persist.CassandraTransactionDataPersister;
import com.dgphoenix.casino.common.DomainSessionFactory;
import com.dgphoenix.casino.common.SessionHelper;
import com.dgphoenix.casino.common.api.IAccountInfoPersister;
import com.dgphoenix.casino.common.cache.*;
import com.dgphoenix.casino.common.cache.data.server.ServerCoordinatorInfoProvider;
import com.abs.casino.common.config.HostConfiguration;
import com.dgphoenix.casino.common.currency.ICurrencyRateManager;
import com.dgphoenix.casino.common.lock.ILockManager;
import com.dgphoenix.casino.common.promo.IPrizeWonHandlersFactory;
import com.dgphoenix.casino.common.promo.IPromoCampaignManager;
import com.dgphoenix.casino.common.promo.IPromoCountryRestrictionService;
import com.abs.casino.common.transactiondata.ITransactionDataCreator;
import com.abs.casino.common.transactiondata.ITransactionDataPersister;
import com.dgphoenix.casino.common.transactiondata.storeddate.StoredItemType;
import com.dgphoenix.casino.common.util.CommonExecutorService;
import com.dgphoenix.casino.common.util.IntegerIdGenerator;
import com.abs.casino.common.util.system.SystemPropertyReader;
import com.abs.casino.gs.lock.RemoteUnlocker;
import com.abs.casino.gs.managers.dblink.FreeGameCalculator;
import com.abs.casino.gs.managers.game.history.HistoryInformerManager;
import com.abs.casino.gs.managers.game.settings.DynamicCoinManager;
import com.abs.casino.gs.managers.game.settings.GameSettingsManager;
import com.abs.casino.gs.managers.game.settings.GamesLevelHelper;
import com.dgphoenix.casino.gs.managers.payment.bonus.BonusManager;
import com.dgphoenix.casino.gs.managers.payment.bonus.FRBonusManager;
import com.abs.casino.gs.managers.payment.bonus.mass.DelayedMassAwardManager;
import com.abs.casino.gs.managers.payment.bonus.mass.MassAwardBonusManager;
import com.abs.casino.gs.managers.payment.bonus.tracker.ExpiredMassAwardTracker;
import com.dgphoenix.casino.gs.managers.payment.currency.CurrencyRatesManager;
import com.dgphoenix.casino.gs.managers.payment.wallet.WalletProtocolFactory;
import com.dgphoenix.casino.gs.persistance.bet.PlayerBetPersistenceManager;
import com.dgphoenix.casino.gs.persistance.remotecall.KafkaRequestMultiPlayer;
import com.dgphoenix.casino.gs.persistance.remotecall.RemoteCallHelper;
import com.dgphoenix.casino.gs.GameServer;
import com.dgphoenix.casino.gs.LocalSessionTracker;
import com.dgphoenix.casino.gs.socket.InServiceServiceHandler;
import com.abs.casino.gs.socket.mq.GameUserHistoryService;
import com.dgphoenix.casino.gs.socket.mq.*;
import com.abs.casino.gs.status.ServersStatusWatcher;
import com.abs.casino.init.QuartzInitializer;
import com.abs.casino.kafka.service.KafkaMessageService;
import com.abs.casino.promo.*;
import com.abs.casino.promo.feed.tournament.SummaryTournamentFeedWriter;
import com.abs.casino.promo.messages.handlers.MessagesHandlersFactory;
import com.abs.casino.promo.tournaments.TournamentManager;
import com.abs.casino.promo.tournaments.handlers.TournamentMessageHandlersFactory;
import com.abs.casino.promo.wins.handlers.PrizeWonHandlersFactory;
import com.abs.casino.services.LoginService;
import com.abs.casino.services.PlayerBetHistoryService;
import com.abs.casino.services.geoip.CountryRestrictionService;
import com.abs.casino.services.mp.MPBotConfigInfoService;
import com.dgphoenix.casino.services.mp.MPGameSessionService;
import com.abs.casino.services.tournament.PlayerAliasService;
import com.dgphoenix.casino.support.ErrorPersisterHelper;
import com.dgphoenix.casino.system.configuration.GameServerConfiguration;
import com.abs.casino.tracker.CurrencyUpdateProcessor;
import com.abs.casino.transactiondata.BasicTransactionDataCreator;
import com.abs.casino.transactiondata.storeddataprocessor.*;
import com.abs.casino.websocket.IWebSocketSessionsController;
import com.abs.casino.websocket.WebSocketSessionsController;
import com.abs.casino.websocket.tournaments.TournamentWebSocketSessionsController;
import org.quartz.Scheduler;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import java.util.concurrent.ScheduledExecutorService;

/**
 * User: flsh
 * Date: 28.11.16.
 */
@Configuration
@PropertySource("classpath:gdpr.properties")
public class GameServerComponentsConfiguration {

    @Bean
    TournamentPlaceholdersCache tournamentPlaceholdersCache() {
        return new TournamentPlaceholdersCache();
    }

    @Bean
    IPromoCampaignManager promoCampaignManager(CassandraPersistenceManager persistenceManager,
                                               IPrizeWonHandlersFactory prizeWonHandlersFactory,
                                               HostConfiguration hostConfiguration,
                                               ICurrencyRateManager currencyRatesManager,
                                               KafkaRequestMultiPlayer kafkaRequestMultiPlayer,
                                               ScheduledExecutorService executorService,
                                               PlayerAliasService playerAliasService,
                                               IPromoCountryRestrictionService promoCountryRestrictionService) {
        IAccountInfoPersister accountPersister = persistenceManager.getPersister(CassandraAccountInfoPersister.class);
        return new PromoCampaignManager(
                persistenceManager,
                prizeWonHandlersFactory,
                currencyRatesManager,
                accountPersister,
                hostConfiguration,
                kafkaRequestMultiPlayer,
                executorService,
                playerAliasService,
                promoCountryRestrictionService);
    }

    @Bean
    PlayerAliasService playerAliasService(CassandraPersistenceManager persistenceManager, GameServerConfiguration gameServerConfiguration) {
        return new PlayerAliasService(persistenceManager, IntegerIdGenerator.getInstance(), gameServerConfiguration.getClusterId());
    }

    @Bean
    IPrizeWonHandlersFactory prizeWonHandlersFactory() {
        return new PrizeWonHandlersFactory();
    }

    @Bean
    MessagesHandlersFactory messagesHandlersFactory(IPromoCampaignManager promoCampaignManager,
                                                    CassandraPersistenceManager persistenceManager,
                                                    GameServerConfiguration gameServerConfiguration) {
        return new MessagesHandlersFactory(promoCampaignManager, persistenceManager, gameServerConfiguration);
    }

    @Bean
    IPromoMessagesDispatcher promoMessagesDispatcher(IWebSocketSessionsController webSocketSessionsController,
                                                     IPromoCampaignManager promoCampaignManager,
                                                     TournamentsManager tournamentsManager,
                                                     PromoNotificationsCreator promoNotificationsCreator) {
        return new PromoMessagesDispatcher(webSocketSessionsController, promoCampaignManager, tournamentsManager,
                promoNotificationsCreator);
    }

    @Bean
    IWebSocketSessionsController webSocketSessionsController(MessagesHandlersFactory messagesHandlersFactory, ScheduledExecutorService executorService) {
        return new WebSocketSessionsController(messagesHandlersFactory, executorService);
    }

    @Bean
    SummaryTournamentFeedWriter summaryTournamentFeedWriter(GameServerConfiguration configuration,
                                                            CassandraPersistenceManager persistenceManager) {
        return new SummaryTournamentFeedWriter(configuration, persistenceManager);
    }

    @Bean
    CurrencyUpdateProcessor currencyUpdateProcessor(GameServerConfiguration configuration,
                                                    CassandraPersistenceManager persistenceManager) {
        return new CurrencyUpdateProcessor(configuration, persistenceManager);
    }

    @Bean
    ICurrencyRateManager currencyRatesManager(CassandraPersistenceManager persistenceManager,
                                              CurrencyUpdateProcessor currencyUpdateProcessor) {
        return new CurrencyRatesManager(persistenceManager, currencyUpdateProcessor);
    }

    @Bean
    TournamentsManager tournamentsManager(IPromoCampaignManager promoCampaignManager,
                                          CassandraPersistenceManager persistenceManager, ScheduledExecutorService executor) {
        return new TournamentsManager(promoCampaignManager, persistenceManager, executor);
    }

    @Bean
    RemoteCallHelper remoteCallHelper(IWebSocketSessionsController webSocketSessionsController,
                                      IPromoCampaignManager promoCampaignManager,
                                      InServiceServiceHandler serviceHandler,
                                      KafkaMessageService kafkaMessageService) {
        return new RemoteCallHelper(webSocketSessionsController, promoCampaignManager, serviceHandler, kafkaMessageService);
    }

    @Bean
    PromoNotificationsCreator promoNotificationsCreator(IPromoCampaignManager promoCampaignManager,
                                                        TournamentsManager tournamentsManager) {
        return new PromoNotificationsCreator(promoCampaignManager, tournamentsManager);
    }

    @Bean
    public SchedulerFactoryBean scheduler(ApplicationContext context) {
        SchedulerFactoryBean schedulerFactoryBean = new SchedulerFactoryBean();
        schedulerFactoryBean.setApplicationContext(context);
        return schedulerFactoryBean;
    }

    @Bean
    public QuartzInitializer cronInitializer(Scheduler scheduler, CassandraPersistenceManager persistenceManager) {
        return new QuartzInitializer(scheduler, persistenceManager);
    }

    @Bean
    public HistoryInformerManager historyInformerManager(CassandraPersistenceManager persistenceManager,
                                                         PlayerBetPersistenceManager playerBetPersistenceManager) {
        return new HistoryInformerManager(persistenceManager, playerBetPersistenceManager);
    }

    @Bean
    public InServiceServiceHandler inServiceServiceHandler(MQServiceHandler mqServiceHandler,
                                                           ServersStatusWatcher serversStatusWatcher,
                                                           CassandraPersistenceManager persistenceManager) {
        return new InServiceServiceHandler(mqServiceHandler, serversStatusWatcher, persistenceManager);
    }

    @Bean
    public MQServiceHandler mqServiceHandler(CassandraPersistenceManager persistenceManager,
                                             ICurrencyRateManager currencyRatesManager,
                                             ErrorPersisterHelper errorPersisterHelper,
                                             TournamentBuyInHelper tournamentBuyInHelper,
                                             IPromoCampaignManager promoCampaignManager,
                                             PlayerBetPersistenceManager betPersistenceManager,
                                             AccountManager accountManager,
                                             CommonExecutorService executorService) {
        return new MQServiceHandler(persistenceManager, promoCampaignManager, tournamentBuyInHelper, currencyRatesManager, errorPersisterHelper,
                betPersistenceManager, accountManager, executorService);
    }

    @Bean
    public ServersStatusWatcher serversStatusWatcher(LoadBalancerCache loadBalancerCache,
                                                     KafkaMessageService kafkaMessageService,
                                                     ServerCoordinatorInfoProvider serverCoordinatorInfoProvider) {
        return new ServersStatusWatcher(loadBalancerCache, kafkaMessageService, serverCoordinatorInfoProvider);
    }

    @Bean
    public IRemoteUnlocker remoteUnlocker(KafkaMessageService kafkaMessageService) {
        return new RemoteUnlocker(kafkaMessageService);
    }

    @Bean
    public ExpiredMassAwardTracker expiredFRBonusTracker(ScheduledExecutorService scheduler,
                                                         CassandraPersistenceManager persistenceManager,
                                                         MassAwardBonusManager massAwardBonusManager) {
        return new ExpiredMassAwardTracker(scheduler, persistenceManager, massAwardBonusManager);
    }

    @Bean
    public DelayedMassAwardManager delayedMassAwardManager(CassandraPersistenceManager cpm, ScheduledExecutorService scheduler) {
        return new DelayedMassAwardManager(cpm, scheduler);
    }

    @Bean
    public BackgroundImagesCache backgroundImagesCache() {
        return new BackgroundImagesCache();
    }

    @Bean
    public ErrorPersisterHelper errorPersisterHelper(GameServerConfiguration gameServerConfiguration,
                                                     CassandraPersistenceManager persistenceManager) {
        return new ErrorPersisterHelper(gameServerConfiguration, persistenceManager);
    }

    @Bean
    public TournamentManager tournamentManager(IPromoCampaignManager promoCampaignManager,
                                               TournamentWebSocketSessionsController webSocketSessionsController,
                                               CassandraPersistenceManager cpm,
                                               ICurrencyRateManager currencyRateManager,
                                               GameServerConfiguration configuration) {
        return new TournamentManager(promoCampaignManager, webSocketSessionsController, cpm, currencyRateManager, configuration);
    }

    @Bean
    public TournamentWebSocketSessionsController tournamentWebSocketSessionsController(
            TournamentMessageHandlersFactory handlersFactory,
            RemoteCallHelper remoteCallHelper,
            ScheduledExecutorService executorService) {
        return new TournamentWebSocketSessionsController(handlersFactory, remoteCallHelper, executorService);
    }

    @Bean
    TournamentMessageHandlersFactory tournamentMessageHandlersFactory(IPromoCampaignManager promoCampaignManager,
                                                                      CassandraPersistenceManager cpm,
                                                                      ICurrencyRateManager currencyRatesManager,
                                                                      TournamentBuyInHelper tournamentBuyInHelper,
                                                                      ErrorPersisterHelper errorPersisterHelper,
                                                                      PlayerAliasManager playerAliasManager,
                                                                      MQServiceHandler mqServiceHandler) {
        return new TournamentMessageHandlersFactory(promoCampaignManager, cpm, currencyRatesManager,
                tournamentBuyInHelper, errorPersisterHelper, playerAliasManager, mqServiceHandler);
    }

    @Bean
    public TournamentBuyInHelper tournamentBuyInHelper(ICurrencyRateManager currencyRatesManager,
                                                       CassandraPersistenceManager cpm) {
        return new TournamentBuyInHelper(currencyRatesManager, cpm);
    }

    @Bean
    public GamesLevelHelper gamesLevelHelper(ICurrencyRateManager currencyRateManager) {
        return new GamesLevelHelper(currencyRateManager);
    }

    @Bean
    public DynamicCoinManager dynamicCoinManager(ICurrencyRateManager currencyRateManager, GamesLevelHelper gamesLevelHelper) {
        BankInfoCache bankInfoCache = BankInfoCache.getInstance();
        BaseGameInfoTemplateCache baseGameInfoTemplateCache = BaseGameInfoTemplateCache.getInstance();
        return new DynamicCoinManager(bankInfoCache, baseGameInfoTemplateCache, currencyRateManager, gamesLevelHelper);
    }

    @Bean
    public GameSettingsManager gameSettingsManager(ICurrencyRateManager currencyRateManager, DynamicCoinManager dynamicCoinManager) {
        BaseGameInfoTemplateCache baseGameInfoTemplateCache = BaseGameInfoTemplateCache.getInstance();
        SessionHelper sessionHelper = SessionHelper.getInstance();
        return new GameSettingsManager(currencyRateManager, baseGameInfoTemplateCache, sessionHelper, dynamicCoinManager);
    }

    @Bean
    public PlayerAliasManager getPlayerAliasManager(CassandraPersistenceManager cpm,
                                                    GameServerConfiguration gameServerConfiguration) {
        return new PlayerAliasManager(cpm, gameServerConfiguration);
    }

    @Bean
    public FreeGameCalculator freeGameCalculator(GameServerConfiguration gameServerConfiguration,
                                                 ICurrencyRateManager currencyRateManager,
                                                 GameSettingsManager gameSettingsManager) {
        return new FreeGameCalculator(gameServerConfiguration, currencyRateManager, gameSettingsManager,
                BankInfoCache.getInstance(), BaseGameInfoTemplateCache.getInstance());
    }

    @Bean
    public MassAwardBonusManager massAwardBonusManager(CassandraPersistenceManager persistenceManager, RemoteCallHelper remoteCallHelper) {
        return new MassAwardBonusManager(persistenceManager, remoteCallHelper);
    }

    @Bean
    public CommonExecutorService commonExecutorService() {
        return new CommonExecutorService();
    }

    @Bean
    public BattlegroundService getBattlegroundService(CassandraPersistenceManager cpm, LoginService loginService,
                                                      AccountManager accountManager,
                                                      CommonExecutorService executorService,
                                                      MQServiceHandler mqServiceHandler,
                                                      KafkaRequestMultiPlayer kafkaRequestMultiPlayer) {
        return new BattlegroundService(cpm, BankInfoCache.getInstance(), BaseGameInfoTemplateCache.getInstance(), loginService,
                accountManager, executorService, mqServiceHandler, kafkaRequestMultiPlayer);
    }

    @Bean
    public KafkaRequestMultiPlayer kafkaRequestMultiPlayer(KafkaMessageService kafkaMessageService){
        return new KafkaRequestMultiPlayer(kafkaMessageService);
    }

    @Bean
    public MPBotConfigInfoService getMPBotConfigInfoService(KafkaRequestMultiPlayer kafkaRequestMultiPlayer) {
        return new MPBotConfigInfoService(kafkaRequestMultiPlayer);
    }

    @Bean
    public LoginService loginService() {
        return new LoginService(BankInfoCache.getInstance(), WalletProtocolFactory.getInstance());
    }

    @Bean
    public GameUserHistoryService getGameUserHistoryService(CassandraPersistenceManager cpm, LoginService loginService,
                                                            AccountManager accountManager) {
        return new GameUserHistoryService(loginService, cpm, accountManager, BaseGameInfoTemplateCache.getInstance());
    }

    @Bean
    public FRBonusManager frBonusManager(CassandraPersistenceManager cpm,
                                         KafkaRequestMultiPlayer kafkaRequestMultiPlayer,
                                         MassAwardBonusManager massAwardBonusManager,
                                         ICurrencyRateManager currencyConverter) {
        return new FRBonusManager(cpm, kafkaRequestMultiPlayer, massAwardBonusManager, currencyConverter);
    }

    @Bean
    public BonusManager bonusManager(CassandraPersistenceManager cpm,
                                     KafkaRequestMultiPlayer kafkaRequestMultiPlayer,
                                     MassAwardBonusManager massAwardBonusManager) {
        return new BonusManager(cpm, kafkaRequestMultiPlayer, massAwardBonusManager);
    }

    @Bean
    public CountryRestrictionService countryRestrictionService(GeoIp geoIp, CassandraPersistenceManager persistenceManager) {
        return new CountryRestrictionService(geoIp, persistenceManager);
    }

    @Bean
    public MPGameSessionService mpGameSessionService(CassandraPersistenceManager cpm,
                                                     BattlegroundService battlegroundService,
                                                     KafkaRequestMultiPlayer kafkaRequestMultiPlayer) {
        return new MPGameSessionService(
                BankInfoCache.getInstance(),
                GameServer.getInstance(),
                SessionHelper.getInstance(),
                kafkaRequestMultiPlayer,
                battlegroundService,
                cpm);
    }

    @Bean
    public LocalSessionTracker localSessionTracker() {
        return new LocalSessionTracker();
    }

    @Bean
    public PlayerBetHistoryService playerBetHistoryService(PlayerBetPersistenceManager playerBetPersistenceManager, CassandraPersistenceManager persistenceManager) {
        return new PlayerBetHistoryService(playerBetPersistenceManager, persistenceManager);
    }

    @Bean
    public ITransactionDataCreator basicTransactionDataCreator() {
        return new BasicTransactionDataCreator();
    }

    @Bean
    public DomainSessionFactory domainSessionFactory(CassandraPersistenceManager persistenceManager, LoadBalancerCache loadBalancerCache,
                                                     IRemoteUnlocker remoteUnlocker, ITransactionDataCreator transactionDataCreator,
                                                     AccountManager accountManager, SystemPropertyReader systemPropertyReader,
                                                     ServerCoordinatorInfoProvider serverIdProvider) {
        ILockManager lockManager = initLockManager(persistenceManager, loadBalancerCache, remoteUnlocker, serverIdProvider);
        ITransactionDataPersister transactionDataPersister = initTransactionDataPersister(systemPropertyReader, persistenceManager, serverIdProvider);
        return new DomainSessionFactory(lockManager, transactionDataPersister, transactionDataCreator, accountManager);
    }

    private ILockManager initLockManager(CassandraPersistenceManager persistenceManager, LoadBalancerCache loadBalancerCache,
                                         IRemoteUnlocker remoteUnlocker, ServerCoordinatorInfoProvider serverIdProvider) {
        AccountDistributedLockManager accountDistributedLockManager = persistenceManager.getPersister(AccountDistributedLockManager.class);
        accountDistributedLockManager.setLoadBalancer(loadBalancerCache);
        accountDistributedLockManager.setRemoteUnlocker(remoteUnlocker);
        accountDistributedLockManager.setServerId(serverIdProvider.getServerId());
        return accountDistributedLockManager;
    }

    private ITransactionDataPersister initTransactionDataPersister(SystemPropertyReader systemPropertyReader,
                                                                   CassandraPersistenceManager persistenceManager,
                                                                   ServerCoordinatorInfoProvider serverIdProvider) {
        int serverId = serverIdProvider.getServerId();
        CassandraTransactionDataPersister transactionDataPersister =
                persistenceManager.getPersister(CassandraTransactionDataPersister.class);
        transactionDataPersister.setGameServerId(serverId);
        transactionDataPersister.registerStoredDataProcessors(StoredItemType.GAME_SESSION,
                new GameSessionHistoryChangesProcessor());
        transactionDataPersister.registerStoredDataProcessors(StoredItemType.LASTHAND, new LasthandChangesProcessor());
        transactionDataPersister.registerStoredDataProcessors(StoredItemType.PLAYER_BET, new PlayerBetChangesProcessor());
        transactionDataPersister.registerStoredDataProcessors(StoredItemType.TRANSFER_PLAYER_BET,
                new PlayerBetTransferProcessor());
        transactionDataPersister.registerStoredDataProcessors(StoredItemType.ACCOUNT, new AccountChangesProcessor());
        transactionDataPersister.registerStoredDataProcessors(StoredItemType.PAYMENT_TRANSACTION,
                new PaymentTransactionChangesProcessor());
        transactionDataPersister.registerStoredDataProcessors(StoredItemType.PROMO_TOURNAMENT_RANKS,
                new PromoTournamentRankChangesProcessor());
        transactionDataPersister.registerStoredDataProcessors(StoredItemType.SHORT_BET_INFO,
                new ShortBetInfoChangesProcessor(BankInfoCache.getInstance()));
        transactionDataPersister.registerStoredDataProcessors(StoredItemType.PROMO_MEMBERS,
                new PromoCampaignMembersChangesProcessor());
        return transactionDataPersister;
    }
}
