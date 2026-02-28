package com.abs.casino.init;

import com.abs.casino.cache.CachesHolder;
import com.dgphoenix.casino.cassandra.*;
import com.dgphoenix.casino.common.configuration.ConfigHelper;
import com.abs.casino.common.util.JsonHelper;
import com.dgphoenix.casino.common.util.NtpTimeProvider;
import com.dgphoenix.casino.gs.persistance.bet.PlayerBetPersistenceManager;
import com.abs.casino.init.DefaultConfigsInitializer;
import com.dgphoenix.casino.system.configuration.GameServerConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

/**
 * @author <a href="mailto:fateyev@dgphoenix.com">Anton Fateyev</a>
 * @since 16.09.16
 */
@Configuration
public class CassandraPersistenceContextConfiguration {

    @Bean
    public CachesHolder cachesHolder() {
        return new CachesHolder();
    }

    @Bean
    public IConfigsInitializer configsInitializer(CachesHolder cachesHolder) {
        return new DefaultConfigsInitializer(cachesHolder);
    }

    @Bean
    public PersisterDependencyInjector persisterDependencyInjector() {
        return new PersisterDependencyInjector();
    }

    @Bean
    @DependsOn({"jsonHelper"})
    public CassandraPersistenceManager persistenceManager(NtpTimeProvider timeProvider, IConfigsInitializer configsInitializer,
                                                          PersisterDependencyInjector persisterDependencyInjector) {
        ConfigHelper configHelper = ConfigHelper.getInstance();
        KeyspaceConfigurationFactory configurationFactory = new KeyspaceConfigurationFactory(configHelper, timeProvider);
        KeyspaceManagerFactory managerFactory = new KeyspaceManagerFactory(configurationFactory);
        return new CassandraPersistenceManager(managerFactory, persisterDependencyInjector)
                .withConfigsInitializer(configsInitializer)
                .addKeyspace("ClusterConfig.xml", "cluster.cql")
                .addKeyspace("SCClusterConfig.xml", "sccluster.cql");
    }

    @Bean
    PlayerBetPersistenceManager playerBetPersistenceManager(GameServerConfiguration gameServerConfiguration,
                                                            CassandraPersistenceManager persistenceManager) {
        return new PlayerBetPersistenceManager(gameServerConfiguration, persistenceManager);
    }

    @Bean("jsonHelper")
    public JsonHelper jsonHelper() {
        return new JsonHelper("com.dgphoenix.casino.*");
    }
}
