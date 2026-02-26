package com.betsoft.casino.mp.data.persister;

import com.betsoft.casino.mp.model.IGameRoomSnapshot;
import com.betsoft.casino.mp.service.IGameRoomSnapshotPersister;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.DataType;
import com.datastax.driver.core.Row;
import com.dgphoenix.casino.cassandra.persist.engine.AbstractCassandraPersister;
import com.dgphoenix.casino.cassandra.persist.engine.ColumnDefinition;
import com.dgphoenix.casino.cassandra.persist.engine.TableDefinition;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * User: flsh
 * Date: 28.09.18.
 */
public class GameRoomSnapshotPersister extends AbstractCassandraPersister<Long, String>
        implements IGameRoomSnapshotPersister {
    private static final Logger LOG = LogManager.getLogger(GameRoomSnapshotPersister.class);
    private static final String CF_NAME = "GameRoomSnapshot";
    private static final String ROOM_ID_COLUMN = "RoomId";
    private static final String ROUND_ID_COLUMN = "RoundId";

    private static final TableDefinition TABLE = new TableDefinition(CF_NAME,
            Arrays.asList(
                    new ColumnDefinition(ROOM_ID_COLUMN, DataType.bigint(), false, false, true),
                    new ColumnDefinition(ROUND_ID_COLUMN, DataType.bigint(), false, false, true),
                    new ColumnDefinition(SERIALIZED_COLUMN_NAME, DataType.blob()),
                    new ColumnDefinition(JSON_COLUMN_NAME, DataType.text())
            ), ROOM_ID_COLUMN);


    @Override
    public void init() {
        getLog().info("Init");
        super.init();
    }

    @Override
    public void shutdown() {
        getLog().info("Shutdown");
        super.shutdown();
    }

    @Override
    public TableDefinition getMainTableDefinition() {
        return TABLE;
    }

    @Override
    public Logger getLog() {
        return LOG;
    }

    @Override
    public void persist(IGameRoomSnapshot snapshot) {
        ByteBuffer byteBuffer = TABLE.serializeWithClassToBytes(snapshot);
        String json = TABLE.serializeWithClassToJson(snapshot);
        try {
            execute(getInsertQuery()
                            .value(ROOM_ID_COLUMN, snapshot.getRoomId())
                            .value(ROUND_ID_COLUMN, snapshot.getRoundId())
                            .value(SERIALIZED_COLUMN_NAME, byteBuffer)
                            .value(JSON_COLUMN_NAME, json),
                    "persist", ConsistencyLevel.LOCAL_QUORUM);
        } finally {
            releaseBuffer(byteBuffer);
        }
    }

    @Override
    public IGameRoomSnapshot get(long roomId, long roundId) {
        com.datastax.driver.core.ResultSet resultSet = execute(getSelectColumnsQuery(SERIALIZED_COLUMN_NAME, JSON_COLUMN_NAME)
                        .where(eq(ROOM_ID_COLUMN, roomId))
                        .and(eq(ROUND_ID_COLUMN, roundId)),
                "get");
        Row row = resultSet.one();
        if (row == null) {
            return null;
        }
        IGameRoomSnapshot snapshot = 
                TABLE.deserializeWithClassFromJson(row.getString(JSON_COLUMN_NAME));

        if (snapshot == null) {
            snapshot = 
                    TABLE.deserializeWithClassFrom(row.getBytes(SERIALIZED_COLUMN_NAME));
        }
        return snapshot;
    }
}
