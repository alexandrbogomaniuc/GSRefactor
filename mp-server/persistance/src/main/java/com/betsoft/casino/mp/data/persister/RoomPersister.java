package com.betsoft.casino.mp.data.persister;

import com.betsoft.casino.mp.model.room.IRoom;
import com.datastax.driver.core.DataType;
import com.dgphoenix.casino.cassandra.persist.engine.AbstractCassandraPersister;
import com.dgphoenix.casino.cassandra.persist.engine.ColumnDefinition;
import com.dgphoenix.casino.cassandra.persist.engine.TableDefinition;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Optional;

public class RoomPersister extends AbstractCassandraPersister<Long, String> {
    private static final Logger LOG = LogManager.getLogger(RoomPersister.class);

    private static final String CF_NAME = "Room";
    private static final String KEY_COLUMN = "id";

    private static final TableDefinition TABLE = new TableDefinition(CF_NAME,
            Arrays.asList(
                    new ColumnDefinition(KEY_COLUMN, DataType.bigint(), false, false, true),
                    new ColumnDefinition(SERIALIZED_COLUMN_NAME, DataType.blob()),
                    new ColumnDefinition(JSON_COLUMN_NAME, DataType.text())
            ), KEY_COLUMN);

    public Optional<IRoom> get(Long id) {
        String json = getJson(id);
        IRoom room = TABLE.deserializeWithClassFromJson(json);
        if (room == null) {
            ByteBuffer bytes = get(id, SERIALIZED_COLUMN_NAME);
            room = TABLE.deserializeWithClassFrom(bytes);
        }
        return Optional.ofNullable(room);
    }

    public void persist(IRoom room) {
        ByteBuffer byteBuffer = TABLE.serializeWithClassToBytes(room);
        String json = TABLE.serializeWithClassToJson(room);
        try {
            insert(room.getId(), new HashMap<String, Object>() {{ put(SERIALIZED_COLUMN_NAME, byteBuffer); put(JSON_COLUMN_NAME, json); }});
        } finally {
            releaseBuffer(byteBuffer);
        }
    }

    @Override
    public TableDefinition getMainTableDefinition() {
        return TABLE;
    }

    @Override
    public String getMainColumnFamilyName() {
        return CF_NAME;
    }

    @Override
    public Logger getLog() {
        return LOG;
    }
}
