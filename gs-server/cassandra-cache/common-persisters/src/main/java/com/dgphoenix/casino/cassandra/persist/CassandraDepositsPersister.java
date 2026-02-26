package com.dgphoenix.casino.cassandra.persist;

import com.datastax.driver.core.DataType;
import com.datastax.driver.core.Row;
import com.dgphoenix.casino.cassandra.persist.engine.AbstractCassandraPersister;
import com.dgphoenix.casino.cassandra.persist.engine.ColumnDefinition;
import com.dgphoenix.casino.cassandra.persist.engine.TableDefinition;
import com.dgphoenix.casino.cassandra.persist.engine.configuration.Caching;
import com.dgphoenix.casino.cassandra.persist.engine.configuration.CompactionStrategy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class CassandraDepositsPersister extends AbstractCassandraPersister<String, Long> {
    private static final Logger LOG = LogManager.getLogger(CassandraDepositsPersister.class);
    private static final String SESSION_ID = "sessId";
    private static final String AMOUNT = "amount";
    private static final String TABLE_NAME = "depositsCF";
    private static final TableDefinition TABLE = new TableDefinition(TABLE_NAME,
            Arrays.asList(
                    new ColumnDefinition(SESSION_ID, DataType.text(), false, false, true),
                    new ColumnDefinition(AMOUNT, DataType.bigint(), false, false, false)
            ), SESSION_ID)
            .compaction(CompactionStrategy.LEVELED)
            .gcGraceSeconds(TimeUnit.DAYS.toMillis(1))
            .caching(Caching.NONE);

    //deposit < 0 for CT/DirectTransfer (ptpt, vietbet)
    public void persist(String sessionId, long deposit) {
        LOG.debug("persist: sid={}, deposit={}", sessionId, deposit);
        insert(sessionId, AMOUNT, deposit);
    }

    public Long getDeposit(String sessionId) {
        com.datastax.driver.core.Row result = getAsRow(sessionId, AMOUNT);
        return result == null ? null : result.getLong(AMOUNT);
    }

    @Override
    protected String getKeyColumnName() {
        return SESSION_ID;
    }

    @Override
    public TableDefinition getMainTableDefinition() {
        return TABLE;
    }

    @Override
    public Logger getLog() {
        return LOG;
    }
}
