package com.abs.casino.cassandra.persist.engine;

import com.abs.casino.cassandra.persist.engine.configuration.Caching;
import com.abs.casino.cassandra.persist.engine.configuration.CompactionStrategy;
import com.abs.casino.cassandra.persist.engine.configuration.Compression;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.abs.casino.cassandra.persist.engine.ICassandraPersister.DEFAULT_GC_GRACE_PERIOD_IN_SECONDS;
import static com.abs.casino.cassandra.persist.engine.TableDefinition.INDEX_POSTFIX;
import static com.abs.casino.cassandra.persist.engine.TableDefinition.SEPARATOR;
import static com.abs.casino.cassandra.persist.engine.configuration.CompactionStrategy.SS_TABLE_SIZE;
import static org.junit.Assert.*;
import static com.abs.casino.cassandra.persist.engine.CassandraDataTypes.ascii;
import static com.abs.casino.cassandra.persist.engine.CassandraDataTypes.cboolean;
import static com.abs.casino.cassandra.persist.engine.CassandraDataTypes.cdouble;
import static com.abs.casino.cassandra.persist.engine.CassandraDataTypes.cint;
import static com.abs.casino.cassandra.persist.engine.CassandraDataTypes.counter;

/**
 * @author <a href="mailto:fateyev@dgphoenix.com">Anton Fateyev</a>
 * @since 23.05.16
 */
public class TableDefinitionTest {

    private static final String TABLE_NAME = "TEST_CF";
    private static final String COL_1 = "column_1";
    private static final String COL_2 = "column_2";
    private static final String COL_3 = "column_3";

    private static final String UNEXPECTED_TABLE = "Unexpected create table statement";

    @Test(expected = IllegalStateException.class)
    public void withoutPartitionKeyColumn() {
        TableDefinition tableDefinition = new TableDefinition(TABLE_NAME,
                Arrays.asList(
                        new ColumnDefinition(COL_1, cint()),
                        new ColumnDefinition(COL_2, cboolean())
                ));
        tableDefinition.getCreateTableStatement().getQueryString();
    }

    @Test
    public void simpleColumns() {
        TableDefinition tableDefinition = new TableDefinition(TABLE_NAME,
                Arrays.asList(
                        new ColumnDefinition(COL_1, cint()),
                        new ColumnDefinition(COL_2, cboolean())
                ), COL_1);
        String actualCreateTable = tableDefinition.getCreateTableStatement().getQueryString();

        String expectedCreateTable = com.datastax.driver.core.schemabuilder.SchemaBuilder.createTable(TABLE_NAME)
                .ifNotExists()
                .addPartitionKey(COL_1, cint())
                .addColumn(COL_2, cboolean())
                .withOptions()
                .compactionOptions(com.datastax.driver.core.schemabuilder.SchemaBuilder.sizedTieredStategy())
                .gcGraceSeconds(DEFAULT_GC_GRACE_PERIOD_IN_SECONDS)
                .compressionOptions(new com.datastax.driver.core.schemabuilder.CompressionOptions3(com.datastax.driver.core.schemabuilder.TableOptions.CompressionOptions.Algorithm.NONE))
                .getQueryString();

        assertEquals(UNEXPECTED_TABLE, expectedCreateTable, actualCreateTable);
        assertTrue("Indexes should be empty", tableDefinition.getCreateIndexStatements().isEmpty());
    }

    @Test
    public void indexes() {
        TableDefinition tableDefinition = new TableDefinition(TABLE_NAME,
                Arrays.asList(
                        new ColumnDefinition(COL_1, cint(), false, true, false),
                        new ColumnDefinition(COL_2, cboolean()),
                        new ColumnDefinition(COL_3, cdouble(), false, true, true)
                ), COL_1);
        tableDefinition.getCreateTableStatement().getQueryString();
        Map<String, com.datastax.driver.core.schemabuilder.SchemaStatement> actualIndexes = tableDefinition.getCreateIndexStatements();

        Map<String, com.datastax.driver.core.schemabuilder.SchemaStatement> expectedIndexes = new HashMap<>();
        com.datastax.driver.core.schemabuilder.SchemaStatement index1 = com.datastax.driver.core.schemabuilder.SchemaBuilder
                .createIndex(StringUtils.join(Arrays.asList(TABLE_NAME, COL_1, INDEX_POSTFIX), SEPARATOR))
                .ifNotExists()
                .onTable(TABLE_NAME)
                .andColumn(COL_1);
        expectedIndexes.put(COL_1, index1);
        com.datastax.driver.core.schemabuilder.SchemaStatement index2 = com.datastax.driver.core.schemabuilder.SchemaBuilder
                .createIndex(StringUtils.join(Arrays.asList(TABLE_NAME, COL_3, INDEX_POSTFIX), SEPARATOR))
                .ifNotExists()
                .onTable(TABLE_NAME)
                .andColumn(COL_3);
        expectedIndexes.put(COL_3, index2);

        assertEquals("Unexpected index count", expectedIndexes.size(), actualIndexes.size());
        expectedIndexes.forEach((columnName, expectedIndex) -> {
            assertEquals("Unexpected index statement", expectedIndex.getQueryString(),
                    actualIndexes.get(columnName).getQueryString());
        });
    }

    @Test(expected = IllegalStateException.class)
    public void indexOnCounter() {
        new TableDefinition(TABLE_NAME,
                Arrays.asList(
                        new ColumnDefinition(COL_1, cint()),
                        new ColumnDefinition(COL_2, counter(), false, true, false)
                ), COL_1);
    }

    @Test(expected = IllegalStateException.class)
    public void duplicateColumns() {
        new TableDefinition(TABLE_NAME,
                Arrays.asList(
                        new ColumnDefinition(COL_1, cint()),
                        new ColumnDefinition(COL_2, cboolean()),
                        new ColumnDefinition(COL_3, cdouble()),
                        new ColumnDefinition(COL_2, cboolean())
                ), COL_1);
    }

    @Test
    public void compositePrimaryKey() {
        TableDefinition tableDefinition = new TableDefinition(TABLE_NAME,
                Arrays.asList(
                        new ColumnDefinition(COL_1, cint()),
                        new ColumnDefinition(COL_2, ascii()),
                        new ColumnDefinition(COL_3, cboolean(), false, false, true)
                ), COL_1, COL_2);
        String actualCreateTable = tableDefinition.getCreateTableStatement().getQueryString();

        String expectedCreateTable = com.datastax.driver.core.schemabuilder.SchemaBuilder.createTable(TABLE_NAME)
                .ifNotExists()
                .addPartitionKey(COL_1, cint())
                .addPartitionKey(COL_2, ascii())
                .addClusteringColumn(COL_3, cboolean())
                .withOptions()
                .compactionOptions(com.datastax.driver.core.schemabuilder.SchemaBuilder.sizedTieredStategy())
                .gcGraceSeconds(DEFAULT_GC_GRACE_PERIOD_IN_SECONDS)
                .compressionOptions(new com.datastax.driver.core.schemabuilder.CompressionOptions3(com.datastax.driver.core.schemabuilder.TableOptions.CompressionOptions.Algorithm.NONE))
                .getQueryString();
        assertEquals(UNEXPECTED_TABLE, expectedCreateTable, actualCreateTable);
    }

    @Test
    public void staticColumn() {
        TableDefinition tableDefinition = new TableDefinition(TABLE_NAME,
                Arrays.asList(
                        new ColumnDefinition(COL_1, cint()),
                        new ColumnDefinition(COL_2, ascii(), true, false, false),
                        new ColumnDefinition(COL_3, ascii(), false, false, true)
                ), COL_1);
        String actualCreateTable = tableDefinition.getCreateTableStatement().getQueryString();

        String expectedCreateTable = com.datastax.driver.core.schemabuilder.SchemaBuilder.createTable(TABLE_NAME)
                .ifNotExists()
                .addPartitionKey(COL_1, cint())
                .addStaticColumn(COL_2, ascii())
                .addClusteringColumn(COL_3, ascii())
                .withOptions()
                .compactionOptions(com.datastax.driver.core.schemabuilder.SchemaBuilder.sizedTieredStategy())
                .gcGraceSeconds(DEFAULT_GC_GRACE_PERIOD_IN_SECONDS)
                .compressionOptions(new com.datastax.driver.core.schemabuilder.CompressionOptions3(com.datastax.driver.core.schemabuilder.TableOptions.CompressionOptions.Algorithm.NONE))
                .getQueryString();
        assertEquals(UNEXPECTED_TABLE, expectedCreateTable, actualCreateTable);
    }

    @Test(expected = IllegalStateException.class)
    public void staticColumnWithoutClusteringColumn() {
        TableDefinition tableDefinition = new TableDefinition(TABLE_NAME,
                Arrays.asList(
                        new ColumnDefinition(COL_1, cint()),
                        new ColumnDefinition(COL_2, ascii(), true, false, false),
                        new ColumnDefinition(COL_3, ascii())
                ), COL_1);
        tableDefinition.getCreateTableStatement().getQueryString();
    }

    @Test(expected = IllegalStateException.class)
    public void staticColumnWithIndex() {
        TableDefinition tableDefinition = new TableDefinition(TABLE_NAME,
                Arrays.asList(
                        new ColumnDefinition(COL_1, cint()),
                        new ColumnDefinition(COL_2, ascii(), true, true, false),
                        new ColumnDefinition(COL_3, ascii(), false, false, false)
                ), COL_1);
        tableDefinition.getCreateTableStatement().getQueryString();
    }

    @Test(expected = IllegalStateException.class)
    public void staticClusteringColumn() {
        TableDefinition tableDefinition = new TableDefinition(TABLE_NAME,
                Arrays.asList(
                        new ColumnDefinition(COL_1, cint()),
                        new ColumnDefinition(COL_2, ascii(), true, false, true),
                        new ColumnDefinition(COL_3, ascii(), false, false, false)
                ), COL_1);
        tableDefinition.getCreateTableStatement().getQueryString();
    }

    @Test
    public void cachingNone() {
        String actualCreateTable = createSimpleTableDefinition()
                .caching(Caching.NONE)
                .getCreateTableStatement()
                .getQueryString();

        String expectedCreateTable = createSimpleTableWithOptions()
                .compactionOptions(com.datastax.driver.core.schemabuilder.SchemaBuilder.sizedTieredStategy())
                .gcGraceSeconds(DEFAULT_GC_GRACE_PERIOD_IN_SECONDS)
                .compressionOptions(new com.datastax.driver.core.schemabuilder.CompressionOptions3(com.datastax.driver.core.schemabuilder.TableOptions.CompressionOptions.Algorithm.NONE))
                .caching(com.datastax.driver.core.schemabuilder.SchemaBuilder.KeyCaching.NONE, com.datastax.driver.core.schemabuilder.SchemaBuilder.noRows())
                .getQueryString();

        assertEquals(UNEXPECTED_TABLE, expectedCreateTable, actualCreateTable);
    }

    @Test
    public void cachingActualData() {
        String actualCreateTable = createSimpleTableDefinition()
                .caching(Caching.ACTUAL_DATA)
                .getCreateTableStatement()
                .getQueryString();

        String expectedCreateTable = createSimpleTableWithOptions()
                .compactionOptions(com.datastax.driver.core.schemabuilder.SchemaBuilder.sizedTieredStategy())
                .gcGraceSeconds(DEFAULT_GC_GRACE_PERIOD_IN_SECONDS)
                .caching(com.datastax.driver.core.schemabuilder.SchemaBuilder.KeyCaching.NONE, com.datastax.driver.core.schemabuilder.SchemaBuilder.rows(Caching.CACHING_ROW_NUMBER))
                .compressionOptions(new com.datastax.driver.core.schemabuilder.CompressionOptions3(com.datastax.driver.core.schemabuilder.TableOptions.CompressionOptions.Algorithm.NONE))
                .getQueryString();

        assertEquals(UNEXPECTED_TABLE, expectedCreateTable, actualCreateTable);
    }

    @Test
    public void compactionLeveled() {
        String actualCreateTable = createSimpleTableDefinition()
                .compaction(CompactionStrategy.LEVELED)
                .getCreateTableStatement()
                .getQueryString();

        String expectedCreateTable = createSimpleTableWithOptions()
                .gcGraceSeconds(DEFAULT_GC_GRACE_PERIOD_IN_SECONDS)
                .compressionOptions(new com.datastax.driver.core.schemabuilder.CompressionOptions3(com.datastax.driver.core.schemabuilder.TableOptions.CompressionOptions.Algorithm.NONE))
                .compactionOptions(com.datastax.driver.core.schemabuilder.SchemaBuilder.leveledStrategy().ssTableSizeInMB(SS_TABLE_SIZE))
                .getQueryString();

        assertEquals(UNEXPECTED_TABLE, expectedCreateTable, actualCreateTable);
    }

    @Test
    public void compactionLeveledWithCustomParams() {
        String actualCreateTable = createSimpleTableDefinition()
                .compaction(CompactionStrategy.getLeveled(true, TimeUnit.HOURS.toSeconds(5)))
                .getCreateTableStatement()
                .getQueryString();

        String expectedCreateTable = createSimpleTableWithOptions()
                .gcGraceSeconds(DEFAULT_GC_GRACE_PERIOD_IN_SECONDS)
                .compressionOptions(new com.datastax.driver.core.schemabuilder.CompressionOptions3(com.datastax.driver.core.schemabuilder.TableOptions.CompressionOptions.Algorithm.NONE))
                .compactionOptions(com.datastax.driver.core.schemabuilder.SchemaBuilder.leveledStrategy()
                        .ssTableSizeInMB(SS_TABLE_SIZE)
                        .uncheckedTombstoneCompaction(true)
                        .tombstoneCompactionIntervalInDay((int) TimeUnit.HOURS.toSeconds(5)))
                .getQueryString();

        assertEquals(UNEXPECTED_TABLE, expectedCreateTable, actualCreateTable);
    }

    @Test
    public void compactionSizeTired() {
        String actualCreateTable = createSimpleTableDefinition()
                .compaction(CompactionStrategy.SIZE_TIRED)
                .getCreateTableStatement()
                .getQueryString();

        String expectedCreateTable = createSimpleTableWithOptions()
                .gcGraceSeconds(DEFAULT_GC_GRACE_PERIOD_IN_SECONDS)
                .compressionOptions(new com.datastax.driver.core.schemabuilder.CompressionOptions3(com.datastax.driver.core.schemabuilder.TableOptions.CompressionOptions.Algorithm.NONE))
                .compactionOptions(com.datastax.driver.core.schemabuilder.SchemaBuilder.sizedTieredStategy())
                .getQueryString();

        assertEquals(UNEXPECTED_TABLE, expectedCreateTable, actualCreateTable);
    }

    @Test
    public void compactionSizeTiredWithCustomParams() {
        String actualCreateTable = createSimpleTableDefinition()
                .compaction(CompactionStrategy.getSizeTired(true, TimeUnit.HOURS.toSeconds(3), 0.5))
                .getCreateTableStatement()
                .getQueryString();

        String expectedCreateTable = createSimpleTableWithOptions()
                .gcGraceSeconds(DEFAULT_GC_GRACE_PERIOD_IN_SECONDS)
                .compressionOptions(new com.datastax.driver.core.schemabuilder.CompressionOptions3(com.datastax.driver.core.schemabuilder.TableOptions.CompressionOptions.Algorithm.NONE))
                .compactionOptions(com.datastax.driver.core.schemabuilder.SchemaBuilder.sizedTieredStategy()
                        .uncheckedTombstoneCompaction(true)
                        .tombstoneCompactionIntervalInDay((int) TimeUnit.HOURS.toSeconds(3)))
                .getQueryString();

        assertEquals(UNEXPECTED_TABLE, expectedCreateTable, actualCreateTable);
    }

    @Test
    public void compactionDateTired() {
        String actualCreateTable = createSimpleTableDefinition()
                .compaction(CompactionStrategy.DATE_TIRED)
                .getCreateTableStatement()
                .getQueryString();

        String expectedCreateTable = createSimpleTableWithOptions()
                .gcGraceSeconds(DEFAULT_GC_GRACE_PERIOD_IN_SECONDS)
                .compressionOptions(new com.datastax.driver.core.schemabuilder.CompressionOptions3(com.datastax.driver.core.schemabuilder.TableOptions.CompressionOptions.Algorithm.NONE))
                .compactionOptions(com.datastax.driver.core.schemabuilder.SchemaBuilder.dateTieredStrategy())
                .getQueryString();

        assertEquals(UNEXPECTED_TABLE, expectedCreateTable, actualCreateTable);
    }

    @Test
    public void gcGraceSeconds() {
        String actualCreateTable = createSimpleTableDefinition()
                .gcGraceSeconds(TimeUnit.DAYS.toSeconds(1))
                .getCreateTableStatement()
                .getQueryString();

        String expectedCreateTable = createSimpleTableWithOptions()
                .compressionOptions(new com.datastax.driver.core.schemabuilder.CompressionOptions3(com.datastax.driver.core.schemabuilder.TableOptions.CompressionOptions.Algorithm.NONE))
                .compactionOptions(com.datastax.driver.core.schemabuilder.SchemaBuilder.sizedTieredStategy())
                .gcGraceSeconds((int) TimeUnit.DAYS.toSeconds(1))
                .getQueryString();

        assertEquals(UNEXPECTED_TABLE, expectedCreateTable, actualCreateTable);
    }

    @Test
    public void compressionDeflate() {
        TableDefinition actualTableDefinition = createSimpleTableDefinition()
                .compression(Compression.DEFLATE);
        String actualCreateTable = actualTableDefinition.getCreateTableStatement().getQueryString();

        String expectedCreateTable = createSimpleTableWithOptions()
                .compactionOptions(com.datastax.driver.core.schemabuilder.SchemaBuilder.sizedTieredStategy())
                .gcGraceSeconds(DEFAULT_GC_GRACE_PERIOD_IN_SECONDS)
                .compressionOptions(new com.datastax.driver.core.schemabuilder.CompressionOptions3(com.datastax.driver.core.schemabuilder.TableOptions.CompressionOptions.Algorithm.DEFLATE))
                .getQueryString();

        assertEquals(UNEXPECTED_TABLE, expectedCreateTable, actualCreateTable);
        assertFalse("Client compression should be off", actualTableDefinition.isClientCompression());
    }

    @Test
    public void compressionClient() {
        TableDefinition actualTableDefinition = createSimpleTableDefinition()
                .compression(Compression.CLIENT);
        String actualCreateTable = actualTableDefinition.getCreateTableStatement().getQueryString();

        String expectedCreateTable = createSimpleTableWithOptions()
                .compactionOptions(com.datastax.driver.core.schemabuilder.SchemaBuilder.sizedTieredStategy())
                .gcGraceSeconds(DEFAULT_GC_GRACE_PERIOD_IN_SECONDS)
                .compressionOptions(new com.datastax.driver.core.schemabuilder.CompressionOptions3(com.datastax.driver.core.schemabuilder.TableOptions.CompressionOptions.Algorithm.NONE))
                .getQueryString();

        assertEquals(UNEXPECTED_TABLE, expectedCreateTable, actualCreateTable);
        assertTrue("Client compression should be on", actualTableDefinition.isClientCompression());
    }

    @Test(expected = IllegalArgumentException.class)
    public void clusteringOrderOnUnknownColumn() {
        createSimpleTableDefinition()
                .clusteringOrder(COL_2, com.datastax.driver.core.schemabuilder.SchemaBuilder.Direction.ASC)
                .getCreateTableStatement()
                .getQueryString();
    }

    @Test
    public void clusteringOrder() {
        String actualCreateTable = new TableDefinition(TABLE_NAME,
                Arrays.asList(
                        new ColumnDefinition(COL_1, cint()),
                        new ColumnDefinition(COL_2, ascii(), false, false, true)
                ), COL_1)
                .clusteringOrder(COL_2, com.datastax.driver.core.schemabuilder.SchemaBuilder.Direction.DESC)
                .getCreateTableStatement()
                .getQueryString();

        String expectedCreateTable = com.datastax.driver.core.schemabuilder.SchemaBuilder.createTable(TABLE_NAME).ifNotExists()
                .addPartitionKey(COL_1, cint())
                .addClusteringColumn(COL_2, ascii())
                .withOptions()
                .compactionOptions(com.datastax.driver.core.schemabuilder.SchemaBuilder.sizedTieredStategy())
                .gcGraceSeconds(DEFAULT_GC_GRACE_PERIOD_IN_SECONDS)
                .compressionOptions(new com.datastax.driver.core.schemabuilder.CompressionOptions3(com.datastax.driver.core.schemabuilder.TableOptions.CompressionOptions.Algorithm.NONE))
                .clusteringOrder(COL_2, com.datastax.driver.core.schemabuilder.SchemaBuilder.Direction.DESC)
                .getQueryString();

        assertEquals(UNEXPECTED_TABLE, expectedCreateTable, actualCreateTable);
    }

    @Test
    public void clusteringOrderOnTwoColumns() {
        String actualCreateTable = new TableDefinition(TABLE_NAME,
                Arrays.asList(
                        new ColumnDefinition(COL_1, cint()),
                        new ColumnDefinition(COL_2, ascii(), false, false, true),
                        new ColumnDefinition(COL_3, ascii(), false, false, true)
                ), COL_1)
                .clusteringOrder(COL_2, com.datastax.driver.core.schemabuilder.SchemaBuilder.Direction.DESC)
                .clusteringOrder(COL_3, com.datastax.driver.core.schemabuilder.SchemaBuilder.Direction.ASC)
                .getCreateTableStatement()
                .getQueryString();

        String expectedCreateTable = com.datastax.driver.core.schemabuilder.SchemaBuilder.createTable(TABLE_NAME).ifNotExists()
                .addPartitionKey(COL_1, cint())
                .addClusteringColumn(COL_2, ascii())
                .addClusteringColumn(COL_3, ascii())
                .withOptions()
                .compactionOptions(com.datastax.driver.core.schemabuilder.SchemaBuilder.sizedTieredStategy())
                .gcGraceSeconds(DEFAULT_GC_GRACE_PERIOD_IN_SECONDS)
                .compressionOptions(new com.datastax.driver.core.schemabuilder.CompressionOptions3(com.datastax.driver.core.schemabuilder.TableOptions.CompressionOptions.Algorithm.NONE))
                .clusteringOrder(COL_2, com.datastax.driver.core.schemabuilder.SchemaBuilder.Direction.DESC)
                .clusteringOrder(COL_3, com.datastax.driver.core.schemabuilder.SchemaBuilder.Direction.ASC)
                .getQueryString();

        assertEquals(UNEXPECTED_TABLE, expectedCreateTable, actualCreateTable);
    }

    @Test
    public void speculativeRetryAlways() {
        String actualCreateTable = createSimpleTableDefinition()
                .speculativeRetry(com.datastax.driver.core.schemabuilder.SchemaBuilder.always())
                .getCreateTableStatement()
                .getQueryString();

        String expectedCreateTable = createDefaultTable()
                .speculativeRetry(com.datastax.driver.core.schemabuilder.SchemaBuilder.always())
                .getQueryString();

        assertEquals(UNEXPECTED_TABLE, expectedCreateTable, actualCreateTable);
    }

    private TableDefinition createSimpleTableDefinition() {
        return new TableDefinition(TABLE_NAME,
                Arrays.asList(
                        new ColumnDefinition(COL_1, cint()),
                        new ColumnDefinition(COL_2, ascii())
                ), COL_1);
    }

    private com.datastax.driver.core.schemabuilder.Create.Options createDefaultTable() {
        return createSimpleTableWithOptions()
                .compactionOptions(com.datastax.driver.core.schemabuilder.SchemaBuilder.sizedTieredStategy())
                .gcGraceSeconds(DEFAULT_GC_GRACE_PERIOD_IN_SECONDS)
                .compressionOptions(new com.datastax.driver.core.schemabuilder.CompressionOptions3(com.datastax.driver.core.schemabuilder.TableOptions.CompressionOptions.Algorithm.NONE));
    }

    private com.datastax.driver.core.schemabuilder.Create.Options createSimpleTableWithOptions() {
        return com.datastax.driver.core.schemabuilder.SchemaBuilder.createTable(TABLE_NAME).ifNotExists()
                .addPartitionKey(COL_1, cint())
                .addColumn(COL_2, ascii())
                .withOptions();
    }
}
