package com.betsoft.casino.mp.config;

import com.betsoft.casino.mp.common.GameMapStore;
import com.betsoft.casino.mp.data.config.CassandraContextConfiguration;
import com.betsoft.casino.mp.data.config.HazelcastContextConfiguration;
import com.betsoft.casino.mp.data.persister.*;
import com.betsoft.casino.mp.data.service.ClusterInfoService;
import com.betsoft.casino.mp.data.service.LockService;
import com.betsoft.casino.mp.data.service.ServerConfigService;
import com.betsoft.casino.mp.kafka.KafkaConfiguration;
import com.betsoft.casino.mp.kafka.KafkaMessageService;
import com.betsoft.casino.mp.model.IPrivateRoomPlayersStatusService;
import com.betsoft.casino.mp.server.ServerCoordinatorInfoProvider;
import com.betsoft.casino.mp.server.ServersCoordinatorService;
import com.betsoft.casino.mp.server.ZookeeperConfiguration;
import com.betsoft.casino.mp.server.ZookeeperProperties;
import com.betsoft.casino.mp.server.status.ServersStatusWatcher;
import com.betsoft.casino.mp.service.*;
import com.betsoft.casino.mp.web.GsonFactory;
import com.betsoft.casino.mp.web.GsonMessageSerializer;
import com.betsoft.casino.mp.web.IConfigurableWebSocketHandler;
import com.betsoft.casino.mp.web.IMessageSerializer;
import com.betsoft.casino.mp.web.NettyServer;
import com.betsoft.casino.mp.web.service.*;
import com.betsoft.casino.mp.web.socket.*;
import com.dgphoenix.casino.cassandra.CassandraPersistenceManager;
import com.dgphoenix.casino.cassandra.IRemoteUnlocker;
import com.dgphoenix.casino.common.config.UtilsApplicationContextHelper;
import com.google.gson.Gson;
import com.hazelcast.core.HazelcastInstance;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.*;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.server.WebSocketService;
import org.springframework.web.reactive.socket.server.support.HandshakeWebSocketService;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;
import org.springframework.web.reactive.socket.server.upgrade.TomcatRequestUpgradeStrategy;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * User: flsh
 * Date: 12.08.17.
 */
@Configuration
@EnableWebFlux
@Import({
        HazelcastContextConfiguration.class,
        CassandraContextConfiguration.class,
        KafkaConfiguration.class,
        ZookeeperConfiguration.class
})
@ComponentScan({
        "com.betsoft.casino.mp.web.handlers",
        "com.betsoft.casino.mp.web.socket",
        "com.betsoft.casino.mp.web.controllers"
})
@PropertySource("classpath:core.properties")
public class WebSocketRouter implements ApplicationContextAware {
    private static final Logger LOG = LogManager.getLogger(WebSocketRouter.class);
    private static ApplicationContext applicationContext;

    @Value("${logger.dir}")
    private String loggerDir;

    @Value("${canex.async.request.number.of.threads}")
    private int canexAsyncRequestNumberOfThreads;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    @Bean
    public ServersStatusWatcher serversStatusWatcher(ServerConfigService serverConfigService,
                                                     KafkaMessageService kafkaMessageService,
                                                     ServerCoordinatorInfoProvider serverCoordinatorInfoProvider) {
        return new ServersStatusWatcher(serverConfigService, kafkaMessageService, serverCoordinatorInfoProvider);
    }

    @Bean
    public HandlerMapping webSocketMapping(LobbyWebSocketHandler lobbyHandler, GameWebSocketHandler gameHandler,
                                           UnifiedWebSocketHandler unifiedHandler) {
        LOG.debug("webSocketMapping");
        Map<String, WebSocketHandler> map = new HashMap<>();
        map.put("/websocket/echo", new EchoFluxWebSocketHandler());
        map.put("/websocket/tick", new TickWebSocketHandler());
        map.put("/websocket/mplobby", lobbyHandler);
        map.put("/mplobby", lobbyHandler);
        //todo: add Binary(compressed) MessageSerializer for production
        map.put("/websocket/mpgame", gameHandler);
        map.put("/mpgame", gameHandler);

        map.put("/websocket/mpunified", unifiedHandler);
        map.put("/mpunified", unifiedHandler);

        SimpleUrlHandlerMapping mapping = new SimpleUrlHandlerMapping();
        mapping.setOrder(10);
        mapping.setUrlMap(map);
        return mapping;
    }

    @Bean
    public SampleDataInitializer sampleDataInitializer() {
        LOG.warn("run sampleDataInitializer()");
        return new SampleDataInitializer();
    }

    @Bean
    public Gson gson() {
        return GsonFactory.createGson();
    }

    @Bean
    TransportObjectsFactoryService transportObjectsFactoryService() {
        return new TransportObjectsFactoryService();
    }

    @Bean
    public IMessageSerializer gsonMessageSerializer(Gson gson) {
        return new GsonMessageSerializer(gson);
    }

    @Bean
    public BotGameWebSocketHandler botGameHandler(Gson gson, SingleNodeRoomInfoService singleNodeRoomInfoService,
                                                  MultiNodeRoomInfoService multiNodeRoomInfoService,
                                                  RoomPlayerInfoService playerInfoService,
                                                  RoomServiceFactory roomServiceFactory,
                                                  LobbySessionService lobbySessionService,
                                                  NicknameGenerator nicknameGenerator,
                                                  ServerConfigService serverConfigService,
                                                  CassandraPersistenceManager persistenceManager,
                                                  SocketService socketService, CurrencyRateService currencyRateService,
                                                  CrashGameSettingsService crashGameSettingsService,
                                                  BGPrivateRoomInfoService bgPrivateRoomInfoService,
                                                  MultiNodePrivateRoomInfoService multiNodePrivateRoomInfoService,
                                                  BotConfigInfoService botConfigInfoService,
                                                  PendingOperationService pendingOperationService) {
        return new BotGameWebSocketHandler(new GsonMessageSerializer(gson), singleNodeRoomInfoService, multiNodeRoomInfoService,
                playerInfoService, roomServiceFactory, lobbySessionService, nicknameGenerator, serverConfigService, persistenceManager,
                socketService, currencyRateService, crashGameSettingsService, bgPrivateRoomInfoService, multiNodePrivateRoomInfoService, botConfigInfoService, pendingOperationService);
    }

    @Bean
    public WebSocketHandlerAdapter handlerAdapter(WebSocketService service) {
        LOG.debug("handlerAdapter: create");

        //todo: create custom WebSocketHandlerAdapter, HandshakeWebSocketService for implement handshake redirect logic
        return new WebSocketHandlerAdapter(service);
    }

    @Bean
    public WebSocketService webSocketService() {
        HandshakeWebSocketService webSocketService;
        if (NettyServer.isStandalone()) {
            // NettyServer is standalone (Reactor Netty)
            webSocketService = new HandshakeWebSocketService(new CustomRequestUpgradeStrategy());
        } else {
            // Current (Tomcat) path: raise frame limits
            TomcatRequestUpgradeStrategy strategy = new TomcatRequestUpgradeStrategy();
            strategy.setMaxTextMessageBufferSize(IConfigurableWebSocketHandler.MAX_FRAME_BYTES);
            strategy.setMaxBinaryMessageBufferSize(IConfigurableWebSocketHandler.MAX_FRAME_BYTES);
            // could be: strategy.setMaxSessionIdleTimeout(Duration.ofMinutes(10));
            webSocketService = new HandshakeWebSocketService(strategy);
        }

        LOG.info("webSocketService: {}, using strategy={}",
                webSocketService.getClass().getSimpleName(),
                webSocketService.getUpgradeStrategy().getClass().getSimpleName());

        return webSocketService;
    }

    @Bean
    public LobbyManager lobbyManager() {
        return new LobbyManager();
    }

    @Bean
    @DependsOn({"hazelcastInstance", "mainKeyspaceManager", "persistenceManager", "timeProvider", "lockService",
            "roomPlayerInfoPersister", "botManagerService"})
    public RoomServiceFactory roomServiceFactory(HazelcastInstance hazelcastInstance,
                                                 CassandraPersistenceManager cpm,
                                                 IGameConfigProvider gameConfigProvider,
                                                 ISpawnConfigProvider spawnConfigProvider,
                                                 BotManagerService botManagerService) {
        RoomServiceFactory roomServiceFactory =
                new RoomServiceFactory(getApplicationContext(), hazelcastInstance, cpm, loggerDir, gameConfigProvider, spawnConfigProvider);

        botManagerService.setRoomServiceFactory(roomServiceFactory);

        return roomServiceFactory;
    }

    @Bean
    @DependsOn({"hazelcastInstance", "mainKeyspaceManager", "persistenceManager", "timeProvider"})
    public SingleNodeRoomInfoService singleNodeRoomInfoService(HazelcastInstance hazelcastInstance,
                                                               ServerConfigService serverConfigService,
                                                               RoomTemplateService roomTemplateService,
                                                               RoomPlayerInfoService roomPlayerInfoService,
                                                               IdGenerator idGenerator) {
        return new SingleNodeRoomInfoService(hazelcastInstance, serverConfigService.getServerId(), roomTemplateService,
                roomPlayerInfoService, idGenerator);
    }

    @Bean
    @DependsOn({"hazelcastInstance","canexRequestAsyncExecutor"})
    public IPrivateRoomPlayersStatusService privateRoomPlayersStatusService(HazelcastInstance hazelcastInstance,
                                                                            @Qualifier("canexRequestAsyncExecutor") AsyncExecutorService asyncExecutorService) {
        return new BGPrivateRoomPlayersStatusService(hazelcastInstance, asyncExecutorService);
    }

    @Bean
    @DependsOn({"hazelcastInstance", "mainKeyspaceManager", "persistenceManager", "timeProvider", "privateRoomPlayersStatusService"})
    public BGPrivateRoomInfoService bgPrivateRoomInfoService(HazelcastInstance hazelcastInstance,
                                                             ServerConfigService serverConfigService,
                                                             RoomTemplateService roomTemplateService,
                                                             RoomPlayerInfoService roomPlayerInfoService,
                                                             IdGenerator idGenerator, ISocketService socketService,
                                                             IPrivateRoomPlayersStatusService privateRoomPlayersStatusService) {

        BGPrivateRoomInfoService bgPrivateRoomInfoService = new BGPrivateRoomInfoService(hazelcastInstance,
                serverConfigService.getServerId(), roomTemplateService,
                roomPlayerInfoService, idGenerator, socketService, privateRoomPlayersStatusService);

        privateRoomPlayersStatusService.setBGPrivateRoomInfoService(bgPrivateRoomInfoService);
        privateRoomPlayersStatusService.setSocketService(socketService);

        return bgPrivateRoomInfoService;
    }

    @Bean
    @DependsOn({"hazelcastInstance", "mainKeyspaceManager", "persistenceManager", "timeProvider"})
    public MultiNodePrivateRoomInfoService multiNodePrivateRoomInfoService(HazelcastInstance hazelcastInstance,
                                                             ServerConfigService serverConfigService,
                                                             RoomTemplateService roomTemplateService,
                                                             RoomPlayerInfoService roomPlayerInfoService,
                                                             IdGenerator idGenerator, ISocketService socketService,
                                                             SharedGameStateService sharedGameStateService,
                                                             IPrivateRoomPlayersStatusService privateRoomPlayersStatusService) {

        MultiNodePrivateRoomInfoService multiNodePrivateRoomInfoService = new MultiNodePrivateRoomInfoService(hazelcastInstance,
                serverConfigService.getServerId(), roomTemplateService,
                roomPlayerInfoService, idGenerator, socketService, sharedGameStateService, privateRoomPlayersStatusService);

        privateRoomPlayersStatusService.setMultiNodePrivateRoomInfoService(multiNodePrivateRoomInfoService);

        return multiNodePrivateRoomInfoService;
    }

    @Bean
    @DependsOn({"hazelcastInstance", "mainKeyspaceManager", "persistenceManager", "timeProvider"})
    public MultiNodeRoomInfoService multiNodeRoomInfoService(HazelcastInstance hazelcastInstance,
                                                             ServerConfigService serverConfigService,
                                                             RoomTemplateService roomTemplateService,
                                                             RoomPlayerInfoService roomPlayerInfoService,
                                                             IdGenerator idGenerator,  SharedGameStateService sharedGameStateService) {
        return new MultiNodeRoomInfoService(hazelcastInstance, serverConfigService.getServerId(), roomTemplateService,
                roomPlayerInfoService, idGenerator, sharedGameStateService);
    }

    @Bean
    public IdGenerator idGenerator(SequencerPersister sequencerPersister) {
        return new IdGenerator(sequencerPersister);
    }

    @Bean
    public SequencerPersister sequencerPersister(CassandraPersistenceManager persistenceManager) {
        return persistenceManager.getPersister(SequencerPersister.class);
    }

    @Bean
    @DependsOn({"mainKeyspaceManager", "persistenceManager", "timeProvider"})
    public LockService lockService(LockPersister lockPersister, ClusterInfoService clusterInfoService,
                                   IRemoteUnlocker remoteUnlocker) {
        return new LockService(lockPersister, clusterInfoService, remoteUnlocker);
    }

    @Bean
    public IRemoteUnlocker remoteUnlocker(KafkaMessageService kafkaMessageService) {
        return new RemoteUnlocker(kafkaMessageService);
    }

    @Bean
    @DependsOn({"mainKeyspaceManager", "persistenceManager"})
    public LockPersister lockPersister(CassandraPersistenceManager persistenceManager) {
        return persistenceManager.getPersister(LockPersister.class);
    }

    @Bean
    public ClusterInfoService clusterInfoService(HazelcastInstance hazelcastInstance) {
        return new ClusterInfoService(hazelcastInstance);
    }

    @Bean
    @DependsOn({"hazelcastInstance", "mainKeyspaceManager", "timeProvider", "currencyRateService"})
    public RoomTemplateService roomTemplateService(HazelcastInstance hazelcastInstance,
                                                   CurrencyRateService currencyRateService) {
        return new RoomTemplateService(hazelcastInstance, currencyRateService);
    }

    @Bean
    public ServersCoordinatorService serverIdLockerService(ZookeeperProperties zookeeperProperties) throws Exception {
        return new ServersCoordinatorService(zookeeperProperties);
    }

    @Bean
    public ServerConfigService serverConfigService(ServerConfigPersister serverConfigPersister,
                                                   ServersCoordinatorService serversCoordinatorService) {
        return new ServerConfigService(serverConfigPersister, serversCoordinatorService);
    }

    @Bean
    @DependsOn({"hazelcastInstance", "mainKeyspaceManager", "timeProvider"})
    public RoomPlayerInfoService playerInfoService(HazelcastInstance hazelcastInstance) {
        return new RoomPlayerInfoService(hazelcastInstance);
    }

    @Bean
    @DependsOn({"hazelcastInstance", "mainKeyspaceManager", "timeProvider"})
    public BotConfigInfoService botConfigInfoService(HazelcastInstance hazelcastInstance, IdGenerator idGenerator, NicknameService nicknameService) {
        return new BotConfigInfoService(hazelcastInstance, idGenerator, nicknameService);
    }

    @Bean
    @DependsOn({"hazelcastInstance", "mainKeyspaceManager", "timeProvider"})
    public SharedGameStateService sharedGameStateService(HazelcastInstance hazelcastInstance) {
        return new SharedGameStateService(hazelcastInstance);
    }

    @Bean
    public BigQueryClientService analyticsDBClientService() {
        return new BigQueryClientService();
    }

    @Bean
    @Primary
    public AsyncExecutorService bigQueryAsyncExecutor(){
        int numberOfThreads = (int) Math.ceil(Runtime.getRuntime().availableProcessors());
        return new AsyncExecutorService(1,numberOfThreads,1,TimeUnit.MINUTES,100,new ThreadPoolExecutor.CallerRunsPolicy());
    }
    @Bean
    public AsyncExecutorService canexRequestAsyncExecutor(){
        return new AsyncExecutorService(0,canexAsyncRequestNumberOfThreads,1,TimeUnit.MINUTES,100,new ThreadPoolExecutor.CallerRunsPolicy());
    }

    @Bean
    @DependsOn({"hazelcastInstance", "roomServiceFactory", "canexRequestAsyncExecutor", "analyticsDBClientService"})
    public RoomPlayersMonitorService roomPlayersMonitorService(HazelcastInstance hazelcastInstance,
                                                               IRoomServiceFactory roomServiceFactory,
                                                               ISocketService socketService,
                                                               @Qualifier("canexRequestAsyncExecutor") AsyncExecutorService asyncExecutorService,
                                                               BigQueryClientService analyticsDBClientService) {
        return new RoomPlayersMonitorService(hazelcastInstance, roomServiceFactory, socketService, asyncExecutorService, analyticsDBClientService);
    }

    @Bean
    @DependsOn({"roomPlayersMonitorService"})
    public SocketClientStatisticsService socketClientStatisticsService(RoomPlayersMonitorService roomPlayersMonitorService) {
        return new SocketClientStatisticsService(roomPlayersMonitorService);
    }

    @Bean
    @DependsOn({"hazelcastInstance", "mainKeyspaceManager", "timeProvider"})
    public PendingOperationService pendingOperationService(HazelcastInstance hazelcastInstance) {
        return new PendingOperationService(hazelcastInstance);
    }

    @Bean
    public PendingOperationTracker pendingOperationTracker(PendingOperationService pendingOperationService,
                                                           SocketService socketService, ServerConfigService serverConfigService) {
        return new PendingOperationTracker(pendingOperationService, socketService, serverConfigService);
    }

    @Bean
    @DependsOn({"hazelcastInstance", "mainKeyspaceManager", "botConfigInfoService"})
    public BotManagerService botManagerService(HazelcastInstance hazelcastInstance, BotConfigInfoService botConfigInfoService, BotServiceClient botServiceClient,
                                               SingleNodeRoomInfoService singleNodeRoomInfoService,
                                               MultiNodeRoomInfoService multiNodeRoomInfoService,
                                               ServerConfigService serverConfigService,
                                               LobbySessionService lobbySessionService,
                                               RoomPlayerInfoService roomPlayerInfoService) {
        BotManagerService botManagerService = new BotManagerService(hazelcastInstance, botConfigInfoService, botServiceClient, singleNodeRoomInfoService, multiNodeRoomInfoService,
                serverConfigService, lobbySessionService, roomPlayerInfoService);

        if(botServiceClient != null) {
            botServiceClient.setBotManagerService(botManagerService);
        }

        return botManagerService;
    }

    @Bean
    @DependsOn({"hazelcastInstance", "mainKeyspaceManager", "timeProvider"})
    public MultiNodeSeatService multiNodeSeatService(HazelcastInstance hazelcastInstance) {
        return new MultiNodeSeatService(hazelcastInstance);
    }

    @Bean
    @DependsOn({"hazelcastInstance", "mainKeyspaceManager", "timeProvider"})
    public LobbySessionService lobbySessionService(HazelcastInstance hazelcastInstance) {
        return new LobbySessionService(hazelcastInstance);
    }

    @Bean
    public NicknameValidator nicknameValidator() {
        return new NicknameValidator();
    }

    @Bean
    @DependsOn({"hazelcastInstance", "mainKeyspaceManager", "timeProvider"})
    public SocketService socketService(PendingOperationService pendingOperationService,
                                       KafkaMessageService kafkaMessageService,
                                       ServerConfigService serverConfigService) {
        return new SocketService(pendingOperationService, kafkaMessageService, serverConfigService);
    }

    @Bean
    public BotServiceClient botServiceClient(KafkaMessageService kafkaMessageService) {
        return new BotServiceClient(kafkaMessageService);
    }

    @Bean
    @DependsOn({"hazelcastInstance", "mainKeyspaceManager", "timeProvider"})
    public StatisticsService statisticsService() {
        return new StatisticsService();
    }

    @Bean
    public SocketServer socketServer(ServerConfigService serverConfigService) {
        return new SocketServer(serverConfigService);
    }

    @Bean
    public CrashGameSettingsService crashGameSettingsService(SocketService socketService, CurrencyRateService currencyRateService) {
        return new CrashGameSettingsService(socketService, currencyRateService);
    }

//    @Bean
//    public BotService botService(RoomInfoService roomInfoService, ServerConfigService serverConfigService,
//                                 IMessageSerializer serializer, BotGameWebSocketHandler handler) {
//        return new BotService(roomInfoService, serverConfigService, serializer, handler);
//    }

    @Bean
    public NicknameGenerator nicknameGenerator() {
        return new NicknameGenerator();
    }

    @Bean
    public GameMapStore gameMapStore(CassandraPersistenceManager cpm) {
        return new GameMapStore(cpm.getPersister(MapConfigPersister.class));
    }

    @Bean
    public CurrencyRateService currencyRateService() {
        return new CurrencyRateService();
    }

    @Bean
    public UtilsApplicationContextHelper utilsApplicationContextHelper() {
        return new UtilsApplicationContextHelper();
    }

    @Bean
    public NicknameService nicknameService(CassandraPersistenceManager cpm, SocketService socketService) {
        return new NicknameService(cpm, socketService);
    }

    @Bean
    public IGameConfigProvider gameConfigProvider(CassandraPersistenceManager cpm) {
        return new GameConfigProvider(cpm.getPersister(GameConfigPersister.class));
    }

    @Bean
    public ISpawnConfigProvider spawnConfigProvider(CassandraPersistenceManager cpm) {
        return new SpawnConfigProvider(cpm.getPersister(SpawnConfigPersister.class));
    }
}
