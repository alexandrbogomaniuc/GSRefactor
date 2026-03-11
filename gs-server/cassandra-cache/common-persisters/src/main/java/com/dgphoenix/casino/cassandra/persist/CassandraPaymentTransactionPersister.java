package com.abs.casino.cassandra.persist;

import com.abs.casino.cassandra.persist.engine.Cql;

import com.abs.casino.cassandra.persist.engine.AbstractCassandraPersister;
import com.abs.casino.cassandra.persist.engine.ColumnDefinition;
import com.abs.casino.cassandra.persist.engine.ICassandraPersister;
import com.abs.casino.cassandra.persist.engine.StatementPlan;
import com.abs.casino.cassandra.persist.engine.TableDefinition;
import com.abs.casino.cassandra.persist.CassandraAccountInfoPersister;
import com.abs.casino.common.SessionHelper;
import com.abs.casino.common.cache.CacheKeyInfo;
import com.abs.casino.common.cache.IDistributedCache;
import com.abs.casino.common.cache.data.account.AccountInfo;
import com.abs.casino.common.cache.data.payment.transfer.PaymentTransaction;
import com.abs.casino.common.util.string.StringUtils;
import com.abs.casino.common.web.statistics.StatisticsManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.ByteBuffer;
import java.util.*;

import static com.abs.casino.cassandra.persist.engine.CassandraDataTypes.bigint;
import static com.abs.casino.cassandra.persist.engine.CassandraDataTypes.blob;
import static com.abs.casino.cassandra.persist.engine.CassandraDataTypes.cint;
import static com.abs.casino.cassandra.persist.engine.CassandraDataTypes.text;

/**
 * User: flsh
 * Date: 25.10.14.
 */
@CacheKeyInfo(description = "paymentTransaction.id")
public class CassandraPaymentTransactionPersister extends AbstractCassandraPersister<Long, String> implements
        IDistributedCache<String, PaymentTransaction> {
    private static final String CF_NAME = "PaymentTransactionCF2";
    private static final String BUCKET_FIELD = "Bucket";
    private static final String START_DATE_FIELD = "StartDate";
    private static final String TRANSACTION_ID_FIELD = "TransactionId";
    //extId = bankId+extTransactionId
    private static final String EXTERNAL_ID_FIELD = "ExtId";
    private static final Logger LOG = LogManager.getLogger(CassandraPaymentTransactionPersister.class);
    private static final int RANDOM_FACTOR = 16;
    private CassandraAccountInfoPersister accountInfoPersister;
    private Random random;
    //key: bucket (random value), startDate, transactionId
    private static final TableDefinition TABLE = new TableDefinition(CF_NAME,
            Arrays.asList(
                    new ColumnDefinition(BUCKET_FIELD, cint(), false, false, true),
                    new ColumnDefinition(START_DATE_FIELD, bigint(), false, false, true),
                    new ColumnDefinition(KEY, bigint(), false, false, true),
                    new ColumnDefinition(TRANSACTION_ID_FIELD, bigint(), false, true, false),
                    //external_id = bankId+extId
                    new ColumnDefinition(EXTERNAL_ID_FIELD, text(), false, true, false),
                    new ColumnDefinition(SERIALIZED_COLUMN_NAME, blob()),
                    new ColumnDefinition(JSON_COLUMN_NAME, text())),
            BUCKET_FIELD);

    private CassandraPaymentTransactionPersister() {
        random = new Random(System.currentTimeMillis());
    }

    @SuppressWarnings("unused")
    private void setAccountInfoPersister(CassandraAccountInfoPersister accountInfoPersister) {
        this.accountInfoPersister = accountInfoPersister;
    }

    public void prepareToPersist(Map<com.abs.casino.cassandra.persist.engine.Session, List<com.datastax.driver.core.Statement>> statementsMap, PaymentTransaction transaction,
                                 List<ByteBuffer> byteBuffersCollector) {
        List<com.datastax.driver.core.Statement> statements = getOrCreateStatements(statementsMap);
        String json = TABLE.serializeToJson(transaction);
        ByteBuffer byteBuffer = TABLE.serializeToBytes(transaction);
        byteBuffersCollector.add(byteBuffer);
        statements.add(getUpdateStatement(transaction, null, byteBuffer, json));
    }

    public void prepareToPersist(StatementPlan statementsPlan, PaymentTransaction transaction,
                                 List<ByteBuffer> byteBuffersCollector) {
        List<com.abs.casino.cassandra.persist.engine.Statement> statements = getOrCreateStatements(statementsPlan);
        String json = TABLE.serializeToJson(transaction);
        ByteBuffer byteBuffer = TABLE.serializeToBytes(transaction);
        byteBuffersCollector.add(byteBuffer);
        statements.add(com.abs.casino.cassandra.persist.engine.Statement.of(getUpdateStatement(transaction, null, byteBuffer, json)));
    }

    private com.datastax.driver.core.Statement getUpdateStatement(PaymentTransaction transaction, String extIdOverride, ByteBuffer byteBuffer,
                                         String json) {
        com.datastax.driver.core.Statement query = getSelectColumnsQuery(TABLE, BUCKET_FIELD, START_DATE_FIELD)
                .where(eq(TRANSACTION_ID_FIELD, transaction.getId()));
        com.abs.casino.cassandra.persist.engine.ResultSet resultSet = executeWrapped(query, "getUpdateStatement");
        com.abs.casino.cassandra.persist.engine.Row stored = resultSet.one();
        int bucket;
        if (stored != null) {
            bucket = stored.getInt(BUCKET_FIELD);
        } else {
            bucket = random.nextInt(RANDOM_FACTOR);
        }
        String extId = resolveExtIdKey(transaction, extIdOverride);
        if (extId != null) {
            return getUpdateQuery()
                    .where(eq(BUCKET_FIELD, bucket))
                    .and(eq(START_DATE_FIELD, transaction.getStartDate()))
                    .and(eq(KEY, transaction.getId()))
                    .with(Cql.set(TRANSACTION_ID_FIELD, transaction.getId()))
                    .and(Cql.set(EXTERNAL_ID_FIELD, extId))
                    .and(Cql.set(SERIALIZED_COLUMN_NAME, byteBuffer))
                    .and(Cql.set(JSON_COLUMN_NAME, json));
        }
        return getUpdateQuery()
                .where(eq(BUCKET_FIELD, bucket))
                .and(eq(START_DATE_FIELD, transaction.getStartDate()))
                .and(eq(KEY, transaction.getId()))
                .with(Cql.set(TRANSACTION_ID_FIELD, transaction.getId()))
                .and(Cql.set(SERIALIZED_COLUMN_NAME, byteBuffer))
                .and(Cql.set(JSON_COLUMN_NAME, json));
    }

    private String resolveExtIdKey(PaymentTransaction transaction, String extIdOverride) {
        if (extIdOverride != null) {
            return extIdOverride;
        }
        if (transaction.getExternalTransactionId() == null) {
            return null;
        }
        int bankId = SessionHelper.getInstance().getTransactionData().getBankId();
        if (bankId <= 0) {
            getLog().warn("save: bankId not initialized in TD: {}", SessionHelper.getInstance().getTransactionData());
            AccountInfo accountInfo = accountInfoPersister.getById(transaction.getAccountId());
            bankId = accountInfo.getBankId();
        }
        return buildExtIdKey(bankId, transaction.getExternalTransactionId());
    }

    public void save(PaymentTransaction transaction) {
        getLog().debug("save: {}", transaction);
        String json = TABLE.serializeToJson(transaction);
        ByteBuffer byteBuffer = TABLE.serializeToBytes(transaction);
        try {
            com.datastax.driver.core.Statement query = getUpdateStatement(transaction, null, byteBuffer, json);
            execute(query, "save");
        } finally {
            releaseBuffer(byteBuffer);
        }
    }

    public PaymentTransaction getTransaction(long transactionId) {
        getLog().debug("getTransaction: {}", transactionId);
        com.datastax.driver.core.Statement select = getSelectColumnsQuery(TABLE, SERIALIZED_COLUMN_NAME, JSON_COLUMN_NAME)
                .where(eq(TRANSACTION_ID_FIELD, transactionId));
        com.abs.casino.cassandra.persist.engine.ResultSet resultSet = executeWrapped(select, "getTransaction");
        com.abs.casino.cassandra.persist.engine.Row row = resultSet.one();
        if (row == null) {
            return null;
        }

        String json = row.getString(JSON_COLUMN_NAME);
        PaymentTransaction obj = TABLE.deserializeFromJson(json, PaymentTransaction.class);

        if (obj == null) {
            ByteBuffer buffer = row.getBytes(SERIALIZED_COLUMN_NAME);
            obj = TABLE.deserializeFrom(buffer, PaymentTransaction.class);
        }
        return obj;
    }

    public void loadAndProcess(long startRangeDate, long endRangeDate, PaymentTransactionProcessor processor) {
        int count = 0;
        for (int i = 0; i < RANDOM_FACTOR; i++) {
            com.datastax.driver.core.Statement query = getSelectColumnsQuery(TABLE, SERIALIZED_COLUMN_NAME, JSON_COLUMN_NAME)
                    .where(eq(BUCKET_FIELD, i))
                    .and(Cql.gte(START_DATE_FIELD, startRangeDate))
                    .and(Cql.lte(START_DATE_FIELD, endRangeDate));
            com.abs.casino.cassandra.persist.engine.ResultSet resultSet = executeWrapped(query, "loadAndProcess");
            for (com.abs.casino.cassandra.persist.engine.Row row : resultSet) {
                String json = row.getString(JSON_COLUMN_NAME);
                PaymentTransaction transaction = TABLE.deserializeFromJson(json, PaymentTransaction.class);

                if (transaction == null) {
                    ByteBuffer buffer = row.getBytes(SERIALIZED_COLUMN_NAME);
                    transaction = TABLE.deserializeFrom(buffer, PaymentTransaction.class);
                }
                getLog().debug("loadAndProcess: {}", transaction);
                processor.process(transaction);
                count++;
            }
        }
        getLog().debug("loadAndProcess: count={}", count);
    }

    public List<Long> getTransactionIdsByDateRange(long startDate, long endDate) {
        long now = System.currentTimeMillis();
        int count = 0;
        List<Long> transactionIds = new ArrayList<>();
        for (int i = 0; i < RANDOM_FACTOR; i++) {
            com.datastax.driver.core.Statement query = getSelectColumnsQuery(TABLE, TRANSACTION_ID_FIELD)
                    .where(eq(BUCKET_FIELD, i))
                    .and(Cql.gte(START_DATE_FIELD, startDate))
                    .and(Cql.lte(START_DATE_FIELD, endDate));
            com.abs.casino.cassandra.persist.engine.ResultSet resultSet = executeWrapped(query, "getTransactionIdsByDateRange");
            for (com.abs.casino.cassandra.persist.engine.Row row : resultSet) {
                long transactionId = row.getLong(TRANSACTION_ID_FIELD);
                transactionIds.add(transactionId);
                count++;
            }
        }
        getLog().debug("getTransactionIdsByDateRange: count={}", count);
        StatisticsManager.getInstance().updateRequestStatistics("CassandraPaymentTransactionPersister " +
                "getTransactionIdsByDateRange", System.currentTimeMillis() - now);
        return transactionIds;
    }

    public void saveExternalTransactionId(PaymentTransaction transaction, long bankId) {
        getLog().debug("saveExternalTransactionId: {}", transaction);
        if (StringUtils.isTrimmedEmpty(transaction.getExternalTransactionId())) {
            throw new RuntimeException("External transactionId is empty, transactionId=" + transaction.getId());
        }
        String extId = buildExtIdKey(bankId, transaction.getExternalTransactionId());
        String json = TABLE.serializeToJson(transaction);
        ByteBuffer byteBuffer = TABLE.serializeToBytes(transaction);
        try {
            com.datastax.driver.core.Statement updateStatement = getUpdateStatement(transaction, extId, byteBuffer, json);
            execute(updateStatement, "saveExternalTransactionId");
        } finally {
            releaseBuffer(byteBuffer);
        }
    }

    public PaymentTransaction getTransactionByExtId(long bankId, String extId) {
        String extKey = buildExtIdKey(bankId, extId);
        com.datastax.driver.core.Statement query = getSelectColumnsQuery(TABLE, SERIALIZED_COLUMN_NAME, JSON_COLUMN_NAME)
                .where(eq(EXTERNAL_ID_FIELD, extKey));
        com.abs.casino.cassandra.persist.engine.ResultSet resultSet = executeWrapped(query, "getUncompletedTransactionIdByExtId");
        com.abs.casino.cassandra.persist.engine.Row row = resultSet.one();
        if (row == null || row.isNull(SERIALIZED_COLUMN_NAME) || row.isNull(JSON_COLUMN_NAME)) {
            return null;
        }
        String json = row.getString(JSON_COLUMN_NAME);
        PaymentTransaction transaction = TABLE.deserializeFromJson(json, PaymentTransaction.class);

        if (transaction == null) {
            ByteBuffer buffer = row.getBytes(SERIALIZED_COLUMN_NAME);
            transaction = TABLE.deserializeFrom(buffer, PaymentTransaction.class);
        }
        getLog().debug("getTransactionByExtId: {}, bankId={}, extId={}", transaction, bankId, extId);
        return transaction;
    }

    @Override
    public Map<String, PaymentTransaction> getAllObjects() {
        return Collections.emptyMap();
    }

    private String buildExtIdKey(long bankId, String extId) {
        return bankId + ICassandraPersister.ID_DELIMITER + extId;
    }

    @Override
    public TableDefinition getMainTableDefinition() {
        return TABLE;
    }

    @Override
    public Logger getLog() {
        return LOG;
    }

    @Override
    public PaymentTransaction getObject(String id) {
        return getTransaction(Long.parseLong(id));
    }

    @Override
    public String getAdditionalInfo() {
        return null;
    }

    @Override
    public String printDebug() {
        return null;
    }

    public interface PaymentTransactionProcessor {
        void process(PaymentTransaction transaction);
    }

    @Override
    public List<TableDefinition> getAllTableDefinitions() {
        return Collections.singletonList(TABLE);
    }
}
