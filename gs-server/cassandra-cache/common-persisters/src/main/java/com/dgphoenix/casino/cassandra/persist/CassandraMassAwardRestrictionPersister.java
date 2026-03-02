package com.abs.casino.cassandra.persist;

import com.abs.casino.cassandra.persist.ICachePersister;
import com.abs.casino.cassandra.persist.engine.AbstractCassandraPersister;
import com.abs.casino.cassandra.persist.engine.ColumnDefinition;
import com.abs.casino.cassandra.persist.engine.TableDefinition;
import com.abs.casino.common.cache.data.bonus.restriction.MassAwardRestriction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class CassandraMassAwardRestrictionPersister extends AbstractCassandraPersister<Long, String>
        implements ICachePersister<Long, MassAwardRestriction> {

    private static final Logger LOG = LogManager.getLogger(CassandraMassAwardRestrictionPersister.class);
    private static final String CF_NAME = "MassAwardRestrictionCF";
    private static final TableDefinition TABLE = new TableDefinition(CF_NAME,
            Arrays.asList(
                    new ColumnDefinition(KEY, com.datastax.driver.core.DataType.bigint(), false, false, true),
                    new ColumnDefinition(SERIALIZED_COLUMN_NAME, com.datastax.driver.core.DataType.blob()),
                    new ColumnDefinition(JSON_COLUMN_NAME, com.datastax.driver.core.DataType.text())
            ), KEY);

    private CassandraMassAwardRestrictionPersister() {
        super();
    }

    @Override
    public MassAwardRestriction get(Long key) {
        String json = getJson(key);
        MassAwardRestriction obj = 
                getMainTableDefinition()
                .deserializeWithClassFromJson(json);

        if (obj == null) {
            ByteBuffer buffer = get(key, SERIALIZED_COLUMN_NAME);
            return getMainTableDefinition().deserializeWithClassFrom(buffer);
        }

        return obj;
    }

    @Override
    public void persist(MassAwardRestriction massAwardRestriction) {
        persist(massAwardRestriction.getId(), massAwardRestriction);
    }

    @Override
    public void persist(Long key, MassAwardRestriction massAwardRestriction) {
        String json = getMainTableDefinition().serializeWithClassToJson(massAwardRestriction);
        ByteBuffer byteBuffer = getMainTableDefinition().serializeWithClassToBytes(massAwardRestriction);
        try {
            Map<String, Object> values = new HashMap<String, Object>(){{
                put(SERIALIZED_COLUMN_NAME, byteBuffer);
                put(JSON_COLUMN_NAME, json);
            }};
            insert(key, values);
        } finally {
            releaseBuffer(byteBuffer);
        }
    }

    public boolean delete(Long key) {
        return super.deleteWithCheck(key);
    }

    @Override
    public TableDefinition getMainTableDefinition() {
        return TABLE;
    }

    @Override
    public Logger getLog() {
        return LOG;
    }
}
