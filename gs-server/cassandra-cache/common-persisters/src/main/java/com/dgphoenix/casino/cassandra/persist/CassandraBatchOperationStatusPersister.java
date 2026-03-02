package com.abs.casino.cassandra.persist;

import com.abs.casino.cassandra.persist.engine.AbstractCassandraPersister;
import com.abs.casino.cassandra.persist.engine.ColumnDefinition;
import com.abs.casino.cassandra.persist.engine.TableDefinition;
import com.abs.casino.cassandra.persist.engine.configuration.CompactionStrategy;
import com.abs.casino.common.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class CassandraBatchOperationStatusPersister extends AbstractCassandraPersister<String, String> {
    public static final String CF_NAME = "BatchOpStatus";
    public static final String ROOM_ID = "ROOM_ID";
    public static final String ROUND_ID = "ROUND_ID";
    public static final String OPERATION_TYPE = "OP_TYPE";
    public static final String STATUS = "STATUS";
    public static final String CHANGE_DATE = "CHANGE_DATE";
    private static final Logger LOG = LogManager.getLogger(CassandraBatchOperationStatusPersister.class);
    private static final TableDefinition TABLE = new TableDefinition(CF_NAME,
            Arrays.asList(
                    new ColumnDefinition(ROOM_ID, com.datastax.driver.core.DataType.bigint(), false, false, true),
                    new ColumnDefinition(ROUND_ID, com.datastax.driver.core.DataType.bigint(), false, false, true),
                    new ColumnDefinition(OPERATION_TYPE, com.datastax.driver.core.DataType.text(), false, false, true),
                    new ColumnDefinition(STATUS, com.datastax.driver.core.DataType.text()),
                    new ColumnDefinition(CHANGE_DATE, com.datastax.driver.core.DataType.bigint())
            ), ROOM_ID, ROUND_ID)
            .compaction(CompactionStrategy.getLeveled(true, TimeUnit.HOURS.toSeconds(8)))
            .gcGraceSeconds(TimeUnit.HOURS.toSeconds(24));

    private CassandraBatchOperationStatusPersister() {
        super();
    }

    public void persist(long roomId, long roundId, String operationType, Status status) {
        com.datastax.driver.core.Statement query = getInsertQuery()
                .value(ROOM_ID, roomId)
                .value(ROUND_ID, roundId)
                .value(OPERATION_TYPE, operationType)
                .value(CHANGE_DATE, System.currentTimeMillis())
                .value(STATUS, status.name());
        execute(query, "persist");
    }

    public Pair<Status, Long> getStatus(long roomId, long roundId, String operationType) {
        com.datastax.driver.core.Statement query = com.datastax.driver.core.querybuilder.QueryBuilder.select(STATUS, CHANGE_DATE).from(getMainColumnFamilyName()).where(eq(ROOM_ID, roomId))
                .and(eq(ROUND_ID, roundId))
                .and(eq(OPERATION_TYPE, operationType))
                .limit(1);
        com.datastax.driver.core.ResultSet rows = execute(query, "getStatus");
        com.datastax.driver.core.Row row = rows.one();
        return row == null ? null : new Pair<>(Status.valueOf(row.getString(STATUS)), row.getLong(CHANGE_DATE));
    }

    @Override
    public TableDefinition getMainTableDefinition() {
        return TABLE;
    }

    @Override
    public Logger getLog() {
        return LOG;
    }

    public enum Status {
        STARTED, FINISHED;
    }
}
