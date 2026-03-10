package com.abs.casino.cassandra.persist;

import com.abs.casino.cassandra.persist.engine.ICassandraPersister;
import com.abs.casino.cassandra.persist.engine.ConsistencyLevel;
import com.abs.casino.cassandra.persist.engine.Session;
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
    public void createTable(Session session, TableDefinition tableDefinition) {

    }

    @Override
    public void updateTable(Session session, TableDefinition tableDefinition, com.datastax.driver.core.TableMetadata existTableMetadata) {

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
    public void initSession(Session session) {

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
    public void setConsistencyLevels(ConsistencyLevel readConsistency, ConsistencyLevel writeConsistency, ConsistencyLevel serialConsistency) {

    }

    @Override
    public void persist(Object persistentObject) {

    }
}
