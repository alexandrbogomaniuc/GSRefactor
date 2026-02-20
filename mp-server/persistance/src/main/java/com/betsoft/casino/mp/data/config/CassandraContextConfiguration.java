package com.betsoft.casino.mp.data.config;

import com.dgphoenix.casino.cassandra.*;
import com.dgphoenix.casino.common.configuration.ConfigHelper;
import com.dgphoenix.casino.common.util.JsonHelper;
import com.dgphoenix.casino.common.util.NtpTimeProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.PropertySource;

import static org.apache.commons.lang3.StringUtils.isBlank;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * User: flsh
 * Date: 04.11.17.
 */
@Configuration
@PropertySource("classpath:persistance.properties")
public class CassandraContextConfiguration implements ApplicationContextAware {
    private static final Logger LOG = LogManager.getLogger(CassandraContextConfiguration.class);

    public static final String NTP_HOST = "ntp.host";

    @Value("${cassandra.keyspace.name}")
    private String mainKeySpace;

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Bean("timeProvider")
    public NtpTimeProvider timeProvider() {
        NtpTimeProvider timeProvider = new NtpTimeProvider();
        String ntpServerHost = System.getProperty(NTP_HOST);
        if (isBlank(ntpServerHost)) {
            LOG.warn("timeProvider: ntp.host variable not found, use 'metadata.google.internal' as we run on GCP");
            ntpServerHost = "metadata.google.internal";
            try {
                InetAddress.getByName(ntpServerHost);
            } catch (UnknownHostException e) {
                LOG.warn(ntpServerHost + " resolve failed, use 'pool.ntp.org' as we run on GCP");
                ntpServerHost = "pool.ntp.org";
            }
        }
        timeProvider.start(ntpServerHost);
        LOG.debug("timeProvider: started with, ntpServerHost={}", ntpServerHost);
        return timeProvider;
    }

    @Bean
    public PersisterDependencyInjector persisterDependencyInjector() {
        return new PersisterDependencyInjector();
    }

    @Bean("persistenceManager")
    @DependsOn({"jsonHelper"})
    public CassandraPersistenceManager persistenceManager(NtpTimeProvider timeProvider, PersisterDependencyInjector persisterDependencyInjector) {
        KeyspaceConfigurationFactory configurationFactory = new KeyspaceConfigurationFactory(ConfigHelper.getInstance(),
                timeProvider);
        KeyspaceManagerFactory managerFactory = new KeyspaceManagerFactory(configurationFactory);
        return new CassandraPersistenceManager(managerFactory, persisterDependencyInjector)
                .addKeyspace("mp-keyspace-config.xml", "metadata-init.cql");
    }

    @Bean("mainKeyspaceManager")
    public IKeyspaceManager mainKeyspaceManager(CassandraPersistenceManager persistenceManager) {
        return persistenceManager.getKeyspaceManager(mainKeySpace);
    }

    @Bean("jsonHelper")
    public JsonHelper jsonHelper() {
        return new JsonHelper("com.betsoft.casino.mp.*");
    }
}
