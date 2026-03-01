package com.betsoft.casino.mp.data.config;

import com.betsoft.casino.mp.data.persister.*;
import com.betsoft.casino.mp.data.service.ClusterInfoService;
import com.betsoft.casino.mp.data.service.ServerConfigService;
import com.betsoft.casino.mp.service.*;
import com.abs.casino.cassandra.CassandraPersistenceManager;
import com.abs.casino.cassandra.IKeyspaceManager;
import com.abs.casino.common.util.NtpTimeProvider;
import com.hazelcast.cache.HazelcastCachingProvider;
import com.hazelcast.config.*;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.MapStore;
import com.hazelcast.internal.diagnostics.HealthMonitorLevel;
import com.hazelcast.spring.context.SpringManagedContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.spi.CachingProvider;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import static com.hazelcast.spi.properties.GroupProperty.*;

/**
 * User: flsh
 * Date: 04.11.17.
 */
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@Configuration
@PropertySource("classpath:hazelcast.properties")
public class HazelcastContextConfiguration implements ApplicationContextAware {
    private static final Logger LOG = LogManager.getLogger(HazelcastContextConfiguration.class);
    private ApplicationContext applicationContext;

    @Value("${cassandra.keyspace.name}")
    private String mainKeySpace;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Bean
    public SpringManagedContext managedContext() {
        return new SpringManagedContext();
    }

    @Bean
    public Config config(Environment env, ServerConfigPersister serverConfigPersister,
                         RoomTemplatePersister roomTemplatePersister, ServerConfigService serverConfigService,
                         NtpTimeProvider timeProvider, SingleNodeRoomInfoPersister singleNodeRoomInfoPersister,
                         MultiNodeRoomInfoPersister multiNodeRoomInfoPersister,
                         MultiNodeSeatPersister multiNodeSeatPersister,
                         RoomPlayerInfoPersister roomPlayerInfoPersister,
                         BotConfigInfoPersister botConfigInfoPersister,
                         BotServiceConfigPersister botServiceConfigPersister,
                         BGPrivateRoomPlayersStatusPersister bgPrivateRoomPlayersStatusPersister,
                         FriendsPersister friendsPersister,
                         OnlinePlayerPersister onlinePlayerPersister,
                         SocketClientInfoPersister socketClientInfoPersister,
                         SocketClientCountPersister socketClientCountPersister,
                         CrashGameStatePersister crashGameStatePersister,
                         BGPrivateRoomInfoPersister bgPrivateRoomInfoPersister,
                         MultiNodePrivateRoomInfoPersister multiNodePrivateRoomInfoPersister,
                         PendingOperationPersister pendingOperationPersister) {

        Properties properties = new Properties();
        properties.setProperty(SOCKET_BUFFER_DIRECT.getName(), "true");
        properties.setProperty(LOGGING_TYPE.getName(), "log4j2");
        properties.setProperty(HEALTH_MONITORING_LEVEL.getName(), HealthMonitorLevel.SILENT.toString());
        properties.setProperty(HTTP_HEALTHCHECK_ENABLED.getName(), "false");

        properties.setProperty(SHUTDOWNHOOK_ENABLED.getName(), "false");
        properties.setProperty(SHUTDOWNHOOK_POLICY.getName(), "GRACEFUL");

        properties.setProperty(SLOW_OPERATION_DETECTOR_ENABLED.getName(), "true");
        properties.setProperty(SLOW_OPERATION_DETECTOR_STACK_TRACE_LOGGING_ENABLED.getName(), "true");
        //properties.setProperty(SLOW_OPERATION_DETECTOR_LOG_PURGE_INTERVAL_SECONDS.getName(), "60");
        //properties.setProperty(SLOW_OPERATION_DETECTOR_LOG_RETENTION_SECONDS.getName(), "360");
        //properties.setProperty(SLOW_OPERATION_DETECTOR_THRESHOLD_MILLIS.getName(), "10000");

        properties.setProperty(BACKPRESSURE_ENABLED.getName(), "true");
        properties.setProperty(OPERATION_BACKUP_TIMEOUT_MILLIS.getName(), "60000");
        //properties.setProperty(BACKPRESSURE_MAX_CONCURRENT_INVOCATIONS_PER_PARTITION.getName(), "150");
        properties.setProperty(EVENT_THREAD_COUNT.getName(), "10");

        //properties.setProperty("hazelcast.client.heartbeat.timeout", "60000");
        //properties.setProperty(HEARTBEAT_FAILURE_DETECTOR_TYPE.getName(), ClusterFailureDetectorType.DEADLINE.toString());
        properties.setProperty(HEARTBEAT_INTERVAL_SECONDS.getName(), "10");
        properties.setProperty(MAX_NO_HEARTBEAT_SECONDS.getName(), "120");

        ConcurrentMap<String, Object> userContext = new ConcurrentHashMap<>();
        userContext.put("applicationContext", applicationContext);

        String hzVMIP = System.getenv("server.vm.ip");
        String hzVMAddress = hzVMIP + ":"
                + Integer.parseInt(env.getProperty("network.port", "5701"));

        String hzEnvStr = env.getProperty("hz.env");

        LOG.info("Identifying hazelcast env... " + hzEnvStr);

        HzEnv hzEnv = HzEnv.UNKNOWN;
        try {
            hzEnv = HzEnv.valueOf(hzEnvStr.toUpperCase());
        } catch (Exception e) {
            // do nothing
        }

        LOG.info("Starting configuring hazelcast for env type " + hzEnv);

        Config config = new Config(env.getProperty("instance.name"));

        if (HzEnv.GCP.equals(hzEnv) && hzVMIP != null) {
            LOG.info("Setting hazelcast localAddress address to " + hzVMIP);
            config.setProperty("hazelcast.local.localAddress", hzVMIP);
            System.setProperty("hazelcast.local.localAddress", hzVMIP);
        }

        NetworkConfig networkConfig = new NetworkConfig()
                .setPort(Integer.parseInt(env.getProperty("network.port", "5701")))
                .setPortAutoIncrement(Boolean.parseBoolean(env.getProperty("network.port.autoincrement")));

        if (HzEnv.GCP.equals(hzEnv) && hzVMIP != null) {
            LOG.info("Setting hazelcast publicAddress address to " + hzVMAddress);
            networkConfig.setPublicAddress(hzVMAddress);
        }

        JoinConfig join = networkConfig.getJoin();

        // Turn off all join mechanisms by default
        join.getMulticastConfig().setEnabled(false);
        join.getTcpIpConfig().setEnabled(false);
        join.getGcpConfig().setEnabled(false);
        join.getKubernetesConfig().setEnabled(false);

        switch (hzEnv) {
            case GCP:
                @SuppressWarnings("rawtypes") Map<String, Comparable> props = new HashMap<>();
                props.put("projects", env.getProperty("hz.gcp.project"));
                props.put("zones", env.getProperty("hz.gcp.zone"));
                props.put("label", env.getProperty("hz.gcp.label.key") + 
                             "=" + env.getProperty("hz.gcp.label.value"));

                join.getGcpConfig()
                    .setProperty("projects", env.getProperty("hz.gcp.project"))
                    .setProperty("zones", env.getProperty("hz.gcp.zone"))
                    .setProperty("label", env.getProperty("hz.gcp.label.key") + 
                             "=" + env.getProperty("hz.gcp.label.value"));

                join.getGcpConfig().setEnabled(true);
                break;
            case KUBERNETES:
                @SuppressWarnings("rawtypes") Map<String, Comparable> k8sProps = new HashMap<>();
                k8sProps.put("namespace", env.getProperty("hz.kubernetes.namespace"));
                k8sProps.put("service-name", env.getProperty("hz.kubernetes.service-name"));
                k8sProps.put("resolve-not-ready-addresses", true);

                join.getKubernetesConfig()
                    .setProperty("namespace", env.getProperty("hz.kubernetes.namespace"))
                    .setProperty("service-name", env.getProperty("hz.kubernetes.service-name"))
                    .setProperty("resolve-not-ready-addresses", "true");

                join.getKubernetesConfig().setEnabled(true);
                break;
            case LIST:
                join.setTcpIpConfig(new TcpIpConfig()
                        .setEnabled(true)
                        .addMember(env.getProperty("network.join.tcp-ip.members", "local"))
                );
                break;
            case MULTICAST:
            case UNKNOWN:
            default:
                join.getMulticastConfig()
                    .setEnabled(true)
                    .setMulticastGroup("224.2.2.3")
                    .setMulticastPort(54327);
                break;
        }

        config.setNetworkConfig(networkConfig);

        LOG.info("Multicast enabled: " + config.getNetworkConfig().getJoin().getMulticastConfig().isEnabled());
        LOG.info("TCP/IP enabled: " + config.getNetworkConfig().getJoin().getTcpIpConfig().isEnabled());
        LOG.info("Discovery strategy count: " + config.getNetworkConfig().getJoin().getDiscoveryConfig().getDiscoveryStrategyConfigs().size());

        config.setGroupConfig(
                        new GroupConfig(env.getProperty("group.name", "mp"), env.getProperty("group.password", "siberteam"))
                )
                .setManagedContext(managedContext())
                .setManagementCenterConfig(new ManagementCenterConfig()
                        .setEnabled(Boolean.parseBoolean(env.getProperty("management-center.enabled")))
                        .setUrl(env.getProperty("management-center.url"))
                )
                .setNetworkConfig(new NetworkConfig()
                        .setPort(Integer.parseInt(env.getProperty("network.port", "5701")))
                        .setPortAutoIncrement(Boolean.parseBoolean(env.getProperty("network.port.autoincrement")))
                        .setJoin(join)
                )
                .addMapConfig(new MapConfig(RoomTemplateService.ROOM_TEMPLATE_STORE)
                        .setEvictionPolicy(EvictionPolicy.NONE)
                        .setBackupCount(1)
                        .setAsyncBackupCount(1)
                        .setReadBackupData(true)
                        .setMapStoreConfig(new MapStoreConfig()
                                .setEnabled(true)
                                .setInitialLoadMode(MapStoreConfig.InitialLoadMode.EAGER)
                                .setImplementation(roomTemplatePersister)
                        )
                )
                .addMapConfig(getRoomInfoConfig(SingleNodeRoomInfoService.ROOM_INFO_STORE, singleNodeRoomInfoPersister))
                .addMapConfig(getRoomInfoConfig(MultiNodeRoomInfoService.ROOM_INFO_STORE, multiNodeRoomInfoPersister))
                .addMapConfig(getRoomInfoConfig(BGPrivateRoomInfoService.ROOM_INFO_STORE, bgPrivateRoomInfoPersister))
                .addMapConfig(getRoomInfoConfig(MultiNodePrivateRoomInfoService.ROOM_INFO_STORE, multiNodePrivateRoomInfoPersister))
                .addMapConfig(new MapConfig(ServerConfigPersister.CONFIG_STORE)
                        .setBackupCount(1)
                        .setAsyncBackupCount(1)
                        .setEvictionPolicy(EvictionPolicy.NONE)
                        .setMapStoreConfig(new MapStoreConfig()
                                .setEnabled(true)
                                .setInitialLoadMode(MapStoreConfig.InitialLoadMode.EAGER)
                                .setImplementation(serverConfigPersister)
                        )
                )
                .addMapConfig(new MapConfig(BotConfigInfoService.BOT_CONFIG_INFO_STORE)
                        .setBackupCount(1)
                        .setAsyncBackupCount(1)
                        .setEvictionPolicy(EvictionPolicy.NONE)
                        .setMapStoreConfig(new MapStoreConfig()
                                .setEnabled(true)
                                .setInitialLoadMode(MapStoreConfig.InitialLoadMode.EAGER)
                                .setImplementation(botConfigInfoPersister)
                        )
                )
                .addMapConfig(new MapConfig(BotConfigInfoService.BOT_SERVICE_CONFIG)
                        .setBackupCount(1)
                        .setAsyncBackupCount(1)
                        .setEvictionPolicy(EvictionPolicy.NONE)
                        .setMapStoreConfig(new MapStoreConfig()
                                .setEnabled(true)
                                .setInitialLoadMode(MapStoreConfig.InitialLoadMode.EAGER)
                                .setImplementation(botServiceConfigPersister)
                        )
                )
                .addMapConfig(new MapConfig(BGPrivateRoomPlayersStatusService.ROOM_PLAYER_STATUS_STORE)
                        .setBackupCount(1)
                        .setAsyncBackupCount(1)
                        .setEvictionPolicy(EvictionPolicy.NONE)
                        .setMapStoreConfig(new MapStoreConfig()
                                .setEnabled(true)
                                .setInitialLoadMode(MapStoreConfig.InitialLoadMode.EAGER)
                                .setImplementation(bgPrivateRoomPlayersStatusPersister)
                        )
                )
                .addMapConfig(new MapConfig(BGPrivateRoomPlayersStatusService.FRIENDS_STORE)
                        .setBackupCount(1)
                        .setAsyncBackupCount(1)
                        .setEvictionPolicy(EvictionPolicy.NONE)
                        .setMapStoreConfig(new MapStoreConfig()
                                .setEnabled(true)
                                .setInitialLoadMode(MapStoreConfig.InitialLoadMode.EAGER)
                                .setImplementation(friendsPersister)
                        )
                )
                .addMapConfig(new MapConfig(BGPrivateRoomPlayersStatusService.ONLINE_PLAYER_STORE)
                        .setBackupCount(1)
                        .setAsyncBackupCount(1)
                        .setEvictionPolicy(EvictionPolicy.NONE)
                        .setMapStoreConfig(new MapStoreConfig()
                                .setEnabled(true)
                                .setInitialLoadMode(MapStoreConfig.InitialLoadMode.EAGER)
                                .setImplementation(onlinePlayerPersister)
                        )
                )
                .addMapConfig(new MapConfig(RoomPlayersMonitorService.SOCKET_CLIENT_INFO_STORE)
                        .setBackupCount(1)
                        .setAsyncBackupCount(1)
                        .setEvictionPolicy(EvictionPolicy.NONE)
                        .setMapStoreConfig(new MapStoreConfig()
                                .setEnabled(true)
                                .setInitialLoadMode(MapStoreConfig.InitialLoadMode.EAGER)
                                .setImplementation(socketClientInfoPersister)
                        )
                )
                .addMapConfig(new MapConfig(RoomPlayersMonitorService.SOCKET_CLIENT_STATS_STORE)
                        .setBackupCount(1)
                        .setAsyncBackupCount(1)
                        .setEvictionPolicy(EvictionPolicy.NONE)
                        .setTimeToLiveSeconds((int) TimeUnit.HOURS.toSeconds(1))
                        .setMapStoreConfig(new MapStoreConfig()
                                .setEnabled(true)
                                .setInitialLoadMode(MapStoreConfig.InitialLoadMode.EAGER)
                                .setImplementation(socketClientCountPersister)
                        )
                )
                .addMapConfig(new MapConfig(BotManagerService.ACTIVE_BOTS_STORE)
                        .setBackupCount(1)
                        .setAsyncBackupCount(1)
                        .setEvictionPolicy(EvictionPolicy.NONE)
                )
                .addMapConfig(new MapConfig(BotManagerService.BOTS_LOCK_STORE)
                        .setBackupCount(1)
                        .setAsyncBackupCount(1)
                        .setEvictionPolicy(EvictionPolicy.NONE)
                )
                .addMapConfig(new MapConfig(RoomPlayerInfoService.ROOM_PLAYER_INFO_STORE)
                        .setBackupCount(1)
                        .setEvictionPolicy(EvictionPolicy.LRU)
                        .setAsyncBackupCount(1)
                        //.setReadBackupData(true)
                        .setMapStoreConfig(new MapStoreConfig()
                                .setEnabled(true)
                                .setInitialLoadMode(MapStoreConfig.InitialLoadMode.EAGER)
                                .setImplementation(roomPlayerInfoPersister)
                                .setWriteDelaySeconds(1)
                        )
                )
                .addMapConfig(new MapConfig(SharedGameStateService.STATE_STORE)
                        .setBackupCount(1)
                        .setEvictionPolicy(EvictionPolicy.NONE)
                        .setAsyncBackupCount(1)
                        .setMapStoreConfig(new MapStoreConfig()
                                .setEnabled(true)
                                .setInitialLoadMode(MapStoreConfig.InitialLoadMode.EAGER)
                                .setImplementation(crashGameStatePersister)
                                .setWriteDelaySeconds(1)
                        )
                )
                .addMapConfig(new MapConfig(PendingOperationService.OPERATION_STORE)
                        .setBackupCount(1)
                        .setEvictionPolicy(EvictionPolicy.NONE)
                        .setAsyncBackupCount(1)
                        .setMapStoreConfig(new MapStoreConfig()
                                .setEnabled(true)
                                .setInitialLoadMode(MapStoreConfig.InitialLoadMode.EAGER)
                                .setImplementation(pendingOperationPersister)
                                .setWriteDelaySeconds(1)
                        )
                )
                .addMapConfig(new MapConfig(MultiNodeSeatService.SEAT_STORE)
                        .setBackupCount(1)
                        .setEvictionPolicy(EvictionPolicy.NONE)
                        .setAsyncBackupCount(1)
                        //.setReadBackupData(true)
                        .setMapStoreConfig(new MapStoreConfig()
                                .setEnabled(true)
                                .setInitialLoadMode(MapStoreConfig.InitialLoadMode.EAGER)
                                .setImplementation(multiNodeSeatPersister)
                                .setWriteDelaySeconds(1)
                        )
                )
                .addMapConfig(new MapConfig(LobbySessionService.LOBBY_SESSION_STORE)
                        .setBackupCount(1)
                        .setAsyncBackupCount(1)
                        .setEvictionPolicy(EvictionPolicy.LRU)
                        //.setReadBackupData(true)
                        .setTimeToLiveSeconds(12 * 3600)
                )
                .setSerializationConfig(new SerializationConfig()
                        .setGlobalSerializerConfig(new GlobalSerializerConfig()
                                .setImplementation(new GlobalKryoSerializer())
                                .setOverrideJavaSerialization(true))
                )
                .setUserContext(userContext)
                .setProperties(properties);
        MemberAttributeConfig attributeConfig = new MemberAttributeConfig();
        attributeConfig.setIntAttribute(ServerConfigService.MP_SERVER_ID, serverConfigService.getServerId());
        attributeConfig.setLongAttribute(ClusterInfoService.START_TIME, timeProvider.getTime());
        config.setMemberAttributeConfig(attributeConfig);
        return config;
    }

    private MapConfig getRoomInfoConfig(String mapName, MapStore<Long, ?> roomInfoPersister) {
        return new MapConfig(mapName)
                .setBackupCount(1)
                .setAsyncBackupCount(1)
                .setEvictionPolicy(EvictionPolicy.LRU)
                //.setReadBackupData(true)
                .setMapStoreConfig(new MapStoreConfig().
                        setEnabled(true).
                        setInitialLoadMode(MapStoreConfig.InitialLoadMode.EAGER)
                        .setImplementation(roomInfoPersister)
                );
    }

    private IKeyspaceManager getKeyspaceManager(CassandraPersistenceManager persistenceManager) {
        IKeyspaceManager keyspaceManager = persistenceManager.getKeyspaceManager(mainKeySpace);
        if (keyspaceManager == null) {
            Collection<IKeyspaceManager> managers = persistenceManager.getKeyspaceManagers();
            for (IKeyspaceManager manager : managers) {
                LOG.debug("serverConfigMapStore: {}", manager.getKeyspaceName());
            }
            throw new IllegalArgumentException("Cannot find keyspace: " + mainKeySpace);
        }
        return keyspaceManager;
    }

    @Bean
    public ServerConfigPersister serverConfigPersister(CassandraPersistenceManager persistenceManager) {
        return getKeyspaceManager(persistenceManager).getPersister(ServerConfigPersister.class);
    }

    @Bean
    public RoomTemplatePersister roomTemplatePersister(CassandraPersistenceManager persistenceManager) {
        return getKeyspaceManager(persistenceManager).getPersister(RoomTemplatePersister.class);
    }

    @Bean
    public SingleNodeRoomInfoPersister singleNodeRoomInfoPersister(CassandraPersistenceManager persistenceManager) {
        return getKeyspaceManager(persistenceManager).getPersister(SingleNodeRoomInfoPersister.class);
    }

    @Bean
    public MultiNodeRoomInfoPersister multiNodeRoomInfoPersister(CassandraPersistenceManager persistenceManager) {
        return getKeyspaceManager(persistenceManager).getPersister(MultiNodeRoomInfoPersister.class);
    }

    @Bean
    public RoomPlayerInfoPersister roomPlayerInfoPersister(CassandraPersistenceManager persistenceManager) {
        return getKeyspaceManager(persistenceManager).getPersister(RoomPlayerInfoPersister.class);
    }

    @Bean
    public MultiNodeSeatPersister multiNodeSeatPersister(CassandraPersistenceManager persistenceManager) {
        return getKeyspaceManager(persistenceManager).getPersister(MultiNodeSeatPersister.class);
    }

    @Bean
    public BotConfigInfoPersister botConfigInfoPersister(CassandraPersistenceManager persistenceManager) {
        return getKeyspaceManager(persistenceManager).getPersister(BotConfigInfoPersister.class);
    }

    @Bean
    public BotServiceConfigPersister botServiceConfigPersister(CassandraPersistenceManager persistenceManager) {
        return getKeyspaceManager(persistenceManager).getPersister(BotServiceConfigPersister.class);
    }

    @Bean
    public BGPrivateRoomPlayersStatusPersister bgPrivateRoomPlayersStatusPersister(CassandraPersistenceManager persistenceManager) {
        return getKeyspaceManager(persistenceManager).getPersister(BGPrivateRoomPlayersStatusPersister.class);
    }

    @Bean
    public FriendsPersister friendsPersister(CassandraPersistenceManager persistenceManager) {
        return getKeyspaceManager(persistenceManager).getPersister(FriendsPersister.class);
    }

    @Bean
    public OnlinePlayerPersister onlinePlayerPersister(CassandraPersistenceManager persistenceManager) {
        return getKeyspaceManager(persistenceManager).getPersister(OnlinePlayerPersister.class);
    }

    @Bean
    public SocketClientInfoPersister socketClientPersister(CassandraPersistenceManager persistenceManager) {
        return getKeyspaceManager(persistenceManager).getPersister(SocketClientInfoPersister.class);
    }

    @Bean
    public SocketClientCountPersister socketClientCountPersister(CassandraPersistenceManager persistenceManager) {
        return getKeyspaceManager(persistenceManager).getPersister(SocketClientCountPersister.class);
    }

    @Bean
    public CrashGameStatePersister crashGameStatePersister(CassandraPersistenceManager persistenceManager) {
        return getKeyspaceManager(persistenceManager).getPersister(CrashGameStatePersister.class);
    }

    @Bean
    public PendingOperationPersister pendingOperationPersister(CassandraPersistenceManager persistenceManager) {
        return getKeyspaceManager(persistenceManager).getPersister(PendingOperationPersister.class);
    }

    @Bean
    public HazelcastInstance hazelcastInstance(Config config) {
        HazelcastInstance hazelcastInstance = Hazelcast.newHazelcastInstance(config);
        hazelcastInstance.getLifecycleService().addLifecycleListener(LOG::info);
        return hazelcastInstance;
    }

    @Bean
    public CacheManager cacheManager(HazelcastInstance hazelcastInstance, Environment env) throws URISyntaxException {
        URI cacheManagerName = new URI("base-cache-manager");
        Properties properties = HazelcastCachingProvider.propertiesByInstanceItself(hazelcastInstance);
        CachingProvider cachingProvider = Caching.getCachingProvider();
        return cachingProvider.getCacheManager(cacheManagerName, null, properties);
    }

    @Bean
    public BGPrivateRoomInfoPersister bgPrivateRoomInfoPersister(CassandraPersistenceManager persistenceManager) {
        return getKeyspaceManager(persistenceManager).getPersister(BGPrivateRoomInfoPersister.class);
    }

    @Bean
    public MultiNodePrivateRoomInfoPersister multiNodePrivateRoomInfoPersister(CassandraPersistenceManager persistenceManager) {
        return getKeyspaceManager(persistenceManager).getPersister(MultiNodePrivateRoomInfoPersister.class);
    }

    private static enum HzEnv {
        KUBERNETES,
        GCP,
        MULTICAST,
        LIST,
        UNKNOWN
    }
}
