package com.dgphoenix.casino.cassandra.persist;

import com.datastax.driver.core.DataType;
import com.dgphoenix.casino.cassandra.persist.engine.AbstractCassandraPersister;
import com.dgphoenix.casino.cassandra.persist.engine.ColumnDefinition;
import com.dgphoenix.casino.cassandra.persist.engine.TableDefinition;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.Date;

/**
 * User: flsh
 * Date: 21.11.14.
 */
public class CassandraExpiredBonusTrackerInfoPersister extends AbstractCassandraPersister<String, String> {
    private static final Logger LOG = LogManager.getLogger(CassandraExpiredBonusTrackerInfoPersister.class);
    public final static String COLUMN_FAMILY_NAME = "ExpiredBonusTrackedCF";
    public static final String LAST_PROCESSED_DATE_COLUMN = "LastProcessedDate";
    public static final String FR_BONUS_KEY = "FR_BONUS";
    public static final String BONUS_KEY = "BONUS";

    private final static TableDefinition TABLE = new TableDefinition(COLUMN_FAMILY_NAME,
            Arrays.asList(
                    new ColumnDefinition(KEY, DataType.text(), false, false, true),
                    new ColumnDefinition(LAST_PROCESSED_DATE_COLUMN, DataType.bigint(), false, false, false)
            ), KEY);

    private CassandraExpiredBonusTrackerInfoPersister() {
    }

    @Override
    public TableDefinition getMainTableDefinition() {
        return TABLE;
    }

    @Override
    public Logger getLog() {
        return LOG;
    }

    public void persistFrBonusTrackerInfo(long lastProcessedDate) {
        persist(FR_BONUS_KEY, lastProcessedDate);
    }

    public void persistBonusTrackerInfo(long lastProcessedDate) {
        persist(BONUS_KEY, lastProcessedDate);
    }

    public Long getFrBonusLastProcessedDate() {
        return getLastProcessedDate(FR_BONUS_KEY);
    }

    public Long getBonusLastProcessedDate() {
        return getLastProcessedDate(BONUS_KEY);
    }

    public void persist(String key, long lastProcessedDate) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("persist: " + key + "=" + new Date(lastProcessedDate));
        }
        com.datastax.driver.core.Statement query = getInsertQuery()
                .value(KEY, key)
                .value(LAST_PROCESSED_DATE_COLUMN, lastProcessedDate);
        execute(query, "persist");
    }

    public Long getLastProcessedDate(String key) {
        com.datastax.driver.core.Row row = getAsRow(key, LAST_PROCESSED_DATE_COLUMN);
        return row != null ? row.getLong(LAST_PROCESSED_DATE_COLUMN) : null;
    }
}
