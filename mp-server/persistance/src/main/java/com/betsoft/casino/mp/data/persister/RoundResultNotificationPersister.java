package com.betsoft.casino.mp.data.persister;

import com.betsoft.casino.mp.model.IRoundResultNotification;
import com.datastax.driver.core.DataType;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.dgphoenix.casino.cassandra.persist.engine.AbstractCassandraPersister;
import com.dgphoenix.casino.cassandra.persist.engine.ColumnDefinition;
import com.dgphoenix.casino.cassandra.persist.engine.TableDefinition;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RoundResultNotificationPersister extends AbstractCassandraPersister<Long, String> {
    private static final Logger LOG = LogManager.getLogger(RoundResultNotificationPersister.class);

    private static final String CF_NAME = "RoundNotify2";
    private static final String ACCOUNT_ID_COLUMN = "a";
    private static final String GAME_ID_COLUMN = "g";
    private static final String NOTIFICATION_ID_COLUMN = "n";
    private static final String SERIALIZED_COLUMN_NAME = "scn";

    private static final TableDefinition TABLE = new TableDefinition(CF_NAME,
            Arrays.asList(
                    new ColumnDefinition(ACCOUNT_ID_COLUMN, DataType.bigint(), false, false, true),
                    new ColumnDefinition(GAME_ID_COLUMN, DataType.bigint(), false, false, true),
                    new ColumnDefinition(NOTIFICATION_ID_COLUMN, DataType.bigint(), false, false, true),
                    new ColumnDefinition(SERIALIZED_COLUMN_NAME, DataType.blob()),
                    new ColumnDefinition(JSON_COLUMN_NAME, DataType.text())
            ), ACCOUNT_ID_COLUMN, GAME_ID_COLUMN);

    public List<IRoundResultNotification> getNotifications(long accountId, long gameId) {
        ResultSet result = execute(
                getSelectAllColumnsQuery(TABLE)
                        .where(eq(ACCOUNT_ID_COLUMN, accountId))
                        .and(eq(GAME_ID_COLUMN, gameId)),
                "getNotifications");

        List<IRoundResultNotification> notifications = new ArrayList<>();
        if (result != null) {
            result.forEach(row -> {
                String json = row.getString(JSON_COLUMN_NAME);
                IRoundResultNotification not = TABLE.deserializeWithClassFromJson(json);
                if (not == null) {
                    ByteBuffer bytes = row.getBytes(SERIALIZED_COLUMN_NAME);
                    not = TABLE.deserializeWithClassFrom(bytes);
                }
                if (not != null) {
                    notifications.add(not);
                }
            });
        }
        return notifications;
    }

    public void addNotification(long accountId, long gameId, IRoundResultNotification notification) {
        ByteBuffer buffer = TABLE.serializeWithClassToBytes(notification);
        String json = TABLE.serializeWithClassToJson(notification);
        try {
            execute(getInsertQuery()
                            .value(ACCOUNT_ID_COLUMN, accountId)
                            .value(GAME_ID_COLUMN, gameId)
                            .value(NOTIFICATION_ID_COLUMN, notification.getNotificationId())
                            .value(SERIALIZED_COLUMN_NAME, buffer)
                            .value(JSON_COLUMN_NAME, json),
                    "addNotification");
        } finally {
            releaseBuffer(buffer);
        }
    }

    public void removeNotification(long accountId, long gameId, long notificationId) {
        execute(QueryBuilder.delete().from(CF_NAME)
                        .where(eq(ACCOUNT_ID_COLUMN, accountId))
                        .and(eq(GAME_ID_COLUMN, gameId))
                        .and(eq(NOTIFICATION_ID_COLUMN, notificationId)),
                "removeNotification");
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
