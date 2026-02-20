package com.betsoft.casino.mp.data.persister;

import com.betsoft.casino.mp.model.GameConfigEntity;
import com.betsoft.casino.mp.service.IGameConfigService;
import com.datastax.driver.core.DataType;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.querybuilder.Select;
import com.datastax.driver.core.querybuilder.Update;
import com.dgphoenix.casino.cassandra.persist.engine.AbstractCassandraPersister;
import com.dgphoenix.casino.cassandra.persist.engine.ColumnDefinition;
import com.dgphoenix.casino.cassandra.persist.engine.TableDefinition;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.nio.ByteBuffer;
import java.util.*;

public class GameConfigPersister extends AbstractCassandraPersister<Long, String>
        implements IGameConfigService<GameConfigEntity> {
    private static final Logger LOG = LogManager.getLogger(GameConfigPersister.class);

    private static final String CF_NAME = "GameConfigs";
    private static final String ROOM_ID_COLUMN = "gid";

    private static final TableDefinition TABLE = new TableDefinition(CF_NAME,
            Arrays.asList(
                    new ColumnDefinition(ROOM_ID_COLUMN, DataType.bigint(), false, false, true),
                    new ColumnDefinition(SERIALIZED_COLUMN_NAME, DataType.blob()),
                    new ColumnDefinition(JSON_COLUMN_NAME, DataType.text())
            ), ROOM_ID_COLUMN);


    @Override
    public TableDefinition getMainTableDefinition() {
        return TABLE;
    }

    @Override
    public String getMainColumnFamilyName() {
        return CF_NAME;
    }

    @Override
    public List<TableDefinition> getAllTableDefinitions() {
        return Collections.singletonList(TABLE);
    }

    @Override
    public Logger getLog() {
        return LOG;
    }

    @Override
    public GameConfigEntity load(long roomId) {
        Select query = getSelectColumnsQuery(TABLE, SERIALIZED_COLUMN_NAME, ROOM_ID_COLUMN, JSON_COLUMN_NAME);
        query.where()
                .and(eq(ROOM_ID_COLUMN, roomId))
                .limit(1);
        Row result = execute(query, "load").one();
        if (result == null) {
            return null;
        }
        GameConfigEntity entity = 
                TABLE.deserializeFromJson(result.getString(JSON_COLUMN_NAME), GameConfigEntity.class);
        if (entity == null) {
            entity = TABLE.deserializeFrom(result.getBytes(SERIALIZED_COLUMN_NAME), GameConfigEntity.class);
        }
        return entity;
    }

    @Override
    public void save(long roomId, GameConfigEntity gameConfig) {
        LOG.debug("save config for roomId: {}, gameConfig: {}" , roomId, gameConfig);
        ByteBuffer buffer = TABLE.serializeToBytes(gameConfig);
        String json = TABLE.serializeToJson(gameConfig);
        try {
            Update.Assignments update = getUpdateQuery()
                    .where(eq(ROOM_ID_COLUMN, roomId))
                    .with(set(SERIALIZED_COLUMN_NAME, buffer))
                    .and(set(JSON_COLUMN_NAME, json));
            execute(update, "update");
        } finally {
            releaseBuffer(buffer);
        }
    }

    public void removeConfig(long roomId) {
        deleteItem(eq(ROOM_ID_COLUMN, roomId));
        LOG.debug("removeConfig for roomId: {}", roomId);
    }
}
