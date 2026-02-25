package com.dgphoenix.casino.cassandra;

import com.datastax.driver.core.Host;
import com.datastax.driver.core.Metrics;
import com.datastax.driver.core.Session;
import com.dgphoenix.casino.cassandra.persist.engine.ICassandraPersister;

import java.util.List;
import java.util.Set;

/**
 * @author <a href="mailto:fateyev@dgphoenix.com">Anton Fateyev</a>
 * @since 19.09.16
 */
public interface IKeyspaceManager {

    <P extends ICassandraPersister> P getPersister(Class<P> persisterClass);

    <P> List<P> getPersistersByInterface(Class<P> persisterInterface);

    List<ICassandraPersister> getAllPersisters();

    boolean isReady();

    Set<Host> getDownHosts();

    /**
     * Driver-neutral representation of down Cassandra nodes (host/ip only).
     * Used by web/runtime diagnostics to reduce direct driver API coupling.
     */
    Set<String> getDownHostAddresses();

    /**
     * Driver-neutral representation of all known Cassandra nodes (host/ip only).
     */
    Set<String> getAllHostAddresses();

    String getKeyspaceName();

    Session getSession();

    Metrics getMetrics();

    void init();

    void shutdown();

    Set<String> getJmxHosts();

}
