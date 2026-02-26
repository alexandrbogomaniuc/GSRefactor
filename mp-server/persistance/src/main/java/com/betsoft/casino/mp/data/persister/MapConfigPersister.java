package com.betsoft.casino.mp.data.persister;

import com.betsoft.casino.mp.model.IMapConfigEntity;
import com.betsoft.casino.mp.model.MapConfigEntity;
import com.betsoft.casino.mp.service.IMapConfigService;
import com.datastax.driver.core.DataType;
import com.dgphoenix.casino.cassandra.persist.engine.AbstractCassandraPersister;
import com.dgphoenix.casino.cassandra.persist.engine.ColumnDefinition;
import com.dgphoenix.casino.cassandra.persist.engine.TableDefinition;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class MapConfigPersister extends AbstractCassandraPersister<String, String> implements IMapConfigService {
    private static final Logger LOG = LogManager.getLogger(MapConfigPersister.class);

    private static final String CF_NAME = "MapConfigs";
    private static final String MAP_ID_COLUMN = "m";

    private static final TableDefinition TABLE = new TableDefinition(CF_NAME,
            Arrays.asList(
                    new ColumnDefinition(MAP_ID_COLUMN, DataType.cint(), false, false, true),
                    new ColumnDefinition(SERIALIZED_COLUMN_NAME, DataType.blob()),
                    new ColumnDefinition(JSON_COLUMN_NAME, DataType.text())
            ), MAP_ID_COLUMN);

    @Override
    public TableDefinition getMainTableDefinition() {
        return TABLE;
    }

    @Override
    public Logger getLog() {
        return LOG;
    }


    @Override
    public void save(int mapId, IMapConfigEntity mapConfig) {
        LOG.debug("save config for mapId: {}, gameConfig: {}", mapId, mapConfig);
        ByteBuffer buffer = TABLE.serializeToBytes(mapConfig);
        String json = TABLE.serializeToJson(mapConfig);
        try {
            execute(getUpdateQuery()
                            .where(eq(MAP_ID_COLUMN, mapId))
                            .with(set(SERIALIZED_COLUMN_NAME, buffer))
                            .and(set(JSON_COLUMN_NAME, json)),
                    "update mapConfig");
        } finally {
            releaseBuffer(buffer);
        }
    }

    @Override
    public void removeConfig(int mapId) {
        deleteItem(eq(MAP_ID_COLUMN, mapId));
    }

    @Override
    public IMapConfigEntity load(int mapId) {
        com.datastax.driver.core.Row result = execute(getSelectColumnsQuery(TABLE, SERIALIZED_COLUMN_NAME, MAP_ID_COLUMN, JSON_COLUMN_NAME)
                        .where(eq(MAP_ID_COLUMN, mapId))
                        .limit(1),
                "load mapConfig").one();
        if (result == null) {
            return null;
        }
        MapConfigEntity entity =
                TABLE.deserializeFromJson(result.getString(JSON_COLUMN_NAME), MapConfigEntity.class);

        if (entity == null) {
            entity = TABLE.deserializeFrom(result.getBytes(SERIALIZED_COLUMN_NAME), MapConfigEntity.class);
        }

        return entity;
    }
}
