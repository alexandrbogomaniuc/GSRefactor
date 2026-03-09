package com.abs.casino.cassandra.persist.engine;

import com.datastax.driver.core.schemabuilder.Alter;
import com.datastax.driver.core.schemabuilder.Create;
import com.datastax.driver.core.schemabuilder.CreateIndex;
import com.datastax.driver.core.schemabuilder.CreateKeyspace;
import com.datastax.driver.core.schemabuilder.SchemaBuilder;
import com.datastax.driver.core.schemabuilder.TableOptions;

public final class SchemaCql {
    private SchemaCql() {}

    public static CreateKeyspace createKeyspace(String keyspace) { return SchemaBuilder.createKeyspace(keyspace); }
    public static Create createTable(String table) { return SchemaBuilder.createTable(table); }
    public static Create createTable(String keyspace, String table) { return SchemaBuilder.createTable(keyspace, table); }
    public static Alter alterTable(String table) { return SchemaBuilder.alterTable(table); }
    public static Alter alterTable(String keyspace, String table) { return SchemaBuilder.alterTable(keyspace, table); }
    public static CreateIndex createIndex(String index) { return SchemaBuilder.createIndex(index); }
    public static TableOptions.CompactionOptions.SizeTieredCompactionStrategyOptions sizedTieredStategy() { return SchemaBuilder.sizedTieredStategy(); }
    public static TableOptions.CompactionOptions.LeveledCompactionStrategyOptions leveledStrategy() { return SchemaBuilder.leveledStrategy(); }
    public static TableOptions.CompactionOptions.DateTieredCompactionStrategyOptions dateTieredStrategy() { return SchemaBuilder.dateTieredStrategy(); }
    public static TableOptions.SpeculativeRetryValue always() { return SchemaBuilder.always(); }
    public static TableOptions.CachingRowsPerPartition noRows() { return SchemaBuilder.noRows(); }
    public static TableOptions.CachingRowsPerPartition rows(int rows) { return SchemaBuilder.rows(rows); }

    public static final class Direction {
        public static final SchemaBuilder.Direction ASC = SchemaBuilder.Direction.ASC;
        public static final SchemaBuilder.Direction DESC = SchemaBuilder.Direction.DESC;
        private Direction() {}
    }

    public static final class KeyCaching {
        public static final SchemaBuilder.KeyCaching NONE = SchemaBuilder.KeyCaching.NONE;
        public static final SchemaBuilder.KeyCaching ALL = SchemaBuilder.KeyCaching.ALL;
        private KeyCaching() {}
    }
}
