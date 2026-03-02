package com.abs.casino.cassandra.persist;

import com.abs.casino.cassandra.persist.engine.ICassandraPersister;
import com.abs.casino.cassandra.persist.engine.TableDefinition;
import org.apache.logging.log4j.Logger;

/**
 * @author <a href="mailto:fateyev@dgphoenix.com">Anton Fateyev</a>
 * @since 06.10.16
 */
public class SimplePersisterImpl implements ICassandraPersister, ISimplePersister {
    @Override
    public TableDefinition getMainTableDefinition() {
        return null;
    }

    @Override
    public void createTable(com.datastax.driver.core.Session session, TableDefinition tableDefinition) {

    }

    @Override
    public void updateTable(com.datastax.driver.core.Session session, TableDefinition tableDefinition, com.datastax.driver.core.TableMetadata existTableMetadata) {

    }

    @Override
    public String getMainColumnFamilyName() {
        return null;
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

    @Override
    public void persist(Object persistentObject) {

    }
}
