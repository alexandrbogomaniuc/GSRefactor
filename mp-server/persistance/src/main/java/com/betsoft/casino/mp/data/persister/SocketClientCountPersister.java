package com.betsoft.casino.mp.data.persister;

import com.betsoft.casino.mp.model.onlineplayer.SocketClientsStats;
import com.dgphoenix.casino.cassandra.persist.engine.AbstractCassandraPersister;
import com.dgphoenix.casino.cassandra.persist.engine.ColumnDefinition;
import com.dgphoenix.casino.cassandra.persist.engine.TableDefinition;
import com.hazelcast.core.MapStore;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.toSet;

public class SocketClientCountPersister extends AbstractCassandraPersister<Long, String>
        implements MapStore<Long, SocketClientsStats> {
    private static final Logger LOG = LogManager.getLogger(SocketClientCountPersister.class);
    private static final String CF_NAME = "SocketClientCount";

    private static final TableDefinition TABLE = new TableDefinition(
            CF_NAME,
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
    public String getMainColumnFamilyName() {
        return CF_NAME;
    }

    @Override
    public Logger getLog() {
        return LOG;
    }

    @Override
    public Set<Long> loadAllKeys() {
        com.datastax.driver.core.ResultSet resultSet = execute(getSelectColumnsQuery(KEY), "loadAllKeys");

        return StreamSupport.stream(resultSet.spliterator(), false)
                .map(row -> row.getLong(KEY))
                .collect(toSet());
    }

    @Override
    public void store(Long key, SocketClientsStats socketClientCount) {
        ByteBuffer byteBuffer = TABLE.serializeWithClassToBytes(socketClientCount);
        String json = TABLE.serializeToJson(socketClientCount);
        try {
            insert(key, new HashMap<String, Object>() {{ put(SERIALIZED_COLUMN_NAME, byteBuffer); put(JSON_COLUMN_NAME, json); }});
        } finally {
            releaseBuffer(byteBuffer);
        }
    }

    @Override
    public void storeAll(Map<Long, SocketClientsStats> map) {
        for (Entry<Long, SocketClientsStats> socketClientCount : map.entrySet()) {
            store(socketClientCount.getKey(), socketClientCount.getValue());
        }
    }

    @Override
    public void deleteAll(Collection<Long> keys) {
        for (Long key : keys) {
            delete(key);
        }
    }

    @Override
    public SocketClientsStats load(Long key) {
        String json = getJson(key);
        SocketClientsStats stats = TABLE.deserializeFromJson(json, SocketClientsStats.class);
        if (stats == null) {
            ByteBuffer bytes = get(key, SERIALIZED_COLUMN_NAME);
            stats = TABLE.deserializeWithClassFrom(bytes);
        }
        return stats;
    }

    public Iterable<SocketClientsStats> loadAll() {
        List<SocketClientsStats> result = new ArrayList<>();
        Iterator<com.datastax.driver.core.Row> it = getAll();
        while (it.hasNext()) {
            com.datastax.driver.core.Row row = it.next();
            SocketClientsStats socketClientInfo = TABLE.deserializeFromJson(row.getString(JSON_COLUMN_NAME), SocketClientsStats.class);
            if (socketClientInfo == null) {
                socketClientInfo = TABLE.deserializeWithClassFrom(row.getBytes(SERIALIZED_COLUMN_NAME));
            }
            result.add(socketClientInfo);
        }
        return result;
    }

    @Override
    public Map<Long, SocketClientsStats> loadAll(Collection<Long> keys) {
        HashMap<Long, SocketClientsStats> result = new HashMap<>(keys.size());
        for (Long key : keys) {
            SocketClientsStats stats = load(key);
            if (stats != null) {
                result.put(key, stats);
            }
        }
        return result;
    }

    @Override
    public void delete(Long key) {
        deleteWithCheck(key);
    }
}
