package com.abs.casino.cassandra.persist;

import com.abs.casino.cassandra.persist.engine.Cql;

import com.abs.casino.cassandra.persist.engine.AbstractCassandraPersister;
import com.abs.casino.cassandra.persist.engine.ColumnDefinition;
import com.abs.casino.cassandra.persist.engine.TableDefinition;
import com.abs.casino.common.cache.data.account.PlayerGameSettings;
import com.abs.casino.common.cache.data.bank.BankInfo;
import com.abs.casino.common.web.statistics.StatisticsManager;
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
 * Date: 8/24/12
 */
public class CassandraPlayerGameSettingsPersister extends AbstractCassandraPersister<Long, String> {
    public static final String PLAYER_GAME_SETTINGS_CF = "PGSCF";
    private static final Logger LOG = LogManager.getLogger(PlayerGameSettings.class);
    public static final String ACCOUNT_ID_FIELD = "AccountId";
    public static final String GAME_ID_FIELD = "GameId";

    private static final TableDefinition TABLE = new TableDefinition(PLAYER_GAME_SETTINGS_CF,
            Arrays.asList(
                    new ColumnDefinition(ACCOUNT_ID_FIELD, bigint(), false, false, true),
                    new ColumnDefinition(GAME_ID_FIELD, bigint(), false, false, true),
                    new ColumnDefinition(SERIALIZED_COLUMN_NAME, blob()),
                    new ColumnDefinition(JSON_COLUMN_NAME, text())
            ),
            Arrays.asList(ACCOUNT_ID_FIELD, GAME_ID_FIELD));

    private CassandraPlayerGameSettingsPersister() {
    }

    public List<PlayerGameSettings> get(long accountId) {
        com.datastax.driver.core.Statement query = getSelectColumnsQuery(SERIALIZED_COLUMN_NAME, JSON_COLUMN_NAME)
                .where()
                .and(eq(ACCOUNT_ID_FIELD, accountId));
        com.abs.casino.cassandra.persist.engine.ResultSet resultSet = executeWrapped(query, "get");
        List<PlayerGameSettings> result = new ArrayList();
        for (com.abs.casino.cassandra.persist.engine.Row row : resultSet) {
            String json = row.getString(JSON_COLUMN_NAME);
            PlayerGameSettings settings = TABLE.deserializeFromJson(json, PlayerGameSettings.class);

            if (settings == null) {
                ByteBuffer bytes = row.getBytes(SERIALIZED_COLUMN_NAME);
                if (bytes != null) {
                    settings = TABLE.deserializeFrom(bytes, PlayerGameSettings.class);
                }

                if (settings != null) {
                    result.add(settings);
                }
            }
        }
        return result;
    }

    public PlayerGameSettings get(long accountId, int gameId) {
        long now = System.currentTimeMillis();
        PlayerGameSettings result = null;
        com.datastax.driver.core.Statement query = getSelectColumnsQuery(SERIALIZED_COLUMN_NAME, JSON_COLUMN_NAME)
                .where()
                .and(eq(ACCOUNT_ID_FIELD, accountId))
                .and(eq(GAME_ID_FIELD, gameId));
        com.abs.casino.cassandra.persist.engine.ResultSet resultSet = executeWrapped(query, "get");
        com.abs.casino.cassandra.persist.engine.Row row = resultSet.one();
        if (row != null) {
            String json = row.getString(JSON_COLUMN_NAME);
            result = TABLE.deserializeFromJson(json, PlayerGameSettings.class);

            if (json == null) {
                ByteBuffer bytes = row.getBytes(SERIALIZED_COLUMN_NAME);
                if (bytes != null) {
                    result = TABLE.deserializeFrom(bytes, PlayerGameSettings.class);
                }
            }
        }
        StatisticsManager.getInstance().updateRequestStatistics(getMainColumnFamilyName() + "get",
                System.currentTimeMillis() - now);
        return result;
    }

    public void persist(long accountId, PlayerGameSettings entry, BankInfo bankInfo) {
        String json = TABLE.serializeToJson(entry);
        ByteBuffer byteBuffer = TABLE.serializeToBytes(entry);
        try {
            com.datastax.driver.core.Statement query = Cql.insertInto(getMainTableDefinition().getTableName())
                    .value(ACCOUNT_ID_FIELD, accountId)
                    .value(GAME_ID_FIELD, entry.getGameId())
                    .value(SERIALIZED_COLUMN_NAME, byteBuffer)
                    .value(JSON_COLUMN_NAME, json);
            execute(query, "persist");
        } finally {
            releaseBuffer(byteBuffer);
        }
    }

    public void delete(long accountId, int gameId) {
        com.datastax.driver.core.Statement query = Cql.delete()
                .from(getMainColumnFamilyName())
                .where()
                .and(eq(ACCOUNT_ID_FIELD, accountId))
                .and(eq(GAME_ID_FIELD, gameId));
        execute(query, "delete");
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
