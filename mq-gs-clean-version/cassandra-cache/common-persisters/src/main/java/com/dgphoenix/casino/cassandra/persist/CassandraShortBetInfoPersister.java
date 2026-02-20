package com.dgphoenix.casino.cassandra.persist;

import com.datastax.driver.core.*;
import com.datastax.driver.core.querybuilder.Insert;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.datastax.driver.core.schemabuilder.SchemaBuilder;
import com.dgphoenix.casino.cassandra.persist.engine.AbstractCassandraPersister;
import com.dgphoenix.casino.cassandra.persist.engine.ColumnDefinition;
import com.dgphoenix.casino.cassandra.persist.engine.TableDefinition;
import com.dgphoenix.casino.cassandra.persist.engine.configuration.Caching;
import com.dgphoenix.casino.cassandra.persist.engine.configuration.CompactionStrategy;
import com.dgphoenix.casino.cassandra.persist.engine.configuration.Compression;
import com.dgphoenix.casino.common.cache.data.bet.ShortBetInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * User: flsh
 * Date: 07.03.17.
 */
public class CassandraShortBetInfoPersister extends AbstractCassandraPersister<Long, Long> {
    private static final Logger LOG = LogManager.getLogger(CassandraShortBetInfoPersister.class);
    private static final String BANK_ID_FIELD = "BID";
    private static final String BET_TIME_FIELD = "BTIME";
    private static final String ACCOUNT_ID_FIELD = "AID";

    private static final String COLUMN_FAMILY_NAME = "ShortBetInfoCF3";
    private static final TableDefinition TABLE = new TableDefinition(COLUMN_FAMILY_NAME,
            Arrays.asList(
                    new ColumnDefinition(BANK_ID_FIELD, DataType.cint(), false, false, true),
                    new ColumnDefinition(BET_TIME_FIELD, DataType.bigint(), false, false, true),
                    new ColumnDefinition(ACCOUNT_ID_FIELD, DataType.bigint(), false, false, true),
                    new ColumnDefinition(SERIALIZED_COLUMN_NAME, DataType.blob()),
                    new ColumnDefinition(JSON_COLUMN_NAME, DataType.text())
            ), BANK_ID_FIELD)
            .caching(Caching.NONE).compaction(CompactionStrategy.LEVELED)
            .gcGraceSeconds(TimeUnit.DAYS.toSeconds(1))
            .compression(Compression.DEFLATE)
            .clusteringOrder(BET_TIME_FIELD, SchemaBuilder.Direction.DESC);

    @Override
    public TableDefinition getMainTableDefinition() {
        return TABLE;
    }

    @Override
    public List<TableDefinition> getAllTableDefinitions() {
        return Collections.singletonList(TABLE);
    }

    public void getByBank(long bankId, long startDate, long endDate, IShortBetInfoProcessor processor) throws Exception {
        Select query = getSelectColumnsQuery(TABLE, SERIALIZED_COLUMN_NAME, JSON_COLUMN_NAME);
        Select.Where where = query.where();
        where.and(QueryBuilder.eq(BANK_ID_FIELD, bankId));
        where.and(QueryBuilder.gte(BET_TIME_FIELD, startDate));
        where.and(QueryBuilder.lte(BET_TIME_FIELD, endDate));
        ResultSet resultSet = execute(query, "getByBank");
        for (Row row : resultSet) {
            String json = row.getString(JSON_COLUMN_NAME);
            ShortBetInfo info = TABLE.deserializeFromJson(json, ShortBetInfo.class);

            if (info == null) {
                ByteBuffer buffer = row.getBytes(SERIALIZED_COLUMN_NAME);
                info = TABLE.deserializeFrom(buffer, ShortBetInfo.class);
            }

            processor.process(info);
        }
    }

    public List<ShortBetInfo> getByBank(long bankId, long startDate, long endDate) {
        List<ShortBetInfo> shortBetInfos = new ArrayList<>(getByBankFromTable(bankId, startDate, endDate, TABLE));
        Collections.sort(shortBetInfos);
        return shortBetInfos;
    }

    private List<ShortBetInfo> getByBankFromTable(long bankId, long startDate, long endDate, TableDefinition table) {
        Select query = getSelectColumnsQuery(table, SERIALIZED_COLUMN_NAME, JSON_COLUMN_NAME);
        Select.Where where = query.where();
        where.and(QueryBuilder.eq(BANK_ID_FIELD, bankId));
        where.and(QueryBuilder.gte(BET_TIME_FIELD, startDate));
        where.and(QueryBuilder.lte(BET_TIME_FIELD, endDate));
        ResultSet resultSet = execute(query, "getByBank");
        List<ShortBetInfo> result = new ArrayList<>(resultSet.getAvailableWithoutFetching());
        for (Row row : resultSet) {
            String json = row.getString(JSON_COLUMN_NAME);
            ShortBetInfo info = TABLE.deserializeFromJson(json, ShortBetInfo.class);

            if (info == null) {
                ByteBuffer buffer = row.getBytes(SERIALIZED_COLUMN_NAME);
                info = TABLE.deserializeFrom(buffer, ShortBetInfo.class);
            }
            result.add(info);
        }
        return result;
    }

    public void prepareToPersist(Map<Session, List<Statement>> statementsMap, ShortBetInfo betInfo,
                                 List<ByteBuffer> byteBuffersCollector, Integer ttl) {
        List<Statement> statements = getOrCreateStatements(statementsMap);
        String json = getMainTableDefinition().serializeToJson(betInfo);
        ByteBuffer byteBuffer = getMainTableDefinition().serializeToBytes(betInfo);
        byteBuffersCollector.add(byteBuffer);
        Insert query = getInsertQuery();
        query.value(BANK_ID_FIELD, betInfo.getBankId());
        query.value(BET_TIME_FIELD, betInfo.getTime());
        query.value(ACCOUNT_ID_FIELD, betInfo.getAccountId());
        query.value(SERIALIZED_COLUMN_NAME, byteBuffer);
        query.value(JSON_COLUMN_NAME, json);
        if (ttl != null) {
            query.using(QueryBuilder.ttl(ttl));
        }
        statements.add(query);
    }

    public void persist(ShortBetInfo betInfo, Integer ttl) {
        Insert query = getInsertQuery();
        query.value(BANK_ID_FIELD, betInfo.getBankId());
        query.value(BET_TIME_FIELD, betInfo.getTime());
        query.value(ACCOUNT_ID_FIELD, betInfo.getAccountId());
        if (ttl != null) {
            query.using(QueryBuilder.ttl(ttl));
        }
        String json = TABLE.serializeToJson(betInfo);
        ByteBuffer byteBuffer = TABLE.serializeToBytes(betInfo);
        try {
            query.value(SERIALIZED_COLUMN_NAME, byteBuffer);
            query.value(JSON_COLUMN_NAME, json);
            execute(query, "persist", ConsistencyLevel.LOCAL_ONE);
        } finally {
            releaseBuffer(byteBuffer);
        }
    }

    @Override
    public Logger getLog() {
        return LOG;
    }
}
