package com.betsoft.casino.mp.data.persister;

import com.betsoft.casino.mp.model.bots.BotConfigInfo;
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

/**
 * User: flsh
 * Date: 20.11.17.
 */
public class BotConfigInfoPersister extends AbstractCassandraPersister<Long, String> implements MapStore<Long, BotConfigInfo> {
    private static final Logger LOG = LogManager.getLogger(BotConfigInfoPersister.class);
    private static final String CF_NAME = "BotConfigInfo";
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
    public void store(Long key, BotConfigInfo config) {
        String json = TABLE.serializeWithClassToJson(config);
        ByteBuffer byteBuffer = TABLE.serializeWithClassToBytes(config);
        try {
            insert(config.getId(), new HashMap<String, Object>() {{ put(SERIALIZED_COLUMN_NAME, byteBuffer); put(JSON_COLUMN_NAME, json); }});
        } finally {
            releaseBuffer(byteBuffer);
        }
    }

    @Override
    public void storeAll(Map<Long, BotConfigInfo> map) {
        for (BotConfigInfo config : map.values()) {
            store(config.getId(), config);
        }
    }

    @Override
    public void deleteAll(Collection<Long> keys) {
        for (Long key : keys) {
            delete(key);
        }
    }

    @Override
    public BotConfigInfo load(Long id) {
        String json = getJson(id);
        BotConfigInfo info = TABLE.deserializeWithClassFromJson(json);

        if (info == null) {
            ByteBuffer bytes = get(id, SERIALIZED_COLUMN_NAME);
            info = TABLE.deserializeWithClassFrom(bytes);
        }
        return info;
    }

    public Iterable<BotConfigInfo> loadAll() {
        List<BotConfigInfo> result = new ArrayList<>();
        Iterator<com.datastax.driver.core.Row> it = getAll();
        while (it.hasNext()) {
            com.datastax.driver.core.Row row = it.next();
            BotConfigInfo config = TABLE.deserializeWithClassFromJson(row.getString(JSON_COLUMN_NAME));
            if (config == null) {
                config = TABLE.deserializeWithClassFrom(row.getBytes(SERIALIZED_COLUMN_NAME));
            }
            result.add(config);
        }
        return result;
    }

    @Override
    public Map<Long, BotConfigInfo> loadAll(Collection<Long> keys) {
        HashMap<Long, BotConfigInfo> result = new HashMap<>(keys.size());
        for (Long key : keys) {
            BotConfigInfo config = load(key);
            if (config != null) {
                result.put(key, config);
            }
        }
        return result;
    }

    @Override
    public void delete(Long id) {
        deleteWithCheck(id);
    }
}
