package com.dgphoenix.casino.cassandra.persist;

import com.dgphoenix.casino.cassandra.persist.engine.AbstractCassandraPersister;
import com.dgphoenix.casino.cassandra.persist.engine.ColumnDefinition;
import com.dgphoenix.casino.cassandra.persist.engine.TableDefinition;
import com.dgphoenix.casino.common.monitoring.OnlineConcurrentMailNotification;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Created by vladislav on 14/08/15.
 */
public class CassandraNotificationPersister extends AbstractCassandraPersister<Long, String> {
    private static final Logger LOG = LogManager.getLogger(CassandraNotificationPersister.class);

    private static final String CONCURRENT_NOTIFICATION_CF = "ConcurrentNotCF";
    private static final String SERVER_ID = "serverId";
    private static final TableDefinition CONCURRENT_NOTIFICATION_TABLE = new TableDefinition(
            CONCURRENT_NOTIFICATION_CF,
            Arrays.asList(new ColumnDefinition(KEY, com.datastax.driver.core.DataType.text(), false, false, true),
                    new ColumnDefinition(SERVER_ID, com.datastax.driver.core.DataType.cint(), false, false, true),
                    new ColumnDefinition(SERIALIZED_COLUMN_NAME, com.datastax.driver.core.DataType.blob()),
                    new ColumnDefinition(JSON_COLUMN_NAME, com.datastax.driver.core.DataType.text())),
            KEY, SERVER_ID);

    private CassandraNotificationPersister() {
    }

    public OnlineConcurrentMailNotification get(int concurrentLimit, int gameServerId) {
        com.datastax.driver.core.Statement query = getSelectAllColumnsQuery()
                .where(eq(KEY, String.valueOf(concurrentLimit)))
                .and(eq(SERVER_ID, gameServerId));
        com.datastax.driver.core.ResultSet resultSet = execute(query, "getOnlineConcurrentMailNotification");
        com.datastax.driver.core.Row row = resultSet.one();
        if (row == null) {
            return null;
        }

        String json = row.getString(JSON_COLUMN_NAME);
        OnlineConcurrentMailNotification obj = getMainTableDefinition().deserializeFromJson(json, OnlineConcurrentMailNotification.class);

        if (obj == null) {
            ByteBuffer buffer = row.getBytes(SERIALIZED_COLUMN_NAME);
            obj = getMainTableDefinition().deserializeFrom(buffer, OnlineConcurrentMailNotification.class);
        }

        return obj;
    }

    public void persist(OnlineConcurrentMailNotification concurrentNotification) {
        String json = CONCURRENT_NOTIFICATION_TABLE.serializeToJson(concurrentNotification);
        ByteBuffer byteBuffer = CONCURRENT_NOTIFICATION_TABLE.serializeToBytes(concurrentNotification);
        try {
            com.datastax.driver.core.Statement insertQuery = getInsertQuery()
                    .value(KEY, String.valueOf(concurrentNotification.getConcurrentLimit())).
                    value(SERVER_ID, concurrentNotification.getGameServerId()).
                    value(SERIALIZED_COLUMN_NAME, byteBuffer).
                    value(JSON_COLUMN_NAME, json);
            execute(insertQuery, "notification persist", com.datastax.driver.core.ConsistencyLevel.ANY);
        } finally {
            releaseBuffer(byteBuffer);
        }
    }

    @Override
    public TableDefinition getMainTableDefinition() {
        return CONCURRENT_NOTIFICATION_TABLE;
    }

    @Override
    public Logger getLog() {
        return LOG;
    }
}
