package com.dgphoenix.casino.cassandra.inject;

import com.datastax.driver.core.TableMetadata;
import com.dgphoenix.casino.cassandra.persist.engine.ICassandraPersister;
import com.dgphoenix.casino.cassandra.persist.engine.TableDefinition;
import org.apache.logging.log4j.Logger;

/**
 * @author <a href="mailto:noragami@dgphoenix.com">Alexander Aldokhin</a>
 * @since 08.08.2022
 */
public class AbstractPersister implements ICassandraPersister {

    @Override
    public TableDefinition getMainTableDefinition() {
        return null;
    }

    @Override
    public void createTable(com.datastax.driver.core.Session session, TableDefinition tableDefinition) {

    }

    @Override
    public void updateTable(com.datastax.driver.core.Session session, TableDefinition tableDefinition, TableMetadata tableMetadata) {

    }

    @Override
    public Logger getLog() {
        return null;
    }

    @Override
    public void initSession(com.datastax.driver.core.Session session) {

    }

    @Override
    public void init() {

    }

    @Override
    public void shutdown() {

    }

    @Override
    public void setTtl(Integer ttl) {

    }

    @Override
    public Integer getTtl() {
        return null;
    }

    @Override
    public void setConsistencyLevels(com.datastax.driver.core.ConsistencyLevel readConsistency, com.datastax.driver.core.ConsistencyLevel writeConsistency, com.datastax.driver.core.ConsistencyLevel serialConsistency) {

    }
}
