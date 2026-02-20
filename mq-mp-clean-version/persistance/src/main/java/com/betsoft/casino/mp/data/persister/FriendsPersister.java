package com.betsoft.casino.mp.data.persister;

import com.betsoft.casino.mp.model.friends.Friends;
import com.datastax.driver.core.DataType;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
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

public class FriendsPersister extends AbstractCassandraPersister<String, String>
        implements MapStore<String, Friends> {
    private static final Logger LOG = LogManager.getLogger(FriendsPersister.class);
    private static final String CF_NAME = "Friends";

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
        Select query = QueryBuilder.select(KEY).from(getMainColumnFamilyName());
        ResultSet resultSet = execute(query, "loadAllKeys");

        return StreamSupport.stream(resultSet.spliterator(), false)
                .map(row -> row.getString(KEY))
                .collect(toSet());
    }

    @Override
    public void store(String key, Friends friends) {
        ByteBuffer byteBuffer = TABLE.serializeWithClassToBytes(friends);
        String json = TABLE.serializeWithClassToJson(friends);
        try {
            insert(friends.getExternalId(), new HashMap<String, Object>() {{ put(SERIALIZED_COLUMN_NAME, byteBuffer); put(JSON_COLUMN_NAME, json); }});
        } finally {
            releaseBuffer(byteBuffer);
        }
    }

    @Override
    public void storeAll(Map<String, Friends> map) {
        for (Friends friends : map.values()) {
            store(friends.getExternalId(), friends);
        }
    }

    @Override
    public void deleteAll(Collection<String> keys) {
        for (String key : keys) {
            delete(key);
        }
    }

    @Override
    public Friends load(String externalId) {
        Friends f = TABLE.deserializeWithClassFromJson(getJson(externalId));

        if (f == null) {
            ByteBuffer bytes = get(externalId, SERIALIZED_COLUMN_NAME);
            f = TABLE.deserializeWithClassFrom(bytes);
        }
        return f;
    }

    public Iterable<Friends> loadAll() {
        List<Friends> result = new ArrayList<>();
        Iterator<Row> it = getAll();
        while (it.hasNext()) {
            Row row = it.next();
            Friends friends = TABLE.deserializeWithClassFromJson(row.getString(JSON_COLUMN_NAME));
            if (friends == null) {
                friends = TABLE.deserializeWithClassFrom(row.getBytes(SERIALIZED_COLUMN_NAME));
            }
            result.add(friends);
        }
        return result;
    }

    @Override
    public Map<String, Friends> loadAll(Collection<String> keys) {
        HashMap<String, Friends> result = new HashMap<>(keys.size());
        for (String key : keys) {
            Friends friends = load(key);
            if (friends != null) {
                result.put(key, friends);
            }
        }
        return result;
    }

    @Override
    public void delete(String privateRoomId) {
        deleteWithCheck(privateRoomId);
    }
}
