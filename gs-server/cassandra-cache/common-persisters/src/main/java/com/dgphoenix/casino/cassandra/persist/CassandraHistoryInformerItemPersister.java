/*
 * CassandraHistoryInformerItemPersister
 *
 * Author: Stepan Vvedenskiy
 * Date: 11.01.2018
 */

package com.dgphoenix.casino.cassandra.persist;

import com.datastax.driver.core.DataType;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.querybuilder.*;
import com.datastax.driver.core.schemabuilder.SchemaBuilder;
import com.dgphoenix.casino.cassandra.CassandraPersistenceManager;
import com.dgphoenix.casino.cassandra.persist.engine.AbstractCassandraPersister;
import com.dgphoenix.casino.cassandra.persist.engine.ColumnDefinition;
import com.dgphoenix.casino.cassandra.persist.engine.TableDefinition;
import com.dgphoenix.casino.common.cache.data.HistoryInformerItem;
import com.dgphoenix.casino.common.persist.TableProcessor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

public class CassandraHistoryInformerItemPersister extends AbstractCassandraPersister<Long, String> {
    private static final Logger LOG = LogManager.getLogger(CassandraHistoryInformerItemPersister.class);
    private static final String HISTORY_INFORMER_ITEM_CF = "HistoryInformerItemCF";
    private static final String MAX_ITERATIONS_ITEM_CF = "HistoryInformerItemMaxIterationsCF";
    private static final String BANK_ID_FIELD = "BankId";
    private static final String CREATE_TIME_FIELD = "CreateTime";
    private static final String SESSION_ID_FIELD = "SessionId";
    private static final String ITERATIONS_FIELD = "Iterations";
    private static final String LAST_ATTEMPT_TIME_FIELD = "LastAttemptTime";

    //Main CF for HistoryInformerItem. New items goes here
    private static final TableDefinition TABLE = new TableDefinition(HISTORY_INFORMER_ITEM_CF,
            Arrays.asList(
                    new ColumnDefinition(BANK_ID_FIELD, DataType.bigint(), false, false, true),
                    new ColumnDefinition(CREATE_TIME_FIELD, DataType.bigint(), false, false, true),
                    new ColumnDefinition(SESSION_ID_FIELD, DataType.bigint(), false, false, true),
                    new ColumnDefinition(ITERATIONS_FIELD, DataType.cint()),
                    new ColumnDefinition(LAST_ATTEMPT_TIME_FIELD, DataType.bigint()),
                    new ColumnDefinition(SERIALIZED_COLUMN_NAME, DataType.blob()),
                    new ColumnDefinition(JSON_COLUMN_NAME, DataType.text())
            ), BANK_ID_FIELD)
            .clusteringOrder(CREATE_TIME_FIELD, SchemaBuilder.Direction.ASC);

    //CF for items with max iterations
    private static final TableDefinition MAX_ITERATIONS_TABLE = new TableDefinition(MAX_ITERATIONS_ITEM_CF,
            Arrays.asList(
                    new ColumnDefinition(BANK_ID_FIELD, DataType.bigint(), false, false, true),
                    new ColumnDefinition(CREATE_TIME_FIELD, DataType.bigint(), false, false, true),
                    new ColumnDefinition(SESSION_ID_FIELD, DataType.bigint(), false, false, true),
                    new ColumnDefinition(LAST_ATTEMPT_TIME_FIELD, DataType.bigint()),
                    new ColumnDefinition(SERIALIZED_COLUMN_NAME, DataType.blob()),
                    new ColumnDefinition(JSON_COLUMN_NAME, DataType.text())
            ), BANK_ID_FIELD)
            .clusteringOrder(CREATE_TIME_FIELD, SchemaBuilder.Direction.ASC);

    /**
     * For instantiate use {@link CassandraPersistenceManager#getPersister(Class)}
     */
    private CassandraHistoryInformerItemPersister() {
        super();
    }

    @Override
    public TableDefinition getMainTableDefinition() {
        return TABLE;
    }

    @Override
    public List<TableDefinition> getAllTableDefinitions() {
        return Arrays.asList(TABLE, MAX_ITERATIONS_TABLE);
    }

    @Override
    public Logger getLog() {
        return LOG;
    }

    public void delete(HistoryInformerItem item) {
        LOG.debug("delete {}", item);

        Delete.Where query = QueryBuilder.delete().from(HISTORY_INFORMER_ITEM_CF)
                .where(eq(BANK_ID_FIELD, item.getBankId()))
                .and(eq(CREATE_TIME_FIELD, item.getCreateTime()))
                .and(eq(SESSION_ID_FIELD, item.getGameSessionId()));

        execute(query, " deleteItem");
    }

    public void deleteFromMaxIterationsCF(HistoryInformerItem item) {
        LOG.debug("deleteFromMaxIterationsCF {}", item);

        Delete.Where query = QueryBuilder.delete().from(MAX_ITERATIONS_ITEM_CF)
                .where(eq(BANK_ID_FIELD, item.getBankId()))
                .and(eq(CREATE_TIME_FIELD, item.getCreateTime()))
                .and(eq(SESSION_ID_FIELD, item.getGameSessionId()));

        execute(query, " deleteItem");
    }

    public void insert(HistoryInformerItem item) {
        if (item == null) {
            throw new NullPointerException("Parameter \"item\" cannot be null");
        }

        LOG.debug("insert({})", item.toString());

        ByteBuffer byteBuffer = getMainTableDefinition().serializeToBytes(item);
        String json = getMainTableDefinition().serializeToJson(item);
        try {
            Insert insert = getInsertQuery()
                    .value(BANK_ID_FIELD, item.getBankId())
                    .value(CREATE_TIME_FIELD, item.getCreateTime())
                    .value(SESSION_ID_FIELD, item.getGameSessionId())
                    .value(ITERATIONS_FIELD, 0)
                    .value(LAST_ATTEMPT_TIME_FIELD, 0)
                    .value(SERIALIZED_COLUMN_NAME, byteBuffer)
                    .value(JSON_COLUMN_NAME, json);
            execute(insert, "insert");
        } finally {
            releaseBuffer(byteBuffer);
        }
    }

    public void update(HistoryInformerItem item, int maxSendAttempts) {
        if (item == null) {
            throw new NullPointerException("Parameter \"item\" cannot be null");
        }

        LOG.debug("update({})", item.toString());

        String json = getMainTableDefinition().serializeToJson(item);
        ByteBuffer byteBuffer = getMainTableDefinition().serializeToBytes(item);
        try {
            if (item.getIterations() <= maxSendAttempts) {
                Update.Where update = QueryBuilder.update(HISTORY_INFORMER_ITEM_CF)
                        .with(QueryBuilder.set(ITERATIONS_FIELD, item.getIterations()))
                        .and(QueryBuilder.set(LAST_ATTEMPT_TIME_FIELD, System.currentTimeMillis()))

                        .where(eq(BANK_ID_FIELD, item.getBankId()))
                        .and(eq(CREATE_TIME_FIELD, item.getCreateTime()))
                        .and(eq(SESSION_ID_FIELD, item.getGameSessionId()));

                execute(update, "update");
            } else {

                Batch batch = QueryBuilder.batch(QueryBuilder.delete().from(HISTORY_INFORMER_ITEM_CF)
                        .where(eq(BANK_ID_FIELD, item.getBankId()))
                        .and(eq(CREATE_TIME_FIELD, item.getCreateTime()))
                        .and(eq(SESSION_ID_FIELD, item.getGameSessionId())));

                batch.add(QueryBuilder.insertInto(MAX_ITERATIONS_ITEM_CF)
                        .value(BANK_ID_FIELD, item.getBankId())
                        .value(CREATE_TIME_FIELD, item.getCreateTime())
                        .value(SESSION_ID_FIELD, item.getGameSessionId())
                        .value(LAST_ATTEMPT_TIME_FIELD, System.currentTimeMillis())
                        .value(SERIALIZED_COLUMN_NAME, byteBuffer)
                        .value(JSON_COLUMN_NAME, json));

                execute(batch, "insert into " + MAX_ITERATIONS_ITEM_CF);
            }
        } finally {
            releaseBuffer(byteBuffer);
        }
    }

    public void processItemsForBank(long bankId, long minIntervalBetweenAttempts,
                                    TableProcessor<HistoryInformerItem> historyItemProcessor) {
        //LOG.debug("processItemsForBank({})", bankId);

        if (historyItemProcessor == null) {
            throw new NullPointerException("Parameter 'historyItemProcessor' can't be null");
        }

        com.datastax.driver.core.Statement query = QueryBuilder.select(SERIALIZED_COLUMN_NAME, LAST_ATTEMPT_TIME_FIELD, ITERATIONS_FIELD, JSON_COLUMN_NAME)
                .from(HISTORY_INFORMER_ITEM_CF)
                .where(eq(BANK_ID_FIELD, bankId))
                .setFetchSize(3);

        com.datastax.driver.core.ResultSet resultSet = execute(query, "processItemsForBank");
        int itemsCount = 0;
        for (Row row : resultSet) {
            String json = row.getString(JSON_COLUMN_NAME);
            HistoryInformerItem item = TABLE.deserializeFromJson(json, HistoryInformerItem.class);

            if (item == null) {
                ByteBuffer buffer = row.getBytes(SERIALIZED_COLUMN_NAME);
                item = TABLE.deserializeFrom(buffer, HistoryInformerItem.class);
            }

            long lastAttemptTime = row.getLong(LAST_ATTEMPT_TIME_FIELD);
            if (item != null && (lastAttemptTime == 0 ||
                    lastAttemptTime < System.currentTimeMillis() - minIntervalBetweenAttempts)) {
                item.setIterations(row.getInt(ITERATIONS_FIELD));

                LOG.debug(item.toString());
                try {
                    historyItemProcessor.process(item);
                    itemsCount++;
                } catch (IOException e) {
                    LOG.error("Error in historyItemProcessor.process({})", item.toString(), e);
                }
            } else if (item == null) {
                LOG.debug("item is null");
            }
        }

        //LOG.debug("Processed {} items (bank {})", itemsCount, bankId);
    }

    public HistoryInformerItem loadMaxIterationsItem(long bankId) {
        Select query = QueryBuilder.select(SERIALIZED_COLUMN_NAME, JSON_COLUMN_NAME)
                .from(MAX_ITERATIONS_ITEM_CF)
                .where(eq(BANK_ID_FIELD, bankId))
                .limit(1);

        com.datastax.driver.core.ResultSet resultSet = execute(query, "loadMaxIterationsItem");

        Row row = resultSet.one();
        if (row != null) {
            String json = row.getString(SERIALIZED_COLUMN_NAME);
            HistoryInformerItem obj = TABLE.deserializeFromJson(json, HistoryInformerItem.class);

            if (obj == null) {
                ByteBuffer buffer = row.getBytes(SERIALIZED_COLUMN_NAME);
                obj = TABLE.deserializeFrom(buffer, HistoryInformerItem.class);
            }
            return obj;
        }

        return null;
    }
}
