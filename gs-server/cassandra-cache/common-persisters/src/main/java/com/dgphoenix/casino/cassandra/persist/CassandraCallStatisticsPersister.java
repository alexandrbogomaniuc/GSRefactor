package com.abs.casino.cassandra.persist;

import com.abs.casino.cassandra.persist.IHttpClientStatisticsPersister;
import com.abs.casino.cassandra.persist.engine.AbstractCassandraPersister;
import com.abs.casino.cassandra.persist.engine.ColumnDefinition;
import com.abs.casino.cassandra.persist.engine.Row;
import com.abs.casino.cassandra.persist.engine.TableDefinition;
import com.abs.casino.common.cache.data.URLCallCounters;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.format.DateTimeFormatter;
import java.util.Arrays;

import static com.abs.casino.cassandra.persist.engine.CassandraDataTypes.counter;
import static com.abs.casino.cassandra.persist.engine.CassandraDataTypes.text;
import static com.abs.casino.cassandra.persist.engine.Cql.incr;

public class CassandraCallStatisticsPersister extends AbstractCassandraPersister<String, String> implements IHttpClientStatisticsPersister {
    private static final Logger LOG = LogManager.getLogger(CassandraCallStatisticsPersister.class);

    public static final String COLUMN_FAMILY_NAME = "HttpCallStatistics";
    public static final String SUCCESS_COUNTER = "SuccessCount";
    public static final String FAIL_COUNTER = "FailedCount";
    public static final String DATE = "Date";
    public static final String URL = "URL";
    public static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yy");

    private CassandraCallIssuesPersister callIssuesPersister;

    private static final TableDefinition TABLE = new TableDefinition(
            COLUMN_FAMILY_NAME,
            Arrays.asList(
                    new ColumnDefinition(DATE, text(), false, false, true),
                    new ColumnDefinition(URL, text(), false, false, true),
                    new ColumnDefinition(SUCCESS_COUNTER, counter()),
                    new ColumnDefinition(FAIL_COUNTER, counter())
            ),
            DATE
    );

    private CassandraCallStatisticsPersister() {
    }

    @SuppressWarnings("unused")
    private void setCallIssuesPersister(CassandraCallIssuesPersister callIssuesPersister) {
        this.callIssuesPersister = callIssuesPersister;
    }

    @Override
    public void persist(String date, String url, boolean isSuccess, long amount) {
        String counterColumn = isSuccess ? SUCCESS_COUNTER : FAIL_COUNTER;

        com.abs.casino.cassandra.persist.engine.Statement update = com.abs.casino.cassandra.persist.engine.Statement.of(getUpdateQuery()
                .where(eq(DATE, date))
                .and(eq(URL, url))
                .with(incr(counterColumn, amount)));
        if (LOG.isDebugEnabled()) {
            LOG.debug("persist " + url + ", isSuccess:" + isSuccess);
        }
        boolean wasApplied = execute(update, "persist").wasApplied();

        if (wasApplied) {
            if (!isSuccess) {
                callIssuesPersister.persist(getCallStatistics(date, url));
            }
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("persist execute was not applied. Date:" + date + ", url:" + url + ", isSuccess: " + isSuccess + ", amount: " + amount);
            }
        }
    }

    private URLCallCounters getCallStatistics(String date, String url) {
        com.abs.casino.cassandra.persist.engine.Statement select = com.abs.casino.cassandra.persist.engine.Statement.of(getSelectColumnsQuery(FAIL_COUNTER, SUCCESS_COUNTER)
                .where(eq(DATE, date))
                .and(eq(URL, url)));
        Row row = executeWrapped(select, "getCallStatistics").one();
        if (row == null) {
            return new URLCallCounters(date, url, 0, 0, 0);
        } else {
            long successes = row.getLong(SUCCESS_COUNTER);
            long fails = row.getLong(FAIL_COUNTER);
            return new URLCallCounters(date, url, successes, fails, 0);
        }
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
