package com.betsoft.casino.mp.data.persister;

import com.betsoft.casino.mp.model.RoomTemplate;
import com.datastax.driver.core.DataType;
import com.datastax.driver.core.ResultSet;
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

/**
 * User: flsh
 * Date: 06.04.18.
 */
public class RoomTemplatePersister extends AbstractCassandraPersister<Long, String> implements MapStore<Long, RoomTemplate> {
    private static final Logger LOG = LogManager.getLogger(RoomTemplatePersister.class);
    private static final String CF_NAME = "RoomTemplate";

    private static final TableDefinition TABLE = new TableDefinition(CF_NAME,
            Arrays.asList(
                    new ColumnDefinition(KEY, DataType.bigint(), false, false, true),
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
    public void store(Long key, RoomTemplate template) {
        persist(template);
    }

    @Override
    public void storeAll(Map<Long, RoomTemplate> map) {
        for (RoomTemplate template : map.values()) {
            persist(template);
        }
    }

    @Override
    public void delete(Long key) {
        LOG.debug("delete: {}", key);
        deleteWithCheck(key);
    }

    @Override
    public void deleteAll(Collection<Long> keys) {
        for (Long key : keys) {
            delete(key);
        }
    }

    @Override
    public RoomTemplate load(Long key) {
        RoomTemplate template = get(key);
        if(template == null) {
            LOG.debug("load: template not found, id={}", key);
        }
        return template;
    }

    @Override
    public Map<Long, RoomTemplate> loadAll(Collection<Long> keys) {
        HashMap<Long, RoomTemplate> result = new HashMap<>(keys.size());
        for (Long key : keys) {
            RoomTemplate config = load(key);
            if(config != null) {
                result.put(key, config);
            }
        }
        return result;
    }

    @Override
    public Set<Long> loadAllKeys() {
        ResultSet resultSet = execute(getSelectColumnsQuery(KEY), "loadAllKeys");
        return StreamSupport.stream(resultSet.spliterator(), false)
                .map(row -> row.getLong(KEY))
                .collect(toSet());
    }

    public void persist(RoomTemplate template) {
        LOG.debug("persist, id={}", template.getId());
        ByteBuffer byteBuffer = TABLE.serializeWithClassToBytes(template);
        String json = TABLE.serializeWithClassToJson(template);
        try {
            insert(template.getId(), new HashMap<String, Object>() {{ put(SERIALIZED_COLUMN_NAME, byteBuffer); put(JSON_COLUMN_NAME, json); }});
        } finally {
            releaseBuffer(byteBuffer);
        }
    }

    public RoomTemplate get(Long id) {
        String json = getJson(id);
        RoomTemplate template = TABLE.deserializeWithClassFromJson(json);

        if (template == null) {
            ByteBuffer bytes = get(id, SERIALIZED_COLUMN_NAME);
            template = TABLE.deserializeWithClassFrom(bytes);
        }
        if (template == null) {
            LOG.debug("get: template not found, id={}", id);
        }
        return template;
    }

    public Collection<RoomTemplate> getAllTemplates() {
        List<RoomTemplate> result = new ArrayList<>();
        Iterator<Row> it = getAll();
        while (it.hasNext()) {
            Row row = it.next();
            RoomTemplate config = TABLE.deserializeWithClassFromJson(row.getString(JSON_COLUMN_NAME));
            if (config == null) {
                config = TABLE.deserializeWithClassFrom(row.getBytes(SERIALIZED_COLUMN_NAME));
            }
            result.add(config);
        }
        return result;
    }
}
