package com.abs.casino.cassandra.persist;

import com.abs.casino.cassandra.persist.engine.ColumnDefinition;
import com.abs.casino.cassandra.persist.engine.TableDefinition;
import com.abs.casino.common.cache.data.IDistributedConfigEntry;

import java.util.Arrays;

import static com.abs.casino.cassandra.persist.engine.CassandraDataTypes.blob;
import static com.abs.casino.cassandra.persist.engine.CassandraDataTypes.text;

/**
 * User: flsh
 * Date: 4/11/12
 */
public abstract class AbstractStringDistributedConfigEntryPersister<T extends IDistributedConfigEntry>
        extends AbstractDistributedConfigEntryPersister<String, T> {
    private final TableDefinition TABLE = new TableDefinition(getMainColumnFamilyName(),
            Arrays.asList(
                    new ColumnDefinition(KEY, text(), false, false, true),
                    new ColumnDefinition(SERIALIZED_COLUMN_NAME, blob()),
                    new ColumnDefinition(JSON_COLUMN_NAME, text())
            ),
            KEY);

    @Override
    protected TableDefinition _getTableDefinition() {
        return TABLE;
    }
}
