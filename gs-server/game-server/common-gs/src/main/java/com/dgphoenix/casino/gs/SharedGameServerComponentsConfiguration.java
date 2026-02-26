package com.abs.casino.gs;

import com.dgphoenix.casino.GeoIp;
import com.dgphoenix.casino.account.AccountManager;
import com.dgphoenix.casino.cassandra.CassandraPersistenceManager;
import com.dgphoenix.casino.cassandra.persist.CassandraServerInfoPersister;
import com.dgphoenix.casino.common.cache.LoadBalancerCache;
import com.dgphoenix.casino.common.cache.ServerConfigsCache;
import com.dgphoenix.casino.common.cache.data.server.ServerCoordinatorInfoProvider;
import com.dgphoenix.casino.common.cache.data.server.ServerInfo;
import com.abs.casino.common.config.HostConfiguration;
import com.dgphoenix.casino.common.config.UtilsApplicationContextHelper;
import com.dgphoenix.casino.common.util.ITimeProvider;
import com.dgphoenix.casino.common.util.NtpTimeProvider;
import com.dgphoenix.casino.common.util.system.SystemPropertyReader;
import com.dgphoenix.casino.common.web.SharedServletExecutorService;
import com.dgphoenix.casino.system.configuration.GameServerConfiguration;
import com.dgphoenix.casino.system.configuration.identification.ServersCoordinatorService;
import com.dgphoenix.casino.system.configuration.identification.ZookeeperProperties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

/**
 * Configuration for components which can be used outside the game server.
 */
@Configuration
public class SharedGameServerComponentsConfiguration {
    @Bean
    public ServerCoordinatorInfoProvider serverIdLockerService(ZookeeperProperties zookeeperProperties) throws Exception {
        return new ServersCoordinatorService(zookeeperProperties);
    }

    @Bean
    @DependsOn("serverIdLockerService")
    public ServerConfigsCache serverConfigsCache(CassandraPersistenceManager persistenceManager,
                                                 ServerCoordinatorInfoProvider serverIdProvider) {
        CassandraServerInfoPersister serverInfoPersister = persistenceManager.getPersister(CassandraServerInfoPersister.class);
        return new ServerConfigsCache(serverInfoPersister, serverIdProvider.getServerId());
    }

    @Bean
    @DependsOn("serverConfigsCache")
    public GameServerConfiguration gameServerConfiguration(GeoIp geoIp, CassandraPersistenceManager persistenceManager,
                                                           ServerCoordinatorInfoProvider serverIdProvider) {
        return new GameServerConfiguration(ServerConfigsCache.getInstance(), geoIp, persistenceManager, serverIdProvider);
    }

    @Bean
    @DependsOn("serverIdLockerService")
    public HostConfiguration hostConfiguration( @Value("${CLUSTER_TYPE}") String clusterType, ServerCoordinatorInfoProvider serverIdProvider) {
        return new HostConfiguration(clusterType, serverIdProvider.getServerId(), ServerConfigsCache.getInstance());
    }

    @Bean
    public UtilsApplicationContextHelper utilsApplicationContextHelper() {
        return new UtilsApplicationContextHelper();
    }

    @Bean
    public GeoIp geoIp() {
        return new GeoIp();
    }

    @Bean
    @DependsOn("utilsApplicationContextHelper")
    public NtpTimeProvider timeProvider() {
        return new NtpTimeProvider();
    }

    @Bean
    @DependsOn("serverIdLockerService")
    public LoadBalancerCache loadBalancerCache(CassandraPersistenceManager persistenceManager,
                                               GameServerConfiguration gameServerConfiguration,
                                               ServerCoordinatorInfoProvider serverCoordinatorInfoProvider,
                                               ITimeProvider timeProvider) {
        CassandraServerInfoPersister serverInfoPersister = persistenceManager.getPersister(CassandraServerInfoPersister.class);
        ServerInfo thisServerInfo = gameServerConfiguration.composeServerInfo(timeProvider.getTime());
        return new LoadBalancerCache(serverInfoPersister, serverCoordinatorInfoProvider, thisServerInfo);
    }

    @Bean
    public SharedServletExecutorService sharedServletExecutorService() {
        return new SharedServletExecutorService();
    }

    @Bean
    public AccountManager accountManager(GameServerConfiguration gameConfig, CassandraPersistenceManager persistenceManager) {
        return new AccountManager(gameConfig.getCasinoSystemType(), persistenceManager);
    }

    @Bean
    public SystemPropertyReader systemPropertyReader() {
        return new SystemPropertyReader();
    }
}
