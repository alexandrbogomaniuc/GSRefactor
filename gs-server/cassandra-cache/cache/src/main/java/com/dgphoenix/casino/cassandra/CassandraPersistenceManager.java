package com.dgphoenix.casino.cassandra;

import com.dgphoenix.casino.cassandra.persist.engine.ICassandraPersister;
import com.dgphoenix.casino.common.util.Pair;
import com.dgphoenix.casino.common.web.statistics.StatisticsManager;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:fateyev@dgphoenix.com">Anton Fateyev</a>
 * @since 26.09.16
 */
public class CassandraPersistenceManager {

    private final KeyspaceManagerFactory keyspaceManagerFactory;
    private final Map<String, IKeyspaceManager> keyspaceManagers;
    private final PersisterDependencyInjector persisterDependencyInjector;
    private IConfigsInitializer configsInitializer;

    public CassandraPersistenceManager(KeyspaceManagerFactory keyspaceManagerFactory, PersisterDependencyInjector persisterDependencyInjector) {
        this.keyspaceManagerFactory = keyspaceManagerFactory;
        this.persisterDependencyInjector = persisterDependencyInjector;
        keyspaceManagers = new LinkedHashMap<>();
    }

    public CassandraPersistenceManager addKeyspace(String configFilename, String schemaScriptFilename) {
        IKeyspaceManager keyspaceManager = keyspaceManagerFactory.create(configFilename, schemaScriptFilename, persisterDependencyInjector);
        keyspaceManagers.put(keyspaceManager.getKeyspaceName(), keyspaceManager);
        return this;
    }

    public CassandraPersistenceManager withConfigsInitializer(IConfigsInitializer configsInitializer) {
        this.configsInitializer = configsInitializer;
        return this;
    }

    public IKeyspaceManager getKeyspaceManager(String keyspaceName) {
        return keyspaceManagers.get(keyspaceName);
    }

    public Collection<IKeyspaceManager> getKeyspaceManagers() {
        return keyspaceManagers.values();
    }

    /**
     * Returns persisters of specified class from all keyspaces.
     *
     * @param persisterClass target persister class
     * @return map where keys are keyspace name and values persisters of specified class
     */
    public <P extends ICassandraPersister> Map<String, P> getPersisters(Class<P> persisterClass) {
        return keyspaceManagers.entrySet().stream()
                .map(entry -> {
                    P persister = entry.getValue().getPersister(persisterClass);
                    return persister != null ? new Pair<>(entry.getKey(), persister) : null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(Pair::getKey, Pair::getValue));
    }

    /**
     * Finds persister of specified class in all keyspaces and returns first one.
     *
     * @param persisterClass target persister class
     * @return first found persister or null
     */
    public <P extends ICassandraPersister> P getPersister(final Class<P> persisterClass) {
        return keyspaceManagers.values().stream()
                .map(keyspaceManager -> keyspaceManager.getPersister(persisterClass))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    /**
     * Finds persister that implements specified interface in all keyspaces.<br/>
     * Interface {@link ICassandraPersister} ignored.
     * For interfaces implemented by multiple persisters returns first found.
     *
     * @param persisterInterface target persister interface
     * @return found persister or null.
     */
    public <P> P getPersisterByInterface(final Class<P> persisterInterface) {
        return keyspaceManagers.values().stream()
                .flatMap(keyspaceManager -> keyspaceManager.getPersistersByInterface(persisterInterface).stream())
                .findFirst()
                .orElse(null);
    }

    /**
     * Finds all persister that implements specified interface in all keyspaces.<br/>
     * Interface {@link ICassandraPersister} ignored.
     *
     * @param persisterInterface target persister interface
     * @return list of persisters or empty list if neither of persisters implements specified interface.
     */
    public <P> List<P> getPersistersByInterface(final Class<P> persisterInterface) {
        return keyspaceManagers.values().stream()
                .flatMap(keyspaceManager -> keyspaceManager.getPersistersByInterface(persisterInterface).stream())
                .collect(Collectors.toList());
    }

    @PostConstruct
    private void init() {
        keyspaceManagers.values().forEach(keyspaceManager -> {
            keyspaceManager.init();
            StatisticsManager.getInstance().registerStatisticsGetter(
                    getClass().getSimpleName() + ": keySpace=" + keyspaceManager.getKeyspaceName(),
                    new KeyspaceManagerStatistics(keyspaceManager.getMetrics()));
        });
        persisterDependencyInjector.inject(this::getPersister);
        if (configsInitializer != null) {
            configsInitializer.initialize(this);
        }
    }

    @PreDestroy
    private void shutdown() {
        keyspaceManagers.values().forEach(IKeyspaceManager::shutdown);
    }
}
