package com.betsoft.casino.mp.data.persister;

import com.betsoft.casino.mp.model.room.IRoomInfo;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.dgphoenix.casino.cassandra.persist.engine.AbstractCassandraPersister;
import com.hazelcast.core.MapStore;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.toSet;

/**
 * User: flsh
 * Date: 28.09.18.
 */
public abstract class AbstractRoomInfoPersister<ROOM_INFO extends IRoomInfo> extends AbstractCassandraPersister<Long, String>
        implements MapStore<Long, ROOM_INFO> {
    protected static final String ROOM_ID_COLUMN = "RoomId";

    @Override
    protected String getKeyColumnName() {
        return ROOM_ID_COLUMN;
    }

    @Override
    public void store(Long key, ROOM_INFO roomInfo) {
        persist(roomInfo);
    }

    @Override
    public void storeAll(Map<Long, ROOM_INFO> map) {
        for (ROOM_INFO roomInfo : map.values()) {
            persist(roomInfo);
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
    public ROOM_INFO load(Long key) {
        return get(key);
    }

    @Override
    public Map<Long, ROOM_INFO> loadAll(Collection<Long> keys) {
        HashMap<Long, ROOM_INFO> result = new HashMap<>(keys.size());
        for (Long key : keys) {
            ROOM_INFO config = load(key);
            if (config != null) {
                result.put(key, config);
            }
        }
        return result;
    }

    @Override
    public Iterable<Long> loadAllKeys() {
        Select query = QueryBuilder.select(ROOM_ID_COLUMN).from(getMainColumnFamilyName());
        ResultSet resultSet = execute(query, "loadAllKeys");
        return StreamSupport.stream(resultSet.spliterator(), false)
                .map(row -> row.getLong(ROOM_ID_COLUMN))
                .collect(toSet());
    }

    public void persist(ROOM_INFO roomInfo) {
        ByteBuffer byteBuffer = getMainTableDefinition().serializeWithClassToBytes(roomInfo);
        String json = getMainTableDefinition().serializeWithClassToJson(roomInfo);
        try {
            insert(roomInfo.getId(), new HashMap<String,Object>(){{ put(SERIALIZED_COLUMN_NAME, byteBuffer); put(JSON_COLUMN_NAME, json); }});
        } finally {
            releaseBuffer(byteBuffer);
        }
    }

    public ROOM_INFO get(Long id) {
        String json = getJson(id);
        ROOM_INFO ri = (ROOM_INFO) getMainTableDefinition().deserializeWithClassFromJson(json);
        if (ri == null) {
            ByteBuffer bytes = get(id, SERIALIZED_COLUMN_NAME);
            ri = getMainTableDefinition().deserializeWithClassFrom(bytes);
        }
        return ri;
    }
}
