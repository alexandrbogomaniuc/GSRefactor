package com.abs.casino.cassandra.persist;

import com.abs.casino.cassandra.persist.engine.AbstractCassandraPersister;
import com.abs.casino.cassandra.persist.engine.ColumnDefinition;
import com.abs.casino.cassandra.persist.engine.Row;
import com.abs.casino.cassandra.persist.engine.TableDefinition;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static com.abs.casino.cassandra.persist.engine.CassandraDataTypes.bigint;
import static com.abs.casino.cassandra.persist.engine.CassandraDataTypes.text;

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
                    new ColumnDefinition(KEY, text(), false, false, true),
                    new ColumnDefinition(LAST_PROCESSED_DATE_COLUMN, bigint(), false, false, false)
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
        com.abs.casino.cassandra.persist.engine.Statement query = com.abs.casino.cassandra.persist.engine.Statement.of(getInsertQuery()
                .value(KEY, cfName)
                .value(LAST_PROCESSED_DATE_COLUMN, lastProcessedDate));
        execute(query, "persist");
    }

    public Long getLastArchiveDate(String cfName, int month) {
        Row row = getAsWrappedRow(cfName, LAST_PROCESSED_DATE_COLUMN);
        if (row != null && !row.isNull(LAST_PROCESSED_DATE_COLUMN)) {
            return row.getLong(LAST_PROCESSED_DATE_COLUMN);
        }
        return System.currentTimeMillis() - TimeUnit.DAYS.toMillis(month * 30);
    }
}
