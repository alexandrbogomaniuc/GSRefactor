package com.betsoft.casino.mp.data.persister;

import com.betsoft.casino.mp.service.ServerConfig;
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

/**
 * User: flsh
 * Date: 20.11.17.
 */
public class ServerConfigPersister extends AbstractCassandraPersister<Integer, String> implements MapStore<Integer, ServerConfig> {
    public static final String CONFIG_STORE = "serverConfigStore";
    private static final Logger LOG = LogManager.getLogger(ServerConfigPersister.class);
    private static final String CF_NAME = "ServerConfig";

    private static final TableDefinition TABLE = new TableDefinition(CF_NAME,
            Arrays.asList(
                    new ColumnDefinition(KEY, com.datastax.driver.core.DataType.cint(), false, false, true),
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

    public Set<Integer> loadAllKeys() {
        com.datastax.driver.core.ResultSet resultSet = execute(getSelectColumnsQuery(KEY), "loadAllKeys");
        return StreamSupport.stream(resultSet.spliterator(), false)
                .map(row -> row.getInt(KEY))
                .collect(toSet());
    }

    @Override
    public void store(Integer key, ServerConfig config) {
        ByteBuffer byteBuffer = TABLE.serializeWithClassToBytes(config);
        String json = TABLE.serializeWithClassToJson(config);
        try {
            insert(config.getServerId(), new HashMap<String, Object>() {{ put(SERIALIZED_COLUMN_NAME, byteBuffer); put(JSON_COLUMN_NAME, json); }});
        } finally {
            releaseBuffer(byteBuffer);
        }
    }

    @Override
    public void storeAll(Map<Integer, ServerConfig> map) {
        for (ServerConfig config : map.values()) {
            store(config.getServerId(), config);
        }
    }

    @Override
    public void deleteAll(Collection<Integer> keys) {
        for (Integer key : keys) {
            delete(key);
        }
    }

    @Override
    public ServerConfig load(Integer id) {
        String json = getJson(id);
        ServerConfig sc = TABLE.deserializeWithClassFromJson(json);
        if (sc == null) {
            ByteBuffer bytes = get(id, SERIALIZED_COLUMN_NAME);
            sc = TABLE.deserializeWithClassFrom(bytes);
        }
        return sc;
    }

    public Iterable<ServerConfig> loadAll() {
        List<ServerConfig> result = new ArrayList<>();
        Iterator<com.datastax.driver.core.Row> it = getAll();
        while (it.hasNext()) {
            com.datastax.driver.core.Row row = it.next();
            ServerConfig config = TABLE.deserializeWithClassFromJson(row.getString(JSON_COLUMN_NAME));
            if (config == null) {
                config = TABLE.deserializeWithClassFrom(row.getBytes(SERIALIZED_COLUMN_NAME));
            }
            result.add(config);
        }
        return result;
    }

    @Override
    public Map<Integer, ServerConfig> loadAll(Collection<Integer> keys) {
        HashMap<Integer, ServerConfig> result = new HashMap<>(keys.size());
        for (Integer key : keys) {
            ServerConfig config = load(key);
            if(config != null) {
                result.put(key, config);
            }
        }
        return result;
    }

    @Override
    public void delete(Integer id) {
        deleteWithCheck(id);
    }
}
