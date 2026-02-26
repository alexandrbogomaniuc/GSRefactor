package com.betsoft.casino.mp.data.persister;

import com.betsoft.casino.mp.model.SpawnConfigEntity;
import com.betsoft.casino.mp.service.ISpawnConfigService;
import com.dgphoenix.casino.cassandra.persist.engine.AbstractCassandraPersister;
import com.dgphoenix.casino.cassandra.persist.engine.ColumnDefinition;
import com.dgphoenix.casino.cassandra.persist.engine.TableDefinition;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class SpawnConfigPersister extends AbstractCassandraPersister<Long, String>
        implements ISpawnConfigService<SpawnConfigEntity> {
    private static final Logger LOG = LogManager.getLogger(SpawnConfigPersister.class);

    private static final String CF_NAME = "SpawnConfigs";
    private static final String ROOM_ID_COLUMN = "gid";

    private static final TableDefinition TABLE = new TableDefinition(CF_NAME,
            Arrays.asList(
                    new ColumnDefinition(ROOM_ID_COLUMN, com.datastax.driver.core.DataType.bigint(), false, false, true),
                    new ColumnDefinition(SERIALIZED_COLUMN_NAME, com.datastax.driver.core.DataType.blob()),
                    new ColumnDefinition(JSON_COLUMN_NAME, com.datastax.driver.core.DataType.text())
            ), ROOM_ID_COLUMN);

    @Override
    public void save(long roomId, SpawnConfigEntity spawnConfig) {
        LOG.debug("save spawn config for roomId: {}, spawnConfig: {}", roomId, spawnConfig);
        String json = TABLE.serializeToJson(spawnConfig);
        ByteBuffer buffer = TABLE.serializeToBytes(spawnConfig);
        try {
            execute(getUpdateQuery()
                            .where(eq(ROOM_ID_COLUMN, roomId))
                            .with(set(SERIALIZED_COLUMN_NAME, buffer))
                            .and(set(JSON_COLUMN_NAME, json)),
                    "update");
        } finally {
            releaseBuffer(buffer);
        }
    }

    @Override
    public void removeConfig(long roomId) {
        deleteItem(eq(ROOM_ID_COLUMN, roomId));
        LOG.debug("remove spawnConfig for roomId: {}", roomId);
    }

    @Override
    public SpawnConfigEntity load(long roomId) {
        com.datastax.driver.core.Row result = execute(getSelectColumnsQuery(TABLE, SERIALIZED_COLUMN_NAME, ROOM_ID_COLUMN, JSON_COLUMN_NAME)
                        .where()
                        .and(eq(ROOM_ID_COLUMN, roomId))
                        .limit(1),
                "load").one();
        if (result == null) {
            return null;
        }
        SpawnConfigEntity entity = TABLE
                .deserializeFrom(result.getBytes(SERIALIZED_COLUMN_NAME), SpawnConfigEntity.class);
        if (entity == null) {
            entity = TABLE
                    .deserializeFrom(result.getBytes(SERIALIZED_COLUMN_NAME), SpawnConfigEntity.class);
        }
        return entity;
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
