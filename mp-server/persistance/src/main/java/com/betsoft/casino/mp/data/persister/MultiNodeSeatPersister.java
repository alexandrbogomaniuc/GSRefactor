package com.betsoft.casino.mp.data.persister;

import com.betsoft.casino.mp.model.IMultiNodeSeat;
import com.betsoft.casino.mp.service.IMultiNodeSeatService;
import com.datastax.driver.core.DataType;
import com.dgphoenix.casino.cassandra.persist.engine.AbstractCassandraPersister;
import com.dgphoenix.casino.cassandra.persist.engine.ColumnDefinition;
import com.dgphoenix.casino.cassandra.persist.engine.TableDefinition;
import com.hazelcast.core.MapStore;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.toSet;

/**
 * User: flsh
 * Date: 30.09.18.
 */
@SuppressWarnings("rawtypes")
public class MultiNodeSeatPersister extends AbstractCassandraPersister<String, String> implements MapStore<String, IMultiNodeSeat> {
    private static final Logger LOG = LogManager.getLogger(MultiNodeSeatPersister.class);
    private static final String CF_NAME = "MultiNodeSeats";

    //key is accountId
    private static final TableDefinition TABLE = new TableDefinition(CF_NAME,
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
    public Logger getLog() {
        return LOG;
    }

    @Override
    public void store(String key, IMultiNodeSeat seat) {
        persist(seat);
    }

    @Override
    public void storeAll(Map<String, IMultiNodeSeat> map) {
        for (IMultiNodeSeat seat : map.values()) {
            persist(seat);
        }
    }

    @Override
    public void delete(String key) {
        deleteWithCheck(key);
    }

    @Override
    public void deleteAll(Collection<String> keys) {
        for (String key : keys) {
            delete(key);
        }
    }

    @Override
    public IMultiNodeSeat load(String key) {
        return get(key);
    }

    @Override
    public Map<String, IMultiNodeSeat> loadAll(Collection<String> keys) {
        HashMap<String, IMultiNodeSeat> result = new HashMap<>(keys.size());
        for (String key : keys) {
            IMultiNodeSeat config = load(key);
            if (config != null) {
                result.put(key, config);
            }
        }
        return result;
    }

    @Override
    public Iterable<String> loadAllKeys() {
        com.datastax.driver.core.ResultSet resultSet = execute(getSelectColumnsQuery(KEY), "loadAllKeys");
        return StreamSupport.stream(resultSet.spliterator(), false)
                .map(row -> row.getString(KEY))
                .collect(toSet());
    }

    public void persist(IMultiNodeSeat seat) {
        ByteBuffer buffer = TABLE.serializeWithClassToBytes(seat);
        String json = TABLE.serializeWithClassToJson(seat);;
        try {
            execute(getInsertQuery()
                            .value(KEY, getKey(seat))
                            .value(SERIALIZED_COLUMN_NAME, buffer)
                            .value(JSON_COLUMN_NAME, json),
                    "persist");
        } finally {
            releaseBuffer(buffer);
        }
    }

    public IMultiNodeSeat get(String id) {
        String json = getJson(id);
        IMultiNodeSeat seat = TABLE.deserializeWithClassFromJson(json);

        if (seat == null) {
            ByteBuffer bytes = get(id, SERIALIZED_COLUMN_NAME);
            seat = TABLE.deserializeWithClassFrom(bytes);
        }
        return seat;
    }

    private String getKey(IMultiNodeSeat seat) {
        return seat.getRoomId() + IMultiNodeSeatService.ID_DELIMITER + seat.getAccountId();
    }
}
