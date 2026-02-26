package com.dgphoenix.casino.cassandra.persist;

import com.datastax.driver.core.DataType;
import com.datastax.driver.core.Row;
import com.dgphoenix.casino.cassandra.persist.engine.AbstractCassandraPersister;
import com.dgphoenix.casino.cassandra.persist.engine.ColumnDefinition;
import com.dgphoenix.casino.cassandra.persist.engine.TableDefinition;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Collections.*;

public class CassandraSupportPersister extends AbstractCassandraPersister<String, Long> {
    private static final Logger LOG = LogManager.getLogger(CassandraSupportPersister.class);

    private static final String CF_NAME = "SupportCF";
    private static final String TIMESTAMP = "Timestamp";
    private static final String INFO = "Info";

    private static final TableDefinition TABLE = new TableDefinition(
            CF_NAME,
            Arrays.asList(
                    new ColumnDefinition(KEY, DataType.text(), false, false, true),
                    new ColumnDefinition(TIMESTAMP, DataType.bigint(), false, false, true),
                    new ColumnDefinition(INFO, DataType.text())
            ),
            KEY);

    private CassandraSupportPersister() {
    }

    public void persist(String sessionId, long timestamp, String info) {
        com.datastax.driver.core.Statement query = getInsertQuery()
                .value(KEY, sessionId)
                .value(TIMESTAMP, timestamp)
                .value(INFO, info);
        execute(query, "persist");
    }

    public Iterable<String> getSessionIDs() {
        com.datastax.driver.core.Statement query = getSelectColumnsQuery(KEY);
        com.datastax.driver.core.ResultSet resultSet = execute(query, "getSessionIDs");

        if (resultSet.isExhausted()) {
            return emptyList();
        }
        final List<Row> rows = resultSet.all();

        return rows.stream()
                .filter(Objects::nonNull)
                .map(input -> input.getString(KEY))
                .collect(Collectors.toList());
    }

    public Map<Long, String> getValuesBySessionID(String sessionId) {
        com.datastax.driver.core.Statement query = getSelectColumnsQuery(TIMESTAMP, INFO)
                .where(eq(KEY, sessionId));
        com.datastax.driver.core.ResultSet resultSet = execute(query, "getValuesBySessionID");

        if (resultSet.isExhausted()) {
            return emptyMap();
        }
        List<Row> rows = resultSet.all();
        Map<Long, String> sortedByTimestamp = new TreeMap<>(reverseOrder());
        rows.forEach(row -> {
            if (row != null)
                sortedByTimestamp.put(row.getLong(TIMESTAMP), row.getString(INFO));
        });
        return sortedByTimestamp;
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
