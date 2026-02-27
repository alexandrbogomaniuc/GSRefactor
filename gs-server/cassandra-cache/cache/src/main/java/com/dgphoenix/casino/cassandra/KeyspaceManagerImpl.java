package com.abs.casino.cassandra;

import com.abs.casino.cassandra.persist.engine.ICassandraPersister;
import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.LinkedHashSet;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * @author <a href="mailto:fateyev@dgphoenix.com">Anton Fateyev</a>
 * @since 19.09.16
 */
public class KeyspaceManagerImpl implements IKeyspaceManager {

    private static final Logger LOG = LogManager.getLogger(KeyspaceManagerImpl.class);
    private static final long ONLINE_HOSTS_CHECK_INTERVAL = TimeUnit.SECONDS.toMillis(10);

    private final KeyspaceConfiguration configuration;
    private final PersistersFactory persistersFactory;
    private final SchemaCreator schemaCreator;

    private boolean initialized;
    private com.datastax.driver.core.Cluster cluster;
    private com.datastax.driver.core.Session session;

    public KeyspaceManagerImpl(KeyspaceConfiguration configuration, PersistersFactory persistersFactory,
                               String schemaUpdateFilename) {
        LOG.debug("Creating keyspace manager...");
        checkNotNull(configuration, "Configuration should be specified");
        checkNotNull(persistersFactory, "Persistence factory should be specified");
        this.configuration = configuration;
        this.persistersFactory = persistersFactory;
        this.schemaCreator = new SchemaCreator(configuration.getClusterConfig(), schemaUpdateFilename);
        LOG.debug("Keyspace manager created");
    }

    @Override
    public String getKeyspaceName() {
        return configuration.getKeyspaceName();
    }

    /**
     * Internal/legacy accessor retained for persister engine compatibility.
     */
    public com.datastax.driver.core.Session getSession() {
        return session;
    }

    @Override
    public KeyspaceMetricsSnapshot getMetricsSnapshot() {
        if (!initialized || cluster == null) {
            return KeyspaceMetricsSnapshot.unavailable();
        }
        return KeyspaceMetricsSnapshot.from(cluster.getMetrics());
    }

    @Override
    public <P extends ICassandraPersister> P getPersister(Class<P> persisterClass) {
        return persistersFactory.getPersister(persisterClass);
    }

    @Override
    public <P> List<P> getPersistersByInterface(Class<P> persisterInterface) {
        return persistersFactory.getPersistersByInterface(persisterInterface);
    }

    @Override
    public List<ICassandraPersister> getAllPersisters() {
        return ImmutableList.copyOf(persistersFactory.getAllPersisters());
    }

    @Override
    public boolean isReady() {
        return initialized && cluster != null && !cluster.isClosed() && session != null && !session.isClosed();
    }

    /**
     * Internal/legacy host view retained for cache-module compatibility tests.
     */
    public Set<com.datastax.driver.core.Host> getDownHosts() {
        if (!initialized) {
            return Collections.emptySet();
        }

        return cluster.getMetadata().getAllHosts().stream()
                .filter(host -> !host.isUp())
                .collect(Collectors.toSet());
    }

    @Override
    public Set<String> getDownHostAddresses() {
        if (!initialized || cluster == null || cluster.getMetadata() == null) {
            return Collections.emptySet();
        }

        return cluster.getMetadata().getAllHosts().stream()
                .filter(host -> !host.isUp())
                .map(this::extractHostAddress)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    public Set<String> getAllHostAddresses() {
        if (!initialized || cluster == null || cluster.getMetadata() == null) {
            return Collections.emptySet();
        }

        return cluster.getMetadata().getAllHosts().stream()
                .map(this::extractHostAddress)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    public Set<String> getJmxHosts() {
        return configuration.getJmxHosts();
    }

    @Override
    public void init() {
        checkState(!initialized, "Already initialized");
        String keyspaceName = configuration.getKeyspaceName();
        LOG.info("Start initialization manager for keyspace: {} with configuration ({})",
                keyspaceName, configuration.getClusterConfig());
        persistersFactory.initializePersisters(configuration.getPersistersConfigs(),
                configuration.getReadConsistencyLevel(),
                configuration.getWriteConsistencyLevel(),
                configuration.getSerialConsistencyLevel());

        cluster = configuration.buildCluster(com.datastax.driver.core.Cluster.builder());
        com.datastax.driver.core.KeyspaceMetadata metadata = cluster.getMetadata().getKeyspace(keyspaceName);
        try (com.datastax.driver.core.Session schemaSession = cluster.connect()) {
            List<ICassandraPersister> persisters = persistersFactory.getAllPersisters();
            if (metadata == null) {
                checkState(configuration.isCreateSchema(), "Cassandra schema not found and schema creation disabled");
                schemaCreator.createSchema(schemaSession, persisters);
            } else {
                schemaCreator.updateSchema(metadata, schemaSession, persisters);
            }
        }

        session = new com.abs.casino.cassandra.persist.engine.Session(keyspaceName, cluster.connect(keyspaceName));

        try {
            awaitOnlineHosts(configuration.getMinimumOnlineHosts(), configuration.getLocalDataCenterName());
        } catch (InterruptedException e) {
            LOG.info("Waiting online hosts was interrupted", e);
            session.close();
            throw new RuntimeException(e);
        }

        persistersFactory.populateSession(session);
        LOG.info("Complete initialize manager for keyspace: {}", keyspaceName);
        initialized = true;
    }

    private void awaitOnlineHosts(long minimumOnlineHosts, String localDataCenter) throws InterruptedException {
        if (minimumOnlineHosts <= 0) {
            LOG.info("AwaitOnlineHosts:: checking minimumOnlineHosts is disabled");
            return;
        }

        int onlineHosts = getOnlineHostsCount(localDataCenter);
        while (onlineHosts < minimumOnlineHosts) {
            LOG.info("Not enough online hosts, online={}, minimum={}", onlineHosts, minimumOnlineHosts);
            Thread.sleep(ONLINE_HOSTS_CHECK_INTERVAL);
            onlineHosts = getOnlineHostsCount(localDataCenter);
        }
    }

    private int getOnlineHostsCount(String localDataCenter) {
        Predicate<com.datastax.driver.core.Host> isRequiredDataCenterHost = host -> StringUtils.isBlank(localDataCenter) || localDataCenter.equals(host.getDatacenter());
        return (int) cluster.getMetadata().getAllHosts().stream()
                .filter(isRequiredDataCenterHost)
                .filter(host -> {
                    boolean hostUp = host.isUp();
                    if (!hostUp) {
                        LOG.debug("Cassandra. com.datastax.driver.core.Host {} is down", host);
                    }
                    return hostUp;
                })
                .count();
    }

    private String extractHostAddress(com.datastax.driver.core.Host host) {
        if (host == null) {
            return null;
        }
        SocketAddress socketAddress = host.getSocketAddress();
        if (socketAddress instanceof InetSocketAddress) {
            InetSocketAddress inetSocketAddress = (InetSocketAddress) socketAddress;
            String hostString = inetSocketAddress.getHostString();
            if (StringUtils.isNotBlank(hostString)) {
                return hostString;
            }
            if (inetSocketAddress.getAddress() != null) {
                return inetSocketAddress.getAddress().getHostAddress();
            }
        }
        return host.getAddress() != null ? host.getAddress().getHostAddress() : null;
    }

    @Override
    public void shutdown() {
        String keyspaceName = configuration.getKeyspaceName();
        LOG.debug("Start shutdown manager for keyspace: {}", keyspaceName);
        if (initialized && !cluster.isClosed()) {
            persistersFactory.shutdownPersisters();

            cluster.close();
            initialized = false;
        }
        LOG.debug("Complete shutdown manager for keyspace: {} (initialized={}, cluster.isClosed={})",
                keyspaceName, initialized, cluster.isClosed());
    }
}
