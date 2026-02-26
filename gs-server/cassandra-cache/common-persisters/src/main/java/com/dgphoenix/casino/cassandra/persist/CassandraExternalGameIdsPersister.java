package com.dgphoenix.casino.cassandra.persist;

import com.dgphoenix.casino.cassandra.persist.engine.ColumnDefinition;
import com.dgphoenix.casino.cassandra.persist.engine.TableDefinition;
import com.dgphoenix.casino.common.cache.AbstractDistributedCache;
import com.dgphoenix.casino.common.cache.ExternalGameIdsCache;
import com.dgphoenix.casino.common.cache.data.IdObject;
import com.dgphoenix.casino.common.persist.StreamPersister;
import com.dgphoenix.casino.common.persist.TableProcessor;
import com.dgphoenix.casino.common.util.Pair;
import com.dgphoenix.casino.common.util.string.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by grien on 18.02.15.
 */
public class CassandraExternalGameIdsPersister extends AbstractStringDistributedConfigEntryPersister<IdObject>
        implements ILazyLoadingPersister<String, IdObject>, StreamPersister<String, IdObject> {
    public static final String CF = "ExtGameIds";
    public static final String ID = "ID";
    public static final String BANK_ID = "BANK";
    private static final TableDefinition TABLE = new TableDefinition(
            CF,
            Arrays.asList(
                    new ColumnDefinition(BANK_ID, com.datastax.driver.core.DataType.cint(), false, false, true),
                    new ColumnDefinition(KEY, com.datastax.driver.core.DataType.text(), false, false, true),
                    new ColumnDefinition(ID, com.datastax.driver.core.DataType.bigint())
            ),
            BANK_ID
    );

    private final Logger LOG = LogManager.getLogger(this.getClass());

    private CassandraExternalGameIdsPersister() {
    }

    @Override
    protected TableDefinition _getTableDefinition() {
        return TABLE;
    }

    @Override
    public Logger getLog() {
        return LOG;
    }

    public void saveIdObjects(Map<String, IdObject> map) {
        for (Map.Entry<String, IdObject> entry : map.entrySet()) {
            persist(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void refresh(String id) {
        if (StringUtils.isTrimmedEmpty(id)) {
            LOG.info("refresh: invalidate all entries");
            ExternalGameIdsCache.getInstance().invalidateAll();
        } else {
            super.refresh(id);
        }
    }

    @Override
    public int loadAll() {
        final Map<String, IdObject> ids = getAllAsMap();
        for (Map.Entry<String, IdObject> entry : ids.entrySet()) {
            ExternalGameIdsCache.getInstance().put(entry.getKey(), entry.getValue());
        }
        LOG.info("loadAll: count=" + ids.size());
        return ids.size();
    }

    @Override
    public IdObject get(String id) {
        Pair<String, Integer> pair = ExternalGameIdsCache.parseKey(id);
        if (pair == null) {
            LOG.error("Can't get. Incorrect key={}", id);
            return ExternalGameIdsCache.NONE;
        }
        return get(pair.getValue(), pair.getKey());
    }

    @Override
    public AbstractDistributedCache getCache() {
        return ExternalGameIdsCache.getInstance();
    }

    @Override
    public Map<String, IdObject> getAllAsMap() {
        Map<String, IdObject> result = new HashMap<>();
        com.datastax.driver.core.ResultSet resultSet = execute(getSelectColumnsQuery(BANK_ID, KEY, ID), "getAllAsMap");
        for (com.datastax.driver.core.Row row : resultSet) {
            if (row != null && !row.isNull(ID)) {
                int bankId = row.getInt(BANK_ID);
                String extId = row.getString(KEY);
                long value = row.getLong(ID);
                result.put(ExternalGameIdsCache.composeKey(extId, bankId), new IdObject(value));
            }
        }
        return result;
    }

    @Override
    public Map<String, IdObject> getAsMap(Integer bankId) {
        Map<String, IdObject> result = new HashMap<>();
        com.datastax.driver.core.ResultSet resultSet = execute(getSelectColumnsQuery(KEY, ID).where(eq(BANK_ID, bankId)), "getAllAsMap");
        for (com.datastax.driver.core.Row row : resultSet) {
            if (row != null && !row.isNull(ID)) {
                String extId = row.getString(KEY);
                long value = row.getLong(ID);
                result.put(ExternalGameIdsCache.composeKey(extId, bankId), new IdObject(value));
            }
        }
        return result;
    }

    @Override
    public void processAll(TableProcessor<Pair<String, IdObject>> tableProcessor) throws IOException {
        com.datastax.driver.core.ResultSet resultSet = execute(getSelectColumnsQuery(BANK_ID, KEY, ID), "getAllAsMap");
        for (com.datastax.driver.core.Row row : resultSet) {
            processRow(row, tableProcessor);
        }
    }

    @Override
    public void processByCondition(TableProcessor<Pair<String, IdObject>> tableProcessor, String conditionName, Object... conditionValues)
            throws IOException {
        if ("byBank".equals(conditionName)) {
            Long bankId = (Long) conditionValues[0];
            com.datastax.driver.core.Statement select = getSelectColumnsQuery(BANK_ID, KEY, ID)
                    .where(eq(BANK_ID, bankId));
            com.datastax.driver.core.ResultSet resultSet = execute(select, "getByBankId");
            for (com.datastax.driver.core.Row row : resultSet) {
                processRow(row, tableProcessor);
            }
        }
    }

    private void processRow(com.datastax.driver.core.Row row, TableProcessor<Pair<String, IdObject>> tableProcessor) throws IOException {
        if (row != null && !row.isNull(ID)) {
            String key = ExternalGameIdsCache.composeKey(row.getString(KEY), row.getInt(BANK_ID));
            IdObject value = new IdObject(row.getLong(ID));
            tableProcessor.process(new Pair<>(key, value));
        }
    }

    @Override
    public void saveAll() {
        Map<String, IdObject> allObjects = ExternalGameIdsCache.getInstance().getAllObjects();
        for (Map.Entry<String, IdObject> entry : allObjects.entrySet()) {
            persist(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void persist(String key, IdObject entry) {
        if (entry == ExternalGameIdsCache.NONE) {
            return;
        }
        Pair<String, Integer> pair = ExternalGameIdsCache.parseKey(key);
        if (pair == null) {
            LOG.error("Can't persist. Incorrect key={}", key);
            return;
        }
        execute(getInsertQuery().
                value(BANK_ID, pair.getValue()).
                value(KEY, pair.getKey()).
                value(ID, entry.getId()), "persist");
    }

    @Override
    public void delete(String key, IdObject idObject) {
        Pair<String, Integer> pair = ExternalGameIdsCache.parseKey(key);
        if (pair == null) {
            LOG.error("Can't delete. Incorrect key={}", key);
            return;
        }
        execute(
                addItemDeletion(eq(BANK_ID, pair.getValue()),
                        eq(KEY, pair.getKey())),
                "persist");
    }

    public IdObject get(int bankId, String extId) {
        com.datastax.driver.core.Row row = execute(
                getSelectColumnsQuery(ID).
                        where().
                        and(eq(BANK_ID, bankId)).
                        and(eq(KEY, extId)).
                        limit(1),
                "get").one();
        if (row == null || row.isNull(ID)) {
            return ExternalGameIdsCache.NONE;
        }
        return new IdObject(row.getLong(ID));
    }

    @Override
    protected Map<String, IdObject> loadAllAsMap(Class<IdObject> entryClass) {
        return getAllAsMap();
    }
}
