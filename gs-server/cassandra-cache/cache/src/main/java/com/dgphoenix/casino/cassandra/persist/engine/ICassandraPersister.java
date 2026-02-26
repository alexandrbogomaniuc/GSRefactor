package com.dgphoenix.casino.cassandra.persist.engine;

import com.datastax.driver.core.TableMetadata;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.List;

/**
 * User: Grien
 * Date: 25.12.2012 19:40
 */
public interface ICassandraPersister {
    String ID_DELIMITER = "+";
    int DEFAULT_GC_GRACE_PERIOD_IN_SECONDS = 3600 * 3;
    String DATE_FORMAT = "yyyy-MM-dd HH:mm:ssZ";

    TableDefinition getMainTableDefinition();

    default String getMainColumnFamilyName() {
        return getMainTableDefinition().getTableName();
    }

    default List<TableDefinition> getAllTableDefinitions() {
        return Collections.singletonList(getMainTableDefinition());
    }

    void createTable(com.datastax.driver.core.Session session, TableDefinition tableDefinition);

    void updateTable(com.datastax.driver.core.Session session, TableDefinition tableDefinition, TableMetadata tableMetadata);

    Logger getLog();

    void initSession(com.datastax.driver.core.Session session);

    void init();

    void shutdown();

    void setTtl(Integer ttl);

    Integer getTtl();

    void setConsistencyLevels(com.datastax.driver.core.ConsistencyLevel readConsistency, com.datastax.driver.core.ConsistencyLevel writeConsistency, com.datastax.driver.core.ConsistencyLevel serialConsistency);
}