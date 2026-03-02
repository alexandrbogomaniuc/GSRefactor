package com.abs.casino.cassandra.persist;

import com.abs.casino.cassandra.persist.engine.ColumnDefinition;
import com.abs.casino.cassandra.persist.engine.ICassandraPersister;
import com.abs.casino.cassandra.persist.engine.TableDefinition;
import org.apache.logging.log4j.Logger;

import java.util.Collections;

/**
 * @author <a href="mailto:fateyev@dgphoenix.com">Anton Fateyev</a>
 * @since 14.09.16
 */
public class CassandraPersisterMock implements ICassandraPersister, ISimplePersister {

    private static CassandraPersisterMock instance = new CassandraPersisterMock();

    public static CassandraPersisterMock getInstance() {
        return instance;
    }

    @Override
    public TableDefinition getMainTableDefinition() {
        return new TableDefinition("Mock_CF", Collections.<ColumnDefinition>emptyList());
    }

    @Override
    public void createTable(com.datastax.driver.core.Session session, TableDefinition tableDefinition) {

    }

    @Override
    public void updateTable(com.datastax.driver.core.Session session, TableDefinition tableDefinition, com.datastax.driver.core.TableMetadata existTableMetadata) {

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
