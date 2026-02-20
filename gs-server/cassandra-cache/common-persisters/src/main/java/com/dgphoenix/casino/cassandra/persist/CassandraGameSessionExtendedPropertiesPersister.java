package com.dgphoenix.casino.cassandra.persist;

import com.datastax.driver.core.DataType;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.querybuilder.Insert;
import com.datastax.driver.core.querybuilder.Select;
import com.dgphoenix.casino.cassandra.persist.engine.AbstractCassandraPersister;
import com.dgphoenix.casino.cassandra.persist.engine.ColumnDefinition;
import com.dgphoenix.casino.cassandra.persist.engine.TableDefinition;
import com.dgphoenix.casino.common.GameSessionExtendedProperties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class CassandraGameSessionExtendedPropertiesPersister extends AbstractCassandraPersister<Long, String> {
    private static final Logger LOG = LogManager.getLogger(CassandraGameSessionExtendedPropertiesPersister.class);

    private static final String CF_NAME = "GSessionExtProps";
    private static final String GAME_SESSION_ID = "g";

    private static final TableDefinition TABLE = new TableDefinition(CF_NAME,
            Arrays.asList(
                    new ColumnDefinition(GAME_SESSION_ID, DataType.bigint(), false, false, true),
                    new ColumnDefinition(SERIALIZED_COLUMN_NAME, DataType.blob()),
                    new ColumnDefinition(JSON_COLUMN_NAME, DataType.text())
            ), GAME_SESSION_ID);

    public void persist(long gameSessionId, GameSessionExtendedProperties properties) {
        String json = TABLE.serializeWithClassToJson(properties);
        ByteBuffer buffer = TABLE.serializeWithClassToBytes(properties);
        try {
            Insert insert = getInsertQuery()
                    .value(GAME_SESSION_ID, gameSessionId)
                    .value(SERIALIZED_COLUMN_NAME, buffer)
                    .value(JSON_COLUMN_NAME, json);
            execute(insert, "persist");
        } finally {
            releaseBuffer(buffer);
        }
    }

    public GameSessionExtendedProperties get(long gameSessionId) {
        GameSessionExtendedProperties props = getOrNull(gameSessionId);
        return props != null ? props : new GameSessionExtendedProperties();
    }

    public GameSessionExtendedProperties getOrNull(long gameSessionId) {
        Select select = getSelectAllColumnsQuery()
                .where(eq(GAME_SESSION_ID, gameSessionId))
                .limit(1);
        Row result = execute(select, "select").one();
        if (result == null) {
            return null;
        }
        GameSessionExtendedProperties p = 
                TABLE.deserializeWithClassFromJson(result.getString(JSON_COLUMN_NAME));
        if (p == null) {
            p = TABLE.deserializeWithClassFrom(result.getBytes(SERIALIZED_COLUMN_NAME));
        }
        return p;
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
