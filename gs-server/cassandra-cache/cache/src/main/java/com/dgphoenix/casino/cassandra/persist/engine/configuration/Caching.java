package com.abs.casino.cassandra.persist.engine.configuration;


import static com.abs.casino.cassandra.persist.engine.SchemaCql.noRows;
import static com.abs.casino.cassandra.persist.engine.SchemaCql.rows;
import com.abs.casino.cassandra.persist.engine.SchemaCql.KeyCaching;
import com.abs.casino.cassandra.persist.engine.SchemaCql;

/**
 * @author <a href="mailto:fateyev@dgphoenix.com">Anton Fateyev</a>
 * @since 25.05.16
 */
public class Caching {

    public static final int CACHING_ROW_NUMBER = 10000;

    public static final Caching NONE = new Caching(KeyCaching.NONE, noRows());
    public static final Caching ACTUAL_DATA = new Caching(KeyCaching.NONE, rows(CACHING_ROW_NUMBER));

    private final KeyCaching keysCache;
    private final com.datastax.driver.core.schemabuilder.TableOptions.CachingRowsPerPartition rowsCache;

    private Caching(KeyCaching keysCache, com.datastax.driver.core.schemabuilder.TableOptions.CachingRowsPerPartition rowsCache) {
        this.keysCache = keysCache;
        this.rowsCache = rowsCache;
    }

    public static Caching get(int cachingRowNumber) {
        return new Caching(KeyCaching.NONE, rows(cachingRowNumber));
    }

    public KeyCaching getKeysCache() {
        return keysCache;
    }

    public com.datastax.driver.core.schemabuilder.TableOptions.CachingRowsPerPartition getRowsCache() {
        return rowsCache;
    }
}
