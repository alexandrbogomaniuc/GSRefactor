package com.dgphoenix.casino.promo.persisters;

import com.datastax.driver.core.DataType;
import com.dgphoenix.casino.cassandra.persist.engine.AbstractCassandraPersister;
import com.dgphoenix.casino.cassandra.persist.engine.ColumnDefinition;
import com.dgphoenix.casino.cassandra.persist.engine.TableDefinition;
import com.dgphoenix.casino.common.promo.ai.ITournamentFeedHistoryPersister;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Map;

public class CassandraTournamentFeedHistoryPersister extends AbstractCassandraPersister<String, String> implements ITournamentFeedHistoryPersister {
    private static final Logger LOG = LogManager.getLogger(CassandraTournamentFeedHistoryPersister.class);

    private static final String CF_NAME = "TournamentHistoryCF";
    private static final String TOURNAMENT_ID_COLUMN = "id";
    private static final String TIME_COLUMN = "t";

    private static final TableDefinition HISTORY_TABLE = new TableDefinition(CF_NAME,
            Arrays.asList(
                    new ColumnDefinition(TOURNAMENT_ID_COLUMN, DataType.bigint(), false, false, true),
                    new ColumnDefinition(TIME_COLUMN, DataType.cint(), false, false, true),
                    new ColumnDefinition(SERIALIZED_COLUMN_NAME, DataType.blob()),
                    new ColumnDefinition(JSON_COLUMN_NAME, DataType.text())
            ), TOURNAMENT_ID_COLUMN);

    @Override
    public void persistRecords(long tournamentId, int time, Map<String, Long> scores) {
        ByteBuffer buffer = HISTORY_TABLE.serializeWithClassToBytes(scores);
        String json = HISTORY_TABLE.serializeToMapJson(scores, String.class, Long.class);
        try {
            com.datastax.driver.core.Statement insert = getInsertQuery()
                    .value(TOURNAMENT_ID_COLUMN, tournamentId)
                    .value(TIME_COLUMN, time)
                    .value(SERIALIZED_COLUMN_NAME, buffer)
                    .value(JSON_COLUMN_NAME, json);

            execute(insert, "persistRecords");
        } finally {
            releaseBuffer(buffer);
        }
    }

    @Override
    public Map<String, Long> getRecords(long tournamentId, int time) {
        com.datastax.driver.core.Statement query = getSelectColumnsQuery(HISTORY_TABLE, SERIALIZED_COLUMN_NAME, JSON_COLUMN_NAME)
                .where(eq(TOURNAMENT_ID_COLUMN, tournamentId))
                .and(eq(TIME_COLUMN, time))
                .limit(1);

        com.datastax.driver.core.Row result = execute(query, "getRecords").one();
        if (result == null) {
            return null;
        }
        Map<String, Long> records = HISTORY_TABLE.deserializeToMapJson(result.getString(JSON_COLUMN_NAME), String.class, Long.class);
        if (records == null) {
            records = HISTORY_TABLE.deserializeWithClassFrom(result.getBytes(SERIALIZED_COLUMN_NAME));
        }
        return records;
    }

    @Override
    public TableDefinition getMainTableDefinition() {
        return HISTORY_TABLE;
    }

    @Override
    public Logger getLog() {
        return LOG;
    }
}
