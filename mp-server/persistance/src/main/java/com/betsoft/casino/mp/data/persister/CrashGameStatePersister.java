package com.betsoft.casino.mp.data.persister;

import com.betsoft.casino.mp.common.SharedCrashGameState;
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
import static com.abs.casino.cassandra.persist.engine.CassandraDataTypes.bigint;
import static com.abs.casino.cassandra.persist.engine.CassandraDataTypes.blob;
import static com.abs.casino.cassandra.persist.engine.CassandraDataTypes.text;

/**
 * User: flsh
 * Date: 20.11.17.
 */
public class CrashGameStatePersister extends AbstractCassandraPersister<Long, String> implements MapStore<Long, SharedCrashGameState> {
    private static final Logger LOG = LogManager.getLogger(CrashGameStatePersister.class);
    private static final String CF_NAME = "SharedCrashGameState";
    private static final TableDefinition TABLE = new TableDefinition(CF_NAME,
            Arrays.asList(
                    new ColumnDefinition(KEY, bigint(), false, false, true),
                    new ColumnDefinition(SERIALIZED_COLUMN_NAME, blob()),
                    new ColumnDefinition(JSON_COLUMN_NAME, text())
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

    public Set<Long> loadAllKeys() {
        com.datastax.driver.core.ResultSet resultSet = execute(getSelectColumnsQuery(KEY), "loadAllKeys");
        return StreamSupport.stream(resultSet.spliterator(), false)
                .map(row -> row.getLong(KEY))
                .collect(toSet());
    }

    @Override
    public void store(Long key, SharedCrashGameState state) {
        ByteBuffer byteBuffer = TABLE.serializeWithClassToBytes(state);
        String json = TABLE.serializeWithClassToJson(state);
        try {
            insert(key, new HashMap<String, Object>() {{ put(SERIALIZED_COLUMN_NAME, byteBuffer); put(JSON_COLUMN_NAME, json); }});
        } finally {
            releaseBuffer(byteBuffer);
        }
    }

    @Override
    public void storeAll(Map<Long, SharedCrashGameState> map) {
        for (Map.Entry<Long, SharedCrashGameState> entry : map.entrySet()) {
            store(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void deleteAll(Collection<Long> keys) {
        for (Long key : keys) {
            delete(key);
        }
    }

    @Override
    public SharedCrashGameState load(Long id) {
        SharedCrashGameState state = TABLE.deserializeWithClassFromJson(getJson(id));
        if (state == null) {
            ByteBuffer bytes = get(id, SERIALIZED_COLUMN_NAME);
            state = TABLE.deserializeWithClassFrom(bytes);
        }
        return state;
    }

    @SuppressWarnings("unused")
    public Iterable<SharedCrashGameState> loadAll() {
        List<SharedCrashGameState> result = new ArrayList<>();
        Iterator<com.datastax.driver.core.Row> it = getAll();
        while (it.hasNext()) {
            com.datastax.driver.core.Row row = it.next();
            SharedCrashGameState state = TABLE.deserializeWithClassFromJson(row.getString(JSON_COLUMN_NAME));
            if (state == null) {
                state = TABLE.deserializeWithClassFrom(row.getBytes(SERIALIZED_COLUMN_NAME));
            }
            result.add(state);
        }
        return result;
    }

    @Override
    public Map<Long, SharedCrashGameState> loadAll(Collection<Long> keys) {
        HashMap<Long, SharedCrashGameState> result = new HashMap<>(keys.size());
        for (Long key : keys) {
            SharedCrashGameState state = load(key);
            if (state != null) {
                result.put(key, state);
            }
        }
        return result;
    }

    @Override
    public void delete(Long id) {
        deleteWithCheck(id);
    }
}
