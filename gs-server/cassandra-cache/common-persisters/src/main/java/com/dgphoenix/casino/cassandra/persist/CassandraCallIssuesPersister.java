package com.abs.casino.cassandra.persist;

import com.dgphoenix.casino.cassandra.persist.engine.AbstractCassandraPersister;
import com.dgphoenix.casino.cassandra.persist.engine.ColumnDefinition;
import com.dgphoenix.casino.cassandra.persist.engine.TableDefinition;
import com.abs.casino.common.cache.data.URLCallCounters;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.stream.Collectors;


public class CassandraCallIssuesPersister extends AbstractCassandraPersister {
    private static final Logger LOG = LogManager.getLogger(CassandraCallIssuesPersister.class);

    public static final String COLUMN_FAMILY_NAME = "HttpCallIssues";
    public static final String DATE_FIELD = "Date";
    public static final String URL_FIELD = "URL";
    public static final String LAST_UPDATE_FIELD = "UpdateTime";
    public static final String SUCCESS_COUNT = "SuccessCount";
    public static final String FAIL_COUNT = "FailCount";

    private static final TableDefinition TABLE = new TableDefinition(
            COLUMN_FAMILY_NAME,
            Arrays.asList(
                    new ColumnDefinition(DATE_FIELD, com.datastax.driver.core.DataType.text(), false, false, true),
                    new ColumnDefinition(URL_FIELD, com.datastax.driver.core.DataType.text(), false, false, true),
                    new ColumnDefinition(SUCCESS_COUNT, com.datastax.driver.core.DataType.bigint()),
                    new ColumnDefinition(FAIL_COUNT, com.datastax.driver.core.DataType.bigint()),
                    new ColumnDefinition(LAST_UPDATE_FIELD, com.datastax.driver.core.DataType.bigint())
            ),
            DATE_FIELD
    );

    private CassandraCallIssuesPersister() {
    }

    public void persist(URLCallCounters callStatistics) {
        com.datastax.driver.core.Statement insert = getInsertQuery()
                .value(DATE_FIELD, callStatistics.getDate())
                .value(URL_FIELD, callStatistics.getUrl())
                .value(SUCCESS_COUNT, callStatistics.getSuccessCount())
                .value(FAIL_COUNT, callStatistics.getFailedCount())
                .value(LAST_UPDATE_FIELD, System.currentTimeMillis());
        execute(insert, "persist");
    }

    public Collection<URLCallCounters> getByDate(final String date) {
        com.datastax.driver.core.Statement select = getSelectColumnsQuery(URL_FIELD, SUCCESS_COUNT, FAIL_COUNT, LAST_UPDATE_FIELD)
                .where(eq(DATE_FIELD, date));
        com.datastax.driver.core.ResultSet resultSet = execute(select, "getByDate");
        if (resultSet.isExhausted()) {
            return Collections.emptyList();
        } else {
            return resultSet.all().stream()
                    .filter(Objects::nonNull)
                    .map(input -> {
                        String url = input.getString(URL_FIELD);
                        long successCount = input.getLong(SUCCESS_COUNT);
                        long failedCount = input.getLong(FAIL_COUNT);
                        long lastFailTime = input.getLong(LAST_UPDATE_FIELD);
                        return new URLCallCounters(date, url, successCount, failedCount, lastFailTime);
                    })
                    .collect(Collectors.toList());
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
