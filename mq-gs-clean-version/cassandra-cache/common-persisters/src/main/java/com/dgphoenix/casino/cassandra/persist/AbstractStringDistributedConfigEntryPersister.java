package com.dgphoenix.casino.cassandra.persist;

import com.datastax.driver.core.DataType;
import com.dgphoenix.casino.cassandra.persist.engine.ColumnDefinition;
import com.dgphoenix.casino.cassandra.persist.engine.TableDefinition;
import com.dgphoenix.casino.common.cache.data.IDistributedConfigEntry;

import java.util.Arrays;

/**
 * User: flsh
 * Date: 4/11/12
 */
public abstract class AbstractStringDistributedConfigEntryPersister<T extends IDistributedConfigEntry>
        extends AbstractDistributedConfigEntryPersister<String, T> {
    private final TableDefinition TABLE = new TableDefinition(getMainColumnFamilyName(),
            Arrays.asList(
                    new ColumnDefinition(KEY, DataType.text(), false, false, true),
                    new ColumnDefinition(SERIALIZED_COLUMN_NAME, DataType.blob()),
                    new ColumnDefinition(JSON_COLUMN_NAME, DataType.text())
            ),
            KEY);

    @Override
    protected TableDefinition _getTableDefinition() {
        return TABLE;
    }
}
