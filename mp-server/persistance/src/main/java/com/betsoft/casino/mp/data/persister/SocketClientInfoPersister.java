package com.betsoft.casino.mp.data.persister;

import com.betsoft.casino.mp.model.onlineplayer.SocketClientInfo;
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

public class SocketClientInfoPersister extends AbstractCassandraPersister<String, String>
        implements MapStore<String, SocketClientInfo> {
    private static final Logger LOG = LogManager.getLogger(SocketClientInfoPersister.class);
    private static final String CF_NAME = "SocketClientInfo";

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
    public void store(String key, SocketClientInfo socketClientInfo) {
        ByteBuffer byteBuffer = TABLE.serializeWithClassToBytes(socketClientInfo);
        String json = TABLE.serializeWithClassToJson(socketClientInfo);
        try {
            insert(socketClientInfo.getWebSocketSessionId(), new HashMap<String, Object>() {{ put(SERIALIZED_COLUMN_NAME, byteBuffer); put(JSON_COLUMN_NAME, json); }});
        } finally {
            releaseBuffer(byteBuffer);
        }
    }

    @Override
    public void storeAll(Map<String, SocketClientInfo> map) {
        for (SocketClientInfo socketClientInfo : map.values()) {
            store(socketClientInfo.getWebSocketSessionId(), socketClientInfo);
        }
    }

    @Override
    public void deleteAll(Collection<String> keys) {
        for (String key : keys) {
            delete(key);
        }
    }

    @Override
    public SocketClientInfo load(String webSocketSessionId) {
        String json = getJson(webSocketSessionId);
        SocketClientInfo info = TABLE.deserializeWithClassFromJson(json);
        if (info == null) {
            ByteBuffer bytes = get(webSocketSessionId, SERIALIZED_COLUMN_NAME);
            info = TABLE.deserializeWithClassFrom(bytes);
        }
        return info;
    }

    public Iterable<SocketClientInfo> loadAll() {
        List<SocketClientInfo> result = new ArrayList<>();
        Iterator<com.datastax.driver.core.Row> it = getAll();
        while (it.hasNext()) {
            com.datastax.driver.core.Row row = it.next();
            SocketClientInfo socketClientInfo = TABLE.deserializeWithClassFromJson(row.getString(JSON_COLUMN_NAME));
            if (socketClientInfo == null) {
                socketClientInfo = TABLE.deserializeWithClassFrom(row.getBytes(SERIALIZED_COLUMN_NAME));
            }
            result.add(socketClientInfo);
        }
        return result;
    }

    @Override
    public Map<String, SocketClientInfo> loadAll(Collection<String> keys) {
        HashMap<String, SocketClientInfo> result = new HashMap<>(keys.size());
        for (String key : keys) {
            SocketClientInfo socketClientInfo = load(key);
            if (socketClientInfo != null) {
                result.put(key, socketClientInfo);
            }
        }
        return result;
    }

    @Override
    public void delete(String webSocketSessionId) {
        deleteWithCheck(webSocketSessionId);
    }
}
