package com.betsoft.casino.mp.data.persister;

import com.betsoft.casino.mp.model.IRoomPlayerInfo;
import com.abs.casino.cassandra.persist.engine.AbstractCassandraPersister;
import com.abs.casino.cassandra.persist.engine.ColumnDefinition;
import com.abs.casino.cassandra.persist.engine.TableDefinition;
import com.hazelcast.core.MapStore;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.toSet;

/**
 * User: flsh
 * Date: 30.09.18.
 */
public class RoomPlayerInfoPersister extends AbstractCassandraPersister<Long, String> implements MapStore<Long, IRoomPlayerInfo> {
    private static final Logger LOG = LogManager.getLogger(RoomPlayerInfoPersister.class);
    private static final String CF_NAME = "RoomPlayerInfo";

    //key is accountId
    private static final TableDefinition TABLE = new TableDefinition(CF_NAME,
            Arrays.asList(
                    new ColumnDefinition(KEY, com.datastax.driver.core.DataType.bigint(), false, false, true),
                    new ColumnDefinition(SERIALIZED_COLUMN_NAME, com.datastax.driver.core.DataType.blob()),
                    new ColumnDefinition(JSON_COLUMN_NAME, com.datastax.driver.core.DataType.text())
            ), KEY);

    @Override
    public TableDefinition getMainTableDefinition() {
        return TABLE;
    }


    @Override
    public Logger getLog() {
        return LOG;
    }

    @Override
    public void store(Long key, IRoomPlayerInfo roomPlayerInfo) {
        persist(roomPlayerInfo);
    }

    @Override
    public void storeAll(Map<Long, IRoomPlayerInfo> map) {
        for (IRoomPlayerInfo roomPlayerInfo : map.values()) {
            persist(roomPlayerInfo);
        }
    }

    @Override
    public void delete(Long key) {
        deleteWithCheck(key);
    }

    @Override
    public void deleteAll(Collection<Long> keys) {
        for (Long key : keys) {
            delete(key);
        }
    }

    @Override
    public IRoomPlayerInfo load(Long key) {
        return get(key);
    }

    @Override
    public Map<Long, IRoomPlayerInfo> loadAll(Collection<Long> keys) {
        HashMap<Long, IRoomPlayerInfo> result = new HashMap<>(keys.size());
        for (Long key : keys) {
            IRoomPlayerInfo config = load(key);
            if (config != null) {
                result.put(key, config);
            }
        }
        return result;
    }

    @Override
    public Iterable<Long> loadAllKeys() {
        com.datastax.driver.core.ResultSet resultSet = execute(getSelectColumnsQuery(KEY), "loadAllKeys");
        return StreamSupport.stream(resultSet.spliterator(), false)
                .map(row -> row.getLong(KEY))
                .collect(toSet());
    }

    public void persist(IRoomPlayerInfo roomPlayerInfo) {
        ByteBuffer buffer = TABLE.serializeWithClassToBytes(roomPlayerInfo);
        String json = TABLE.serializeWithClassToJson(roomPlayerInfo);
        try {
            execute(getInsertQuery()
                            .value(KEY, roomPlayerInfo.getId())
                            .value(SERIALIZED_COLUMN_NAME, buffer)
                            .value(JSON_COLUMN_NAME, json),
                    "persist");
        } finally {
            releaseBuffer(buffer);
        }
    }

    public IRoomPlayerInfo get(Long id) {
        String json = getJson(id);
        IRoomPlayerInfo info = TABLE.deserializeWithClassFromJson(json);
        if (info == null) {
            ByteBuffer bytes = get(id, SERIALIZED_COLUMN_NAME);
            info = TABLE.deserializeWithClassFrom(bytes);
        }
        return info;
    }
}
