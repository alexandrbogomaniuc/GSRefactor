package com.dgphoenix.casino.promo.persisters;

import com.datastax.driver.core.DataType;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.dgphoenix.casino.cassandra.persist.engine.AbstractCassandraPersister;
import com.dgphoenix.casino.cassandra.persist.engine.ColumnDefinition;
import com.dgphoenix.casino.cassandra.persist.engine.TableDefinition;
import com.dgphoenix.casino.cassandra.persist.engine.configuration.CompactionStrategy;
import com.dgphoenix.casino.common.promo.battleground.BattlegroundConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * User: flsh
 * Date: 24.06.2021.
 */
public class CassandraBattlegroundConfigPersister extends AbstractCassandraPersister<Long, String> {
    private static final Logger LOG = LogManager.getLogger(CassandraBattlegroundConfigPersister.class);
    private static final String BG_CONFIG_CF = "BattleGroundCF";
    private static final String BANK_ID = "bankId";
    private static final String GAME_ID = "gameId";

    private static final TableDefinition BG_CONFIG_TABLE = new TableDefinition(BG_CONFIG_CF,
            Arrays.asList(
                    new ColumnDefinition(BANK_ID, DataType.bigint(), false, false, true),
                    new ColumnDefinition(GAME_ID, DataType.bigint(), false, false, true),
                    new ColumnDefinition(SERIALIZED_COLUMN_NAME, DataType.blob(), false, false, false),
                    new ColumnDefinition(JSON_COLUMN_NAME, DataType.text(), false, false, false)
            ), BANK_ID)
            .compaction(CompactionStrategy.LEVELED);

    @Override
    public TableDefinition getMainTableDefinition() {
        return BG_CONFIG_TABLE;
    }

    public void save(long bankId, BattlegroundConfig config) {
        String json = getMainTableDefinition().serializeToJson(config);
        ByteBuffer byteBuffer = getMainTableDefinition().serializeToBytes(config);
        try {
            execute(getInsertQuery(getTtl())
                    .value(BANK_ID, bankId)
                    .value(GAME_ID, config.getGameId())
                    .value(SERIALIZED_COLUMN_NAME, byteBuffer)
                    .value(JSON_COLUMN_NAME, json), "save");
        } finally {
            releaseBuffer(byteBuffer);
        }
    }

    public Set<BattlegroundConfig> getConfigs(long bankId) {
        com.datastax.driver.core.ResultSet resultSet = execute(getSelectColumnsQuery(SERIALIZED_COLUMN_NAME, JSON_COLUMN_NAME)
                .where(eq(BANK_ID, bankId)), "getConfigs");
        Set<BattlegroundConfig> result = new HashSet<>();
        for (Row row : resultSet) {
            String json = row.getString(JSON_COLUMN_NAME);
            ByteBuffer buffer = row.getBytes(SERIALIZED_COLUMN_NAME);
            BattlegroundConfig config = getMainTableDefinition().deserializeFromJson(json, BattlegroundConfig.class);
            if (config == null) {
                config = getMainTableDefinition().deserializeFrom(buffer, BattlegroundConfig.class);
            }
            result.add(config);
        }
        return result;
    }

    public BattlegroundConfig getConfig(long bankId, long gameId) {
        com.datastax.driver.core.ResultSet resultSet = execute(getSelectColumnsQuery(SERIALIZED_COLUMN_NAME, JSON_COLUMN_NAME)
                .where(eq(BANK_ID, bankId))
                .and(eq(GAME_ID, gameId)), "getConfig");
        Row row = resultSet.one();
        BattlegroundConfig result = null;
        if (row != null) {
            String json = row.getString(JSON_COLUMN_NAME);
            ByteBuffer buffer = row.getBytes(SERIALIZED_COLUMN_NAME);
            result = getMainTableDefinition().deserializeFromJson(json, BattlegroundConfig.class);

            if (result == null) {
                result = getMainTableDefinition().deserializeFrom(buffer, BattlegroundConfig.class);
            }
        }
        return result;
    }

    public void deleteForBank(long bankId) {
        execute(QueryBuilder.delete()
                .from(getMainColumnFamilyName())
                .where(eq(BANK_ID, bankId)), "deleteForBank");
    }

    public void delete(long bankId, long gameId) {
        execute(QueryBuilder.delete()
                .from(getMainColumnFamilyName())
                .where(eq(BANK_ID, bankId))
                .and(eq(GAME_ID, gameId)), "delete");
    }

    @Override
    public Logger getLog() {
        return LOG;
    }
}
