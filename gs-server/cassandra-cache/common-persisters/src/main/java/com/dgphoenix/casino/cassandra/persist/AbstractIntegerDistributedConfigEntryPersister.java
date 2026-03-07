package com.abs.casino.cassandra.persist;

import com.abs.casino.cassandra.persist.AbstractDistributedConfigEntryPersister;
import com.abs.casino.cassandra.persist.engine.ColumnDefinition;
import com.abs.casino.cassandra.persist.engine.TableDefinition;
import com.abs.casino.common.cache.data.IDistributedConfigEntry;

import java.util.Arrays;

import static com.abs.casino.cassandra.persist.engine.CassandraDataTypes.blob;
import static com.abs.casino.cassandra.persist.engine.CassandraDataTypes.cint;
import static com.abs.casino.cassandra.persist.engine.CassandraDataTypes.text;

/**
 * User: flsh
 * Date: 4/11/12
 */
public abstract class AbstractIntegerDistributedConfigEntryPersister<T extends IDistributedConfigEntry>
        extends AbstractDistributedConfigEntryPersister<Integer, T> {
    private final TableDefinition TABLE = new TableDefinition(getMainColumnFamilyName(),
            Arrays.asList(
                    new ColumnDefinition(KEY, cint(), false, false, true),
                    new ColumnDefinition(SERIALIZED_COLUMN_NAME, blob()),
                    new ColumnDefinition(JSON_COLUMN_NAME, text())
            ),
            Arrays.asList(KEY));

    @Override
    protected TableDefinition _getTableDefinition() {
        return TABLE;
    }

    public T get(String id, Class<T> entryClass) {
        return super.get(Integer.valueOf(id), entryClass);
    }
}
