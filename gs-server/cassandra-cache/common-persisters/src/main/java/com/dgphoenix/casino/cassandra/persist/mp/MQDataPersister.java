package com.abs.casino.cassandra.persist.mp;

import com.dgphoenix.casino.cassandra.persist.engine.AbstractCassandraPersister;
import com.dgphoenix.casino.cassandra.persist.engine.ColumnDefinition;
import com.dgphoenix.casino.cassandra.persist.engine.TableDefinition;
import com.dgphoenix.casino.common.mp.MQData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class MQDataPersister extends AbstractCassandraPersister<Long, String> {
    private static final Logger LOG = LogManager.getLogger(MQDataPersister.class);

    private static final String CF_NAME = "MQData";
    private static final String ACCOUNT_ID_COLUMN = "a";
    private static final String GAME_ID_COLUMN = "g";

    private static final TableDefinition TABLE = new TableDefinition(CF_NAME,
            Arrays.asList(
                    new ColumnDefinition(ACCOUNT_ID_COLUMN, com.datastax.driver.core.DataType.bigint(), false, false, true),
                    new ColumnDefinition(GAME_ID_COLUMN, com.datastax.driver.core.DataType.bigint(), false, false, true),
                    new ColumnDefinition(SERIALIZED_COLUMN_NAME, com.datastax.driver.core.DataType.blob()),
                    new ColumnDefinition(JSON_COLUMN_NAME, com.datastax.driver.core.DataType.text())
            ), ACCOUNT_ID_COLUMN);

    public void persist(MQData data) {
        ByteBuffer buffer = TABLE.serializeToBytes(data);
        String json = TABLE.serializeToJson(data);
        try {
            com.datastax.driver.core.Statement query = getInsertQuery()
                    .value(ACCOUNT_ID_COLUMN, data.getAccountId())
                    .value(GAME_ID_COLUMN, data.getGameId())
                    .value(SERIALIZED_COLUMN_NAME, buffer)
                    .value(JSON_COLUMN_NAME, json);
            execute(query, "persist");
        } finally {
            releaseBuffer(buffer);
        }
    }

    public MQData load(long accountId, long gameId) {
        com.datastax.driver.core.Statement query = getSelectColumnsQuery(SERIALIZED_COLUMN_NAME, JSON_COLUMN_NAME)
                .where(eq(ACCOUNT_ID_COLUMN, accountId))
                .and(eq(GAME_ID_COLUMN, gameId))
                .limit(1);
        com.datastax.driver.core.Row row = execute(query, "load").one();
        if (row == null) {
            return null;
        }
        MQData mqd = TABLE.deserializeFromJson(row.getString(JSON_COLUMN_NAME), MQData.class);
        if (mqd == null) {
            mqd = TABLE.deserializeFrom(row.getBytes(SERIALIZED_COLUMN_NAME), MQData.class);
        }
        return mqd;
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
