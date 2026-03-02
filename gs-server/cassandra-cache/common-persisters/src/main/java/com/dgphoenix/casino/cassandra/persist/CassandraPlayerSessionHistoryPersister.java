package com.abs.casino.cassandra.persist;

import com.abs.casino.cassandra.persist.engine.AbstractCassandraPersister;
import com.abs.casino.cassandra.persist.engine.ColumnDefinition;
import com.abs.casino.cassandra.persist.engine.TableDefinition;
import com.abs.casino.cassandra.persist.engine.configuration.CompactionStrategy;
import com.abs.casino.cassandra.persist.engine.configuration.Compression;
import com.abs.casino.common.cache.data.session.SessionInfo;
import com.abs.casino.common.util.CalendarUtils;
import com.abs.casino.common.web.statistics.StatisticsManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * User: flsh
 * Date: 27.03.12
 */
public class CassandraPlayerSessionHistoryPersister extends AbstractCassandraPersister<String, String> {
    private static final Logger LOG = LogManager.getLogger(CassandraPlayerSessionHistoryPersister.class);
    public static final String PLAYER_SESSION_HISTORY_CF = "PlayerSessionHistoryCF";

    private static final String EXT_SESSION_ID_FIELD = "extSid";
    private static final String DAY_FIELD = "Day";

    private static final TableDefinition HISTORY_TABLE = new TableDefinition(PLAYER_SESSION_HISTORY_CF,
            Arrays.asList(
                    new ColumnDefinition(KEY, com.datastax.driver.core.DataType.text(), false, false, true),
                    new ColumnDefinition(EXT_SESSION_ID_FIELD, com.datastax.driver.core.DataType.text(), false, true, false),
                    new ColumnDefinition(DAY_FIELD, com.datastax.driver.core.DataType.bigint(), false, true, false),
                    new ColumnDefinition(SERIALIZED_COLUMN_NAME, com.datastax.driver.core.DataType.blob()),
                    new ColumnDefinition(JSON_COLUMN_NAME, com.datastax.driver.core.DataType.text())
            ), KEY)
            .compaction(CompactionStrategy.LEVELED)
            .gcGraceSeconds(TimeUnit.DAYS.toSeconds(1))
            .compression(Compression.DEFLATE);

    protected CassandraPlayerSessionHistoryPersister() {
    }

    @Override
    public TableDefinition getMainTableDefinition() {
        return HISTORY_TABLE;
    }

    public Logger getLog() {
        return LOG;
    }

    public void persist(SessionInfo entry) {
        long now = System.currentTimeMillis();
        ByteBuffer byteBuffer = HISTORY_TABLE.serializeToBytes(entry);
        String json = HISTORY_TABLE.serializeToJson(entry);
        try {
            com.datastax.driver.core.Statement insertQuery = entry.getExternalSessionId() == null
                    ? getInsertQuery()
                    .value(KEY, entry.getSessionId())
                    .value(DAY_FIELD, getDay(entry.getEndTime()))
                    .value(SERIALIZED_COLUMN_NAME, byteBuffer)
                    .value(JSON_COLUMN_NAME, json)
                    : getInsertQuery()
                    .value(KEY, entry.getSessionId())
                    .value(DAY_FIELD, getDay(entry.getEndTime()))
                    .value(EXT_SESSION_ID_FIELD, entry.getExternalSessionId())
                    .value(SERIALIZED_COLUMN_NAME, byteBuffer)
                    .value(JSON_COLUMN_NAME, json);
            execute(insertQuery, "persist");
        } finally {
            releaseBuffer(byteBuffer);
        }
        StatisticsManager.getInstance().updateRequestStatistics(getClass().getSimpleName() + " persist", System.currentTimeMillis() - now);
    }

    public Iterable<SessionInfo> getRecordsByDay(Date day) {
        long now = System.currentTimeMillis();
        //need override, by default 90 sec
        Integer readTimeout = (int) TimeUnit.MINUTES.toMillis(60);
        Iterable<SessionInfo> result =
                getAsIterableSkipNull(new String[] { SERIALIZED_COLUMN_NAME, JSON_COLUMN_NAME }, SessionInfo.class, readTimeout, "getRecordsByDay",
                        eq(DAY_FIELD, getDay(day)));
        StatisticsManager.getInstance().updateRequestStatistics(getClass().getSimpleName() + " getRecordsByDay",
                System.currentTimeMillis() - now);
        return result;
    }

    public boolean delete(String sessionId) {
        return super.deleteWithCheck(sessionId);
    }

    public void delete(String... sessionIds) {
        if (sessionIds.length == 0) {
            return;
        }
        com.datastax.driver.core.Statement query =
                com.datastax.driver.core.querybuilder.QueryBuilder.delete().
                        from(getMainColumnFamilyName()).
                        where(com.datastax.driver.core.querybuilder.QueryBuilder.in(KEY, sessionIds));
        execute(query, "delete playerSession");
    }


    private long getDay(long time) {
        return getDay(new Date(time));
    }

    private long getDay(Date time) {
        return CalendarUtils.getEndDay(time, "GMT").getTimeInMillis();
    }
}
