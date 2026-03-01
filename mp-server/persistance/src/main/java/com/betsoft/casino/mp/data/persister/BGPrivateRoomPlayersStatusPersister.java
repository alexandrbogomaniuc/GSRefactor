package com.betsoft.casino.mp.data.persister;

import com.betsoft.casino.mp.model.privateroom.PrivateRoom;
import com.abs.casino.cassandra.persist.engine.AbstractCassandraPersister;
import com.abs.casino.cassandra.persist.engine.ColumnDefinition;
import com.abs.casino.cassandra.persist.engine.TableDefinition;
import com.hazelcast.core.MapStore;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.toSet;

public class BGPrivateRoomPlayersStatusPersister extends AbstractCassandraPersister<String, String>
        implements MapStore<String, PrivateRoom> {
    private static final Logger LOG = LogManager.getLogger(BGPrivateRoomPlayersStatusPersister.class);
    private static final String CF_NAME = "BGPrivateRoomPlayersStatuses";

    private static final TableDefinition TABLE = new TableDefinition(
            CF_NAME,
            Arrays.asList(
                    new ColumnDefinition(KEY, com.datastax.driver.core.DataType.text(), false, false, true),
                    new ColumnDefinition(SERIALIZED_COLUMN_NAME, com.datastax.driver.core.DataType.blob()),
                    new ColumnDefinition(JSON_COLUMN_NAME, com.datastax.driver.core.DataType.text())
            ), KEY);

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

    @Override
    public Set<String> loadAllKeys() {
        com.datastax.driver.core.ResultSet resultSet = execute(getSelectColumnsQuery(KEY), "loadAllKeys");

        return StreamSupport.stream(resultSet.spliterator(), false)
                .map(row -> row.getString(KEY))
                .collect(toSet());
    }

    @Override
    public void store(String key, PrivateRoom privateRoom) {
        String json = TABLE.serializeWithClassToJson(privateRoom);
        ByteBuffer byteBuffer = TABLE.serializeWithClassToBytes(privateRoom);
        try {
            insert(privateRoom.getPrivateRoomId(), new HashMap<String, Object>() {{ put(SERIALIZED_COLUMN_NAME, byteBuffer); put(JSON_COLUMN_NAME, json); }});
        } finally {
            releaseBuffer(byteBuffer);
        }
    }

    @Override
    public void storeAll(Map<String, PrivateRoom> map) {
        for (PrivateRoom privateRoom : map.values()) {
            store(privateRoom.getPrivateRoomId(), privateRoom);
        }
    }

    @Override
    public void deleteAll(Collection<String> keys) {
        for (String key : keys) {
            delete(key);
        }
    }

    @Override
    public PrivateRoom load(String privateRoomId) {
        String json = getJson(privateRoomId);
        ByteBuffer bytes = get(privateRoomId, SERIALIZED_COLUMN_NAME);

        PrivateRoom pr = TABLE.deserializeWithClassFromJson(json);
        if (pr == null) {
            pr = TABLE.deserializeWithClassFrom(bytes);
        }
        return pr;
    }

    public Iterable<PrivateRoom> loadAll() {
        List<PrivateRoom> result = new ArrayList<>();
        Iterator<com.datastax.driver.core.Row> it = getAll();
        while (it.hasNext()) {
            com.datastax.driver.core.Row row = it.next();
            PrivateRoom privateRoom = TABLE.deserializeWithClassFromJson(row.getString(JSON_COLUMN_NAME));
            if (privateRoom == null) {
                privateRoom = TABLE.deserializeWithClassFrom(row.getBytes(SERIALIZED_COLUMN_NAME));
            }
            result.add(privateRoom);
        }
        return result;
    }

    @Override
    public Map<String, PrivateRoom> loadAll(Collection<String> keys) {
        HashMap<String, PrivateRoom> result = new HashMap<>(keys.size());
        for (String key : keys) {
            PrivateRoom privateRoom = load(key);
            if (privateRoom != null) {
                result.put(key, privateRoom);
            }
        }
        return result;
    }

    @Override
    public void delete(String privateRoomId) {
        deleteWithCheck(privateRoomId);
    }
}
