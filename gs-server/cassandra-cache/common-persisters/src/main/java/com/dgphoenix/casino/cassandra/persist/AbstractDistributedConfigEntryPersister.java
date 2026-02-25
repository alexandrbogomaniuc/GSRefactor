package com.dgphoenix.casino.cassandra.persist;

import com.datastax.driver.core.ColumnDefinitions;
import com.datastax.driver.core.DataType;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.querybuilder.Batch;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.dgphoenix.casino.cassandra.persist.engine.AbstractCassandraPersister;
import com.dgphoenix.casino.cassandra.persist.engine.TableDefinition;
import com.dgphoenix.casino.cassandra.persist.engine.configuration.Caching;
import com.dgphoenix.casino.common.cache.AbstractDistributedCache;
import com.dgphoenix.casino.common.cache.ILoadingCache;
import com.dgphoenix.casino.common.cache.data.IDistributedConfigEntry;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.web.statistics.StatisticsManager;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: Grien
 * Date: 01.09.2014 18:49
 */
public abstract class AbstractDistributedConfigEntryPersister<KEY, T extends IDistributedConfigEntry>
        extends AbstractCassandraPersister<KEY, String> {

    public abstract int loadAll();

    @Override
    public TableDefinition getMainTableDefinition() {
        return _getTableDefinition().caching(Caching.NONE);
    }

    protected abstract TableDefinition _getTableDefinition();

    public void saveAll() {
        Map<? extends Object, ? extends T> objects = getCache().getAllObjects();
        Batch batch = QueryBuilder.batch();
        List<ByteBuffer> list = new ArrayList<>(objects.size());
        try {
            for (Map.Entry<? extends Object, ? extends T> entry : objects.entrySet()) {
                ByteBuffer byteBuffer = getMainTableDefinition().serializeToBytes(entry.getValue());
                String json = getMainTableDefinition().serializeToJson(entry.getValue());
                list.add(byteBuffer);
                batch.add(addInsertion((KEY) entry.getKey(), SERIALIZED_COLUMN_NAME, byteBuffer)
                        .value(JSON_COLUMN_NAME, json));
            }
            execute(batch, "saveAll");
        } finally {
            for (ByteBuffer byteBuffer : list) {
                releaseBuffer(byteBuffer);
            }
        }
    }

    public abstract T get(String id);

    public abstract AbstractDistributedCache getCache();

    protected void put(T entity) {
        try {
            getCache().put(entity);
        } catch (CommonException e) {
            getLog().error("Cannot put entry: " + entity, e);
            throw new RuntimeException("Cannot put entry", e);
        }
    }

    public void refresh(String id) {
        if (ILoadingCache.class.isInstance(getCache())) {
            ((ILoadingCache) getCache()).invalidate(id);
        } else {
            final T cassandraEntity = get(id);
            if (cassandraEntity == null) {
                if (getLog().isDebugEnabled()) {
                    getLog().debug("refresh (remove): " + id);
                }
                getCache().remove(id);
            } else {
                final T cached = (T) getCache().getObject(id);
                if (cached == null) {
                    if (getLog().isDebugEnabled()) {
                        getLog().debug("refresh (put): " + cassandraEntity);
                    }
                    put(cassandraEntity);
                } else {
                    //merge
                    if (getLog().isDebugEnabled()) {
                        getLog().debug("refresh (copy): " + cassandraEntity);
                    }
                    cached.copy(cassandraEntity);
                }
            }
        }
    }

    public void persist(KEY id, T entry) {
        String json = getMainTableDefinition().serializeToJson(entry);
        ByteBuffer byteBuffer = getMainTableDefinition().serializeToBytes(entry);
        try {
            Map<String, Object> columnValues = new HashMap<String, Object>();
            columnValues.put(SERIALIZED_COLUMN_NAME, byteBuffer);
            columnValues.put(JSON_COLUMN_NAME, json);
            insert(id, columnValues);
        } finally {
            releaseBuffer(byteBuffer);
        }
    }

    public void remove(KEY id) {
        super.deleteWithCheck(id);
    }

    public void persistPrepared(Batch batch) {
        execute(batch, "persistPrepared");
    }

    protected Map<KEY, T> loadAllAsMap(Class<T> entryClass) {
        long now = System.currentTimeMillis();
        Statement query = QueryBuilder.select().all().from(getMainColumnFamilyName());
        ResultSet resultSet = execute(query, "loadAllAsMap");
        if (resultSet == null || !resultSet.iterator().hasNext()) {
            getLog().error("loadAllForLongKeysAsMapKryo: rowList is null or empty");
            return null;
        }
        Map<KEY, T> result = new HashMap<>(resultSet.getAvailableWithoutFetching());
        for (com.datastax.driver.core.Row row : resultSet) {
            if (row.isNull(KEY)) {
                getLog().error("Column KEY not found");
            }
            ColumnDefinitions columnDefinitions = row.getColumnDefinitions();
            DataType keyType = columnDefinitions.getType(KEY);
            KEY key = null;
            if (DataType.ascii().equals(keyType) || DataType.varchar().equals(keyType) ||
                    DataType.text().equals(keyType)) {
                key = (KEY) row.getString(KEY);
            } else if (DataType.cint().equals(keyType)) {
                Integer k = row.getInt(KEY);
                key = (KEY) k;
            } else if (DataType.bigint().equals(keyType)) {
                Long k = row.getLong(KEY);
                key = (KEY) k;
            }
            if (key == null) {
                getLog().error("Cannot cast KEY: illegal key type=" + keyType);
                continue;
            }
            String json = row.getString(JSON_COLUMN_NAME);
            ByteBuffer buffer = row.getBytes(SERIALIZED_COLUMN_NAME);
            T entry = getMainTableDefinition().deserializeFromJson(json, entryClass);
            if (entry == null) {
                entry = getMainTableDefinition().deserializeFrom(buffer, entryClass);
            }
            if (entry == null) {
                getLog().error("loadAllAsMap: JCN/SCN is null for key: " + key + ", entryClass=" + entryClass);
            } else {
                result.put(key, entry);
            }
        }
        StatisticsManager.getInstance().updateRequestStatistics(getMainColumnFamilyName() + " loadAllForLongKeysAsMapKryo",
                System.currentTimeMillis() - now);
        return result;
    }
}
