package com.abs.casino.cassandra;

import com.abs.casino.cassandra.persist.engine.ICassandraPersister;

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

    /**
     * Driver-neutral keyspace metrics snapshot used by runtime statistics output.
     */
    KeyspaceMetricsSnapshot getMetricsSnapshot();

    void init();

    void shutdown();

    Set<String> getJmxHosts();

}
