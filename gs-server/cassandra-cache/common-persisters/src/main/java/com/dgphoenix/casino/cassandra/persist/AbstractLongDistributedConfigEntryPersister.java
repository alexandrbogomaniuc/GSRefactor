package com.dgphoenix.casino.cassandra.persist;

import com.dgphoenix.casino.cassandra.persist.engine.ColumnDefinition;
import com.dgphoenix.casino.cassandra.persist.engine.TableDefinition;
import com.dgphoenix.casino.common.cache.data.IDistributedConfigEntry;

import java.util.Arrays;

/**
 * User: flsh
 * Date: 4/11/12
 */
public abstract class AbstractLongDistributedConfigEntryPersister<T extends IDistributedConfigEntry>
        extends AbstractDistributedConfigEntryPersister<Long, T> {
    private final TableDefinition TABLE = new TableDefinition(getMainColumnFamilyName(),
            Arrays.asList(
                    new ColumnDefinition(KEY, com.datastax.driver.core.DataType.bigint(), false, false, true),
                    new ColumnDefinition(SERIALIZED_COLUMN_NAME, com.datastax.driver.core.DataType.blob()),
                    new ColumnDefinition(JSON_COLUMN_NAME, com.datastax.driver.core.DataType.text())
            ),
            Arrays.asList(KEY));

    @Override
    protected TableDefinition _getTableDefinition() {
        return TABLE;
    }

    public T get(String id, Class<T> entryClass) {
        return super.get(Long.valueOf(id), entryClass);
    }
}