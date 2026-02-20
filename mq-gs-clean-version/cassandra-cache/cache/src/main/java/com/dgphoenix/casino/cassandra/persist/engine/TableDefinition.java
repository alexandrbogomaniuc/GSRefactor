package com.dgphoenix.casino.cassandra.persist.engine;

import com.datastax.driver.core.DataType;
import com.datastax.driver.core.schemabuilder.Create;
import com.datastax.driver.core.schemabuilder.SchemaBuilder;
import com.datastax.driver.core.schemabuilder.SchemaBuilder.Direction;
import com.datastax.driver.core.schemabuilder.SchemaStatement;
import com.datastax.driver.core.schemabuilder.TableOptions.SpeculativeRetryValue;
import com.dgphoenix.casino.cassandra.persist.engine.configuration.Caching;
import com.dgphoenix.casino.cassandra.persist.engine.configuration.CompactionStrategy;
import com.dgphoenix.casino.cassandra.persist.engine.configuration.Compression;
import com.dgphoenix.casino.common.util.CollectionUtils;
import com.dgphoenix.casino.common.util.FastKryoHelper;
import com.dgphoenix.casino.common.util.JsonHelper;
import com.dgphoenix.casino.common.util.LZ4Compressor;
import com.dgphoenix.casino.common.web.statistics.StatisticsManager;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.ByteBufferInput;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.util.UnsafeUtil;

import javax.annotation.Nonnull;

import java.nio.ByteBuffer;
import java.util.*;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang.StringUtils.join;

/**
 * User: flsh
 * Date: 25.09.14.
 *
 * @author <a href="mailto:fateyev@dgphoenix.com">Anton Fateyev</a>
 * @since 25.05.16
 */
public class TableDefinition {
    static final String INDEX_POSTFIX = "idx";
    static final String SEPARATOR = "_";
    private static final String DUPLICATE_COLUMN_ERROR = "Duplicate column found: %s";

    private final String tableName;
    private final List<ColumnDefinition> columns;
    private final Set<ColumnDefinition> addedColumns = new HashSet<>();
    private Compression compression;

    private SchemaStatement tableStatement;
    private final Map<String, SchemaStatement> indexesStatements;

    public TableDefinition(String tableName, List<ColumnDefinition> columns, List<String> primaryKeyParts) {
        this(tableName, columns, CollectionUtils.isEmpty(primaryKeyParts) ? new String[0] : new String[]{primaryKeyParts.get(0)});
    }

    public TableDefinition(String tableName, List<ColumnDefinition> columns, String... partitionKey) {
        this.tableName = tableName.trim();
        this.columns = columns;
        Create createStatement = SchemaBuilder.createTable(this.tableName).ifNotExists();
        addPartitionKeyColumns(partitionKey, this.columns, createStatement);
        addColumns(columns, Arrays.asList(partitionKey), createStatement);
        indexesStatements = generateIndexes(columns);
        tableStatement = createStatement;
        //Set default options
        compaction(CompactionStrategy.SIZE_TIRED);
        gcGraceSeconds(ICassandraPersister.DEFAULT_GC_GRACE_PERIOD_IN_SECONDS);
        compression(Compression.NONE);
    }

    private void addPartitionKeyColumns(String[] partitionKey, List<ColumnDefinition> columns, Create createStatement) {
        for (String partitionKeyColumnName : partitionKey) {
            ColumnDefinition partitionKeyColumn = columns.stream()
                    .filter(columnDefinition -> columnDefinition.getName().equals(partitionKeyColumnName))
                    .findFirst()
                    .get();
            checkState(addedColumns.add(partitionKeyColumn), DUPLICATE_COLUMN_ERROR, partitionKeyColumn.getName());
            createStatement.addPartitionKey(partitionKeyColumn.getName(), partitionKeyColumn.getType());
        }
    }

    private void addColumns(List<ColumnDefinition> columns, List<String> skipColumns, Create createStatement) {
        for (ColumnDefinition column : columns) {
            String columnName = column.getName();
            if (!skipColumns.contains(columnName)) {
                checkState(addedColumns.add(column), DUPLICATE_COLUMN_ERROR, column.getName());
                DataType columnType = column.getType();
                if (column.isPrimaryKeyPart()) {
                    createStatement.addClusteringColumn(columnName, columnType);
                } else if (column.isStaticField()) {
                    createStatement.addStaticColumn(columnName, columnType);
                } else {
                    createStatement.addColumn(columnName, columnType);
                }
            }
        }
    }

    private Map<String, SchemaStatement> generateIndexes(List<ColumnDefinition> columns) {
        return columns.stream()
                .filter(ColumnDefinition::isIndexed)
                .collect(toMap(ColumnDefinition::getName, column -> {
                    //http://www.datastax.com/documentation/cql/3.1/cql/cql_using/use_counter_t.html
                    //You cannot index, delete, or and re-adding a counter column
                    checkState(isNonCounterColumn(column), "Counter column cannot be indexed");
                    String columnName = column.getName();
                    return SchemaBuilder.createIndex(getIndexName(columnName)).ifNotExists()
                            .onTable(tableName)
                            .andColumn(columnName);
                }));
    }

    private boolean isNonCounterColumn(ColumnDefinition column) {
        return !column.getType().equals(DataType.counter());
    }

    public String getIndexName(String columnName) {
        return join(Arrays.asList(tableName, columnName, INDEX_POSTFIX), SEPARATOR);
    }

    private Create.Options getOptions() {
        if (tableStatement instanceof Create) {
            tableStatement = ((Create) tableStatement).withOptions();
        }
        return (Create.Options) tableStatement;
    }

    public TableDefinition caching(@Nonnull Caching caching) {
        checkNotNull(caching, "Caching must be not null");
        getOptions().caching(caching.getKeysCache(), caching.getRowsCache());
        return this;
    }

    public TableDefinition compaction(@Nonnull CompactionStrategy strategy) {
        checkNotNull(strategy, "Compaction strategy must be not null");
        getOptions().compactionOptions(strategy.getCompactionOptions());
        return this;
    }

    public TableDefinition gcGraceSeconds(long gcGraceSeconds) {
        getOptions().gcGraceSeconds((int) gcGraceSeconds);
        return this;
    }

    public TableDefinition compression(@Nonnull Compression compression) {
        checkNotNull(compression, "Compression must be not null");
        this.compression = compression;
        getOptions().compressionOptions(compression.getCompressionOptions());
        return this;
    }

    public TableDefinition clusteringOrder(String columnName, Direction direction) {
        getOptions().clusteringOrder(columnName, direction);
        return this;
    }

    public TableDefinition speculativeRetry(SpeculativeRetryValue speculativeRetryValue) {
        getOptions().speculativeRetry(speculativeRetryValue);
        return this;
    }

    public TableDefinition dcLocalReadRepairChance(double repairChance) {
        getOptions().dcLocalReadRepairChance(repairChance);
        return this;
    }

    public TableDefinition readRepairChance(double repairChance) {
        getOptions().readRepairChance(repairChance);
        return this;
    }

    public TableDefinition defaultTimeToLive(Integer defaultTimeToLive) {
        getOptions().defaultTimeToLive(defaultTimeToLive);
        return this;
    }

    public String getTableName() {
        return tableName;
    }

    public Set<ColumnDefinition> getColumns() {
        return addedColumns;
    }

    public SchemaStatement getCreateTableStatement() {
        return tableStatement;
    }

    public Map<String, SchemaStatement> getCreateIndexStatements() {
        return indexesStatements;
    }

    public void releaseBuffer(ByteBuffer buffer) {
        try {
            UnsafeUtil.releaseBuffer(buffer);
        } catch (Throwable ignore) {
        }
    }

    public boolean isClientCompression() {
        return Compression.CLIENT.equals(compression);
    }

    public ByteBuffer serializeToBytes(Object entity) {
        if (isClientCompression()) {
            ByteBuffer uncompressed = FastKryoHelper.serializeToBytes(entity);
            ByteBuffer compressed = null;
            try {
                compressed = LZ4Compressor.getInstance().compress(uncompressed);
            } finally {
                releaseBuffer(uncompressed);
            }
            StatisticsManager.getInstance().updateRequestStatistics("serializeToBytes " + getTableName() +
                    " uncompressed", uncompressed.limit());
            StatisticsManager.getInstance().updateRequestStatistics("serializeToBytes " + getTableName() +
                    " compressed", compressed.limit());
            return compressed;
        } else {
            return FastKryoHelper.serializeToBytes(entity);
        }
    }

    public String serializeToJson(Object entity) {
        return JsonHelper.getInstance().serializeToJson(entity);
    }

    public ByteBuffer serializeWithClassToBytes(Object entity) {
        if (isClientCompression()) {
            ByteBuffer uncompressed = FastKryoHelper.serializeWithClassToBytes(entity);
            ByteBuffer compressed = null;
            try {
                compressed = LZ4Compressor.getInstance().compress(uncompressed);
            } finally {
                releaseBuffer(uncompressed);
            }
            StatisticsManager.getInstance().updateRequestStatistics("serializeWithClassToBytes " + getTableName() +
                    " uncompressed", uncompressed.limit());
            StatisticsManager.getInstance().updateRequestStatistics("serializeWithClassToBytes " + getTableName() +
                    " compressed", compressed.limit());
            return compressed;

        } else {
            return FastKryoHelper.serializeWithClassToBytes(entity);
        }
    }

    public String serializeWithClassToJson(Object entity) {
        return JsonHelper.getInstance().serializeWithClassToJson(entity);
    }

    public <T> T deserializeFrom(ByteBuffer byteBuffer, Class<T> klazz) {
        if (byteBuffer == null) {
            return null;
        }
        if (isClientCompression()) {
            ByteBuffer uncompressed = LZ4Compressor.getInstance().uncompress(byteBuffer);
            StatisticsManager.getInstance().updateRequestStatistics("deserializeFrom " + getTableName() +
                    " compressed", byteBuffer.limit());
            StatisticsManager.getInstance().updateRequestStatistics("deserializeFrom " + getTableName() +
                    " uncompressed", uncompressed.limit());
            T result = FastKryoHelper.deserializeFrom(uncompressed, klazz);
            releaseBuffer(uncompressed);
            return result;
        } else {
            return FastKryoHelper.deserializeFrom(byteBuffer, klazz);
        }
    }

    public <T> T deserializeFromJson(String json, Class<T> klazz) {
        return JsonHelper.getInstance().deserializeFromJson(json, klazz);
    }

    public <T> T deserializeWithClassFrom(ByteBuffer byteBuffer) {
        if (isClientCompression()) {
            ByteBuffer uncompressed = LZ4Compressor.getInstance().uncompress(byteBuffer);
            StatisticsManager.getInstance().updateRequestStatistics("deserializeWithClassFrom " + getTableName() +
                    " compressed", byteBuffer.limit());
            StatisticsManager.getInstance().updateRequestStatistics("deserializeWithClassFrom " + getTableName() +
                    " uncompressed", uncompressed.limit());
            T result = FastKryoHelper.deserializeWithClassFrom(uncompressed);
            releaseBuffer(uncompressed);
            return result;
        } else {
            return FastKryoHelper.deserializeWithClassFrom(byteBuffer);
        }
    }

    public <T> T deserializeWithClassFromJson(String json) {
        return JsonHelper.getInstance().deserializeWithClassFromJson(json);
    }

    public <T extends KryoSerializable> List<T> deserializeToList(ByteBuffer inputBuffer, Serializer collectionSerializer) {
        long now = System.currentTimeMillis();
        try {
            if (FastKryoHelper.isEmpty(inputBuffer)) {
                return null;
            }
            ByteBuffer serialized;
            if (isClientCompression()) {
                serialized = LZ4Compressor.getInstance().uncompress(inputBuffer);
                StatisticsManager.getInstance().updateRequestStatistics("deserializeToList " + getTableName() +
                        " compressed", inputBuffer.limit());
                StatisticsManager.getInstance().updateRequestStatistics("deserializeToList " + getTableName() +
                        " uncompressed", serialized.limit());
            } else {
                serialized = inputBuffer;
            }
            Kryo kryo = null;
            try (Input input = new ByteBufferInput(serialized)) {
                kryo = FastKryoHelper.getKryo();
                return (ArrayList<T>) collectionSerializer.read(kryo, input, ArrayList.class);
            } finally {
                FastKryoHelper.release(kryo);
            }
        } finally {
            StatisticsManager.getInstance().updateRequestStatistics("FastKryoHelper deserializeToList",
                    System.currentTimeMillis() - now);
        }
    }

    public <T> List<T> deserializeToListJson(String json, Class<T> klazz) {
        return JsonHelper.getInstance().deserializeToListJson(json, klazz);
    }

    public <K, V> Map<K, V> deserializeToMapJson(String json, Class<K> keyClass, Class<V> valueClass) {
        return JsonHelper.getInstance().deserializeToMapJson(json, keyClass, valueClass);
    }

    public <T> String serializeToListJson(List<T> list, Class<T> klazz) {
        return JsonHelper.getInstance().serializeToListJson(list, klazz);
    }

    public <K, V> String serializeToMapJson(Map<K, V> map, Class<K> keyClass, Class<V> valueClass) {
        return JsonHelper.getInstance().serializeToMapJson(map, keyClass, valueClass);
    }
}
