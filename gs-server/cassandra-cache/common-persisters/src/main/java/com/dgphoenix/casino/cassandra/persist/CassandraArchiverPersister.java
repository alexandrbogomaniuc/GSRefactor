package com.dgphoenix.casino.cassandra.persist;

import com.datastax.driver.core.DataType;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.dgphoenix.casino.cassandra.persist.engine.AbstractCassandraPersister;
import com.dgphoenix.casino.cassandra.persist.engine.ColumnDefinition;
import com.dgphoenix.casino.cassandra.persist.engine.TableDefinition;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * User: flsh
 * Date: 14.06.13
 */
public class CassandraArchiverPersister extends AbstractCassandraPersister<String, String> {
    private static final Logger LOG = LogManager.getLogger(CassandraArchiverPersister.class);
    public static final String COLUMN_FAMILY_NAME = "ArchiverCF";
    public static final String LAST_PROCESSED_DATE_COLUMN = "LastProcessedDate";
    private static final TableDefinition TABLE = new TableDefinition(COLUMN_FAMILY_NAME,
            Arrays.asList(
                    new ColumnDefinition(KEY, DataType.text(), false, false, true),
                    new ColumnDefinition(LAST_PROCESSED_DATE_COLUMN, DataType.bigint(), false, false, false)
            ),
            Collections.singletonList(KEY));

    private CassandraArchiverPersister() {
    }

    @Override
    public TableDefinition getMainTableDefinition() {
        return TABLE;
    }

    @Override
    public Logger getLog() {
        return LOG;
    }

    public void persist(String cfName, long lastProcessedDate) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("persist: " + cfName + "=" + new Date(lastProcessedDate));
        }
        Statement query = getInsertQuery()
                .value(KEY, cfName)
                .value(LAST_PROCESSED_DATE_COLUMN, lastProcessedDate);
        execute(query, "persist");
    }

    public Long getLastArchiveDate(String cfName, int month) {
        Row row = getAsRow(cfName, LAST_PROCESSED_DATE_COLUMN);
        if (row != null && !row.isNull(LAST_PROCESSED_DATE_COLUMN)) {
            return row.getLong(LAST_PROCESSED_DATE_COLUMN);
        }
        return System.currentTimeMillis() - TimeUnit.DAYS.toMillis(month * 30);
    }
}
