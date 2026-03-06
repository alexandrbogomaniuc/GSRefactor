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
import static com.abs.casino.cassandra.persist.engine.CassandraDataTypes.text;

/**
 * User: flsh
 * Date: 20.11.17.
 */
public class BotServiceConfigPersister extends AbstractCassandraPersister<String, String> implements MapStore<String, String> {
    private static final Logger LOG = LogManager.getLogger(BotServiceConfigPersister.class);
    private static final String CF_NAME = "BotServiceConfig";
    private static final String VALUE = "value";

    private static final TableDefinition TABLE = new TableDefinition(CF_NAME,
            Arrays.asList(
                    new ColumnDefinition(KEY, text(), false, false, true),
                    new ColumnDefinition(VALUE, text(), false, true, false)
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

    public Set<String> loadAllKeys() {
        com.datastax.driver.core.ResultSet resultSet = execute(getSelectColumnsQuery(KEY), "loadAllKeys");

        return StreamSupport.stream(resultSet.spliterator(), false)
                .map(row -> row.getString(KEY))
                .collect(toSet());
    }

    @Override
    public void store(String key, String value) {
        insert(key, VALUE, value);
    }

    @Override
    public void storeAll(Map<String, String> map) {
        for (Map.Entry<String, String> entry : map.entrySet()) {
            store(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void deleteAll(Collection<String> keys) {
        for (String key : keys) {
            delete(key);
        }
    }

    @Override
    public String load(String key) {
        assertInitialized();
        com.datastax.driver.core.ResultSet rows = execute(getSelectColumnsQuery(VALUE)
                        .where(eq(getKeyColumnName(), key))
                        .limit(1),
                "get");
        com.datastax.driver.core.Row row = rows.one();
        String result = null;
        if (row != null) {
            result = row.getString(VALUE);
        }
        return result;
    }

    public Iterable<String> loadAll() {
        List<String> result = new ArrayList<>();
        Iterator<com.datastax.driver.core.Row> it = getAll();
        while (it.hasNext()) {
            com.datastax.driver.core.Row row = it.next();
            String value = row.getString(VALUE);
            result.add(value);
        }
        return result;
    }

    @Override
    public Map<String, String> loadAll(Collection<String> keys) {
        HashMap<String, String> result = new HashMap<>(keys.size());
        for (String key : keys) {
            String value = load(key);
            if (value != null) {
                result.put(key, value);
            }
        }
        return result;
    }

    @Override
    public void delete(String id) {
        deleteWithCheck(id);
    }
}
