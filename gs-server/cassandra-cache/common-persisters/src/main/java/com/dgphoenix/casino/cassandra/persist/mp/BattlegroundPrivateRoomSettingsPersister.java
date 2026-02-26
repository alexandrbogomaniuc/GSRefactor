package com.dgphoenix.casino.cassandra.persist.mp;

import com.datastax.driver.core.DataType;
import com.datastax.driver.core.Row;
import com.dgphoenix.casino.cassandra.persist.engine.AbstractCassandraPersister;
import com.dgphoenix.casino.cassandra.persist.engine.ColumnDefinition;
import com.dgphoenix.casino.cassandra.persist.engine.TableDefinition;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class BattlegroundPrivateRoomSettingsPersister extends AbstractCassandraPersister<String, String> {
    private static final Logger LOG = LogManager.getLogger(BattlegroundPrivateRoomSettingsPersister.class);

    private static final String CF_NAME = "BattlegroundPrivateRoomSettings";

    private static final String PRIVATE_ROOM_ID = "privateRoomId";

    private static final TableDefinition TABLE = new TableDefinition(
            CF_NAME,
            Arrays.asList(
                    new ColumnDefinition(PRIVATE_ROOM_ID, DataType.text(), false, false, true),
                    new ColumnDefinition(SERIALIZED_COLUMN_NAME, DataType.blob()),
                    new ColumnDefinition(JSON_COLUMN_NAME, DataType.text())
            ), PRIVATE_ROOM_ID);

    public void create(String privateRoomId, BattlegroundPrivateRoomSetting battlegroundPrivateRoomSetting) {
        ByteBuffer buffer = TABLE.serializeToBytes(battlegroundPrivateRoomSetting);
        String json = TABLE.serializeToJson(battlegroundPrivateRoomSetting);
        com.datastax.driver.core.Statement query = getInsertQuery()
                .value(PRIVATE_ROOM_ID, privateRoomId)
                .value(SERIALIZED_COLUMN_NAME, buffer)
                .value(JSON_COLUMN_NAME, json);
        execute(query, "persist");
        getLog().info("create: privateRoomId: {}, BattlegroundPrivateRoomSetting: {}", privateRoomId, battlegroundPrivateRoomSetting);
    }

    public BattlegroundPrivateRoomSetting load(String privateRoomId) {
        com.datastax.driver.core.Statement query = getSelectColumnsQuery(SERIALIZED_COLUMN_NAME, JSON_COLUMN_NAME)
                .where(eq(PRIVATE_ROOM_ID, privateRoomId))
                .limit(1);
        Row row = execute(query, "load").one();
        if (row == null) {
            return null;
        }
        BattlegroundPrivateRoomSetting bprs = TABLE.deserializeFromJson(row.getString(JSON_COLUMN_NAME), BattlegroundPrivateRoomSetting.class);
        if (bprs == null) {
            bprs = TABLE.deserializeFrom(row.getBytes(SERIALIZED_COLUMN_NAME), BattlegroundPrivateRoomSetting.class);
        }
        return bprs ;
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
