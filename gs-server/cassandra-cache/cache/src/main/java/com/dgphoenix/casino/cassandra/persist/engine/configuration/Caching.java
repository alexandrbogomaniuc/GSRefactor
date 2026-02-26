package com.dgphoenix.casino.cassandra.persist.engine.configuration;


import static com.datastax.driver.core.schemabuilder.SchemaBuilder.noRows;
import static com.datastax.driver.core.schemabuilder.SchemaBuilder.rows;

/**
 * @author <a href="mailto:fateyev@dgphoenix.com">Anton Fateyev</a>
 * @since 25.05.16
 */
public class Caching {

    public static final int CACHING_ROW_NUMBER = 10000;

    public static final Caching NONE = new Caching(com.datastax.driver.core.schemabuilder.SchemaBuilder.KeyCaching.NONE, noRows());
    public static final Caching ACTUAL_DATA = new Caching(com.datastax.driver.core.schemabuilder.SchemaBuilder.KeyCaching.NONE, rows(CACHING_ROW_NUMBER));

    private final com.datastax.driver.core.schemabuilder.SchemaBuilder.KeyCaching keysCache;
    private final com.datastax.driver.core.schemabuilder.TableOptions.CachingRowsPerPartition rowsCache;

    private Caching(com.datastax.driver.core.schemabuilder.SchemaBuilder.KeyCaching keysCache, com.datastax.driver.core.schemabuilder.TableOptions.CachingRowsPerPartition rowsCache) {
        this.keysCache = keysCache;
        this.rowsCache = rowsCache;
    }

    public static Caching get(int cachingRowNumber) {
        return new Caching(com.datastax.driver.core.schemabuilder.SchemaBuilder.KeyCaching.NONE, rows(cachingRowNumber));
    }

    public com.datastax.driver.core.schemabuilder.SchemaBuilder.KeyCaching getKeysCache() {
        return keysCache;
    }

    public com.datastax.driver.core.schemabuilder.TableOptions.CachingRowsPerPartition getRowsCache() {
        return rowsCache;
    }
}
