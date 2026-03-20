package com.abs.casino.cassandra.persist.mp;

import com.abs.casino.cassandra.persist.engine.AbstractCassandraPersister;
import com.abs.casino.cassandra.persist.engine.ColumnDefinition;
import com.abs.casino.cassandra.persist.engine.ResultSet;
import com.abs.casino.cassandra.persist.engine.Row;
import com.abs.casino.cassandra.persist.engine.TableDefinition;
import com.abs.casino.common.kpi.RoundKPIInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.abs.casino.cassandra.persist.engine.CassandraDataTypes.bigint;
import static com.abs.casino.cassandra.persist.engine.CassandraDataTypes.blob;
import static com.abs.casino.cassandra.persist.engine.CassandraDataTypes.text;

/**
 * User: flsh
 * Date: 20.11.2020.
 */
public class RoundKPIInfoPersister extends AbstractCassandraPersister<Long, String> {
    private static final Logger LOG = LogManager.getLogger(RoundKPIInfoPersister.class);

    private static final String CF_NAME = "MQRoundKPIInfo";
    private static final String GAMESESSION_ID_COLUMN = "gsId";
    private static final String ROUND_ID_COLUMN = "rid";

    private static final TableDefinition TABLE = new TableDefinition(CF_NAME,
            Arrays.asList(
                    new ColumnDefinition(GAMESESSION_ID_COLUMN, bigint(), false, false, true),
                    new ColumnDefinition(ROUND_ID_COLUMN, bigint(), false, false, true),
                    new ColumnDefinition(SERIALIZED_COLUMN_NAME, blob()),
                    new ColumnDefinition(JSON_COLUMN_NAME, text())
            ), GAMESESSION_ID_COLUMN);

    public void persist(long gameSessionId, RoundKPIInfo kpiInfo) {
        String json = TABLE.serializeToJson(kpiInfo);
        ByteBuffer buffer = TABLE.serializeToBytes(kpiInfo);
        try {
            com.abs.casino.cassandra.persist.engine.Statement query = com.abs.casino.cassandra.persist.engine.Statement.of(getInsertQuery()
                    .value(GAMESESSION_ID_COLUMN, gameSessionId)
                    .value(ROUND_ID_COLUMN, kpiInfo.getRoundId())
                    .value(SERIALIZED_COLUMN_NAME, buffer)
                    .value(JSON_COLUMN_NAME, json));
            execute(query, "persist");
        } finally {
            releaseBuffer(buffer);
        }
    }

    public List<RoundKPIInfo> load(long gameSessionId) {
        com.abs.casino.cassandra.persist.engine.Statement query = com.abs.casino.cassandra.persist.engine.Statement.of(getSelectColumnsQuery(SERIALIZED_COLUMN_NAME, JSON_COLUMN_NAME)
                .where(eq(GAMESESSION_ID_COLUMN, gameSessionId)));
        ResultSet rs = executeWrapped(query, "load");
        List<RoundKPIInfo> result = new ArrayList<>();
        for (Row row : rs) {
            RoundKPIInfo roundKPIInfo = TABLE.deserializeFromJson(row.getString(JSON_COLUMN_NAME), RoundKPIInfo.class);
            if (roundKPIInfo == null) {
                roundKPIInfo = TABLE.deserializeFrom(row.getBytes(SERIALIZED_COLUMN_NAME), RoundKPIInfo.class);
            }
            result.add(roundKPIInfo);
        }
        return result;
    }

    public void remove(long gameSessionId, long roundId) {
        deleteItem(eq(GAMESESSION_ID_COLUMN, gameSessionId), eq(ROUND_ID_COLUMN, roundId));
    }

    public void remove(long gameSessionId) {
        deleteItem(eq(GAMESESSION_ID_COLUMN, gameSessionId));
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
