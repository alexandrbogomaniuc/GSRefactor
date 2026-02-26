package com.betsoft.casino.mp.data.persister;

import com.betsoft.casino.mp.model.onlineplayer.OnlinePlayer;
import com.datastax.driver.core.DataType;
import com.datastax.driver.core.Row;
import com.dgphoenix.casino.cassandra.persist.engine.AbstractCassandraPersister;
import com.dgphoenix.casino.cassandra.persist.engine.ColumnDefinition;
import com.dgphoenix.casino.cassandra.persist.engine.TableDefinition;
import com.hazelcast.core.MapStore;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.toSet;

public class OnlinePlayerPersister extends AbstractCassandraPersister<String, String>
        implements MapStore<String, OnlinePlayer> {
    private static final Logger LOG = LogManager.getLogger(OnlinePlayerPersister.class);
    private static final String CF_NAME = "OnlinePlayer";

    private static final TableDefinition TABLE = new TableDefinition(
            CF_NAME,
            Arrays.asList(
                    new ColumnDefinition(KEY, DataType.text(), false, false, true),
                    new ColumnDefinition(SERIALIZED_COLUMN_NAME, DataType.blob()),
                    new ColumnDefinition(JSON_COLUMN_NAME, DataType.text())
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
    public void store(String key, OnlinePlayer onlinePlayer) {
        ByteBuffer byteBuffer = TABLE.serializeWithClassToBytes(onlinePlayer);
        String json = TABLE.serializeWithClassToJson(onlinePlayer);
        try {
            insert(onlinePlayer.getExternalId(), new HashMap<String, Object>() {{ put(SERIALIZED_COLUMN_NAME, byteBuffer); put(JSON_COLUMN_NAME, json); }});
        } finally {
            releaseBuffer(byteBuffer);
        }
    }

    @Override
    public void storeAll(Map<String, OnlinePlayer> map) {
        for (OnlinePlayer onlinePlayer : map.values()) {
            store(onlinePlayer.getExternalId(), onlinePlayer);
        }
    }

    @Override
    public void deleteAll(Collection<String> keys) {
        for (String key : keys) {
            delete(key);
        }
    }

    @Override
    public OnlinePlayer load(String externalId) {
        String json = getJson(externalId);
        OnlinePlayer op = TABLE.deserializeWithClassFromJson(json);

        if (op == null) {
            ByteBuffer bytes = get(externalId, SERIALIZED_COLUMN_NAME);
            op = TABLE.deserializeWithClassFrom(bytes);
        }
        return op;
    }

    public Iterable<OnlinePlayer> loadAll() {
        List<OnlinePlayer> result = new ArrayList<>();
        Iterator<Row> it = getAll();
        while (it.hasNext()) {
            Row row = it.next();
            OnlinePlayer onlinePlayer = TABLE.deserializeWithClassFromJson(row.getString(JSON_COLUMN_NAME));
            if (onlinePlayer == null) {
                onlinePlayer = TABLE.deserializeWithClassFrom(row.getBytes(SERIALIZED_COLUMN_NAME));
            }
            result.add(onlinePlayer);
        }
        return result;
    }

    @Override
    public Map<String, OnlinePlayer> loadAll(Collection<String> keys) {
        HashMap<String, OnlinePlayer> result = new HashMap<>(keys.size());
        for (String key : keys) {
            OnlinePlayer onlinePlayer = load(key);
            if (onlinePlayer != null) {
                result.put(key, onlinePlayer);
            }
        }
        return result;
    }

    @Override
    public void delete(String externalId) {
        deleteWithCheck(externalId);
    }
}
