package com.abs.casino.cassandra.persist;

import com.dgphoenix.casino.cassandra.persist.engine.AbstractCassandraPersister;
import com.dgphoenix.casino.cassandra.persist.engine.ColumnDefinition;
import com.dgphoenix.casino.cassandra.persist.engine.TableDefinition;
import com.abs.casino.cassandra.persist.engine.configuration.Caching;
import com.abs.casino.cassandra.persist.engine.configuration.CompactionStrategy;
import com.abs.casino.cassandra.persist.engine.configuration.Compression;
import com.dgphoenix.casino.common.api.IAccountInfoPersister;
import com.abs.casino.common.cache.ExportableCacheEntry;
import com.dgphoenix.casino.common.cache.data.account.AccountInfo;
import com.abs.casino.common.configuration.CasinoSystemType;
import com.abs.casino.common.configuration.ServerConfiguration;
import com.abs.casino.common.persist.StreamPersister;
import com.abs.casino.common.persist.TableProcessor;
import com.dgphoenix.casino.common.util.NtpTimeProvider;
import com.dgphoenix.casino.common.util.Pair;
import com.dgphoenix.casino.common.web.statistics.StatisticsManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * User: flsh
 * Date: 27.03.12
 */
public class CassandraAccountInfoPersister extends AbstractCassandraPersister<Long, String>
        implements IAccountInfoPersister, StreamPersister<String, AccountInfo> {
    public static final String ACCOUNT_CF = "AccountCF";
    public static final String EXT_ID_CF_INDEX = "AccountCF_EXT";
    public static final String BANK_TESTER_CF_INDEX = "AccountCF_BANK_TESTER";

    public static final String EXTERNAL_ID_FIELD = "ExtId";
    public static final String BANK_ID_FIELD = "BankId";
    public static final String ACCOUNT_ID_FIELD = "AccountId";
    private static final Logger LOG = LogManager.getLogger(CassandraAccountInfoPersister.class);
    private static final TableDefinition TABLE = new TableDefinition(ACCOUNT_CF,
            Arrays.asList(
                    new ColumnDefinition(KEY, com.datastax.driver.core.DataType.bigint(), false, false, true),
                    new ColumnDefinition(SERIALIZED_COLUMN_NAME, com.datastax.driver.core.DataType.blob()),
                    new ColumnDefinition(JSON_COLUMN_NAME, com.datastax.driver.core.DataType.text())
            ), KEY)
            .caching(Caching.NONE)
            .compaction(CompactionStrategy.LEVELED)
            .gcGraceSeconds(TimeUnit.DAYS.toSeconds(1))
            .compression(Compression.DEFLATE);

    private static final TableDefinition EXT_ID_INDEX_TABLE = new TableDefinition(EXT_ID_CF_INDEX,
            Arrays.asList(
                    new ColumnDefinition(BANK_ID_FIELD, com.datastax.driver.core.DataType.cint(), false, false, true),
                    new ColumnDefinition(EXTERNAL_ID_FIELD, com.datastax.driver.core.DataType.text(), false, false, true),
                    new ColumnDefinition(ACCOUNT_ID_FIELD, com.datastax.driver.core.DataType.bigint(), false, false, false)
            ), BANK_ID_FIELD)
            .caching(Caching.ACTUAL_DATA)
            .compaction(CompactionStrategy.LEVELED)
            .gcGraceSeconds(TimeUnit.DAYS.toSeconds(1))
            .compression(Compression.NONE);

    private static final TableDefinition BANK_TESTER_ID_INDEX_TABLE = new TableDefinition(BANK_TESTER_CF_INDEX,
            Arrays.asList(
                    new ColumnDefinition(BANK_ID_FIELD, com.datastax.driver.core.DataType.cint(), false, false, true),
                    new ColumnDefinition(EXTERNAL_ID_FIELD, com.datastax.driver.core.DataType.text(), false, false, true),
                    new ColumnDefinition(ACCOUNT_ID_FIELD, com.datastax.driver.core.DataType.bigint(), false, false, false)
            ), BANK_ID_FIELD)
            .caching(Caching.NONE)
            .compaction(CompactionStrategy.LEVELED)
            .gcGraceSeconds(TimeUnit.DAYS.toSeconds(1))
            .compression(Compression.NONE);

    private CassandraAccountInfoPersister() {
        super();
    }

    @Override
    public TableDefinition getMainTableDefinition() {
        return TABLE;
    }

    @Override
    public List<TableDefinition> getAllTableDefinitions() {
        return Arrays.asList(TABLE, EXT_ID_INDEX_TABLE, BANK_TESTER_ID_INDEX_TABLE);
    }

    @Override
    public Logger getLog() {
        return LOG;
    }

    public void prepareToPersist(Map<com.datastax.driver.core.Session, List<com.datastax.driver.core.Statement>> statementsMap, AccountInfo account, List<ByteBuffer> byteBuffersCollector) {
        if (account.isGuest()) {
            LOG.warn("prepareToPersist: persist skipped [this may be error, please fix], found guest account: {}",
                    account.getId());
            return;
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("prepareToPersist: {}", account.getId());
        }
        List<com.datastax.driver.core.Statement> statements = getOrCreateStatements(statementsMap);
        ByteBuffer bytes = TABLE.serializeToBytes(account);
        byteBuffersCollector.add(bytes);
        String json = TABLE.serializeToJson(account);
        statements.add(getUpdateQuery(account.getId()).with().
                and(com.datastax.driver.core.querybuilder.QueryBuilder.set(SERIALIZED_COLUMN_NAME, bytes)).
                and(com.datastax.driver.core.querybuilder.QueryBuilder.set(JSON_COLUMN_NAME, json)));
    }

    public void persist(AccountInfo account) {
        persist(account, true);
    }

    public void persist(AccountInfo account, boolean newAccount) {
        if (account.isGuest()) {
            LOG.warn("persist skipped [this may be error, please fix], found guest account: {}", account.getId());
            return;
        }
        Long lastTimestamp = null;
        try {
            lastTimestamp = getWriteTime(account.getId());
        } catch (Exception e) {
            LOG.error("Cannot load writeTime for account: " + account.getId(), e);
        }
        long time = NtpTimeProvider.getInstance().getTimeMicroseconds();
        if (lastTimestamp != null && lastTimestamp >= time) {
            LOG.warn("Found possible lost write, need correction, currentTime={}, lastTimestamp={}, accountId={}",
                    time, lastTimestamp, account.getId());
            time = lastTimestamp + 1;
        }
        LOG.debug("persist account: {}, time={}, lastTimestamp={}", account.getId(), time, lastTimestamp);
        com.datastax.driver.core.querybuilder.Update query = getUpdateQuery(account.getId());
        query.using(com.datastax.driver.core.querybuilder.QueryBuilder.timestamp(time));
        ByteBuffer byteBuffer = TABLE.serializeToBytes(account);
        String json = TABLE.serializeToJson(account);
        com.datastax.driver.core.ResultSet result = null;
        try {
            query.with(com.datastax.driver.core.querybuilder.QueryBuilder.set(SERIALIZED_COLUMN_NAME, byteBuffer));
            query.with(com.datastax.driver.core.querybuilder.QueryBuilder.set(JSON_COLUMN_NAME, json));
            if (newAccount) {
                com.datastax.driver.core.querybuilder.Batch batch = com.datastax.driver.core.querybuilder.QueryBuilder.batch();
                batch.add(query);

                com.datastax.driver.core.querybuilder.Insert indexQuery = com.datastax.driver.core.querybuilder.QueryBuilder.insertInto(EXT_ID_CF_INDEX);
                indexQuery.value(BANK_ID_FIELD, account.getBankId()).
                        value(EXTERNAL_ID_FIELD, account.getExternalId()).
                        value(ACCOUNT_ID_FIELD, account.getId());
                batch.add(indexQuery);

                result = execute(batch, "persist (new)");
            } else {
                result = execute(query, "persist");
            }

            LOG.debug("persist account: {}, result={}", account.getId(), result.wasApplied());

        } finally {
            releaseBuffer(byteBuffer);
        }
    }

    @Override
    public AccountInfo getById(long id) {
        return get(id);
    }

    public AccountInfo get(long id) {
        String json = getJson(id);
        AccountInfo ai = TABLE.deserializeFromJson(json, AccountInfo.class);
        if (ai == null) {
            ByteBuffer buffer = get(id, SERIALIZED_COLUMN_NAME);
            ai = TABLE.deserializeFrom(buffer, AccountInfo.class);
        }
        return ai;
    }

    public Long getAccountIdByExtId(long bankId, String externalId) {
        long now = System.currentTimeMillis();
        Long accountId = getAccountId(bankId, externalId, "getAccountIdByExtId");
        StatisticsManager.getInstance().updateRequestStatistics(getClass().getSimpleName() + " getAccountIdByExtId",
                System.currentTimeMillis() - now);
        return accountId;
    }

    public AccountInfo getByCompositeKey(long bankId, String externalId) {
        long now = System.currentTimeMillis();
        Long accountId = getAccountId(bankId, externalId, "getByCompositeKey");
        AccountInfo accountInfo = null;
        if (accountId != null) {
            accountInfo = get(accountId);
        }
        StatisticsManager.getInstance().updateRequestStatistics(getClass().getSimpleName() + " getByCompositeKey",
                System.currentTimeMillis() - now);
        return accountInfo;
    }

    private Long getAccountId(long bankId, String externalId, String callerClassMethodIdentification) {
        com.datastax.driver.core.querybuilder.Select query = com.datastax.driver.core.querybuilder.QueryBuilder.select(ACCOUNT_ID_FIELD).from(EXT_ID_CF_INDEX);
        query.where(eq(BANK_ID_FIELD, (int) bankId)).and(eq(EXTERNAL_ID_FIELD, externalId));
        com.datastax.driver.core.ResultSet resultSet = execute(query, callerClassMethodIdentification);
        com.datastax.driver.core.Row row = resultSet.one();
        Long accountId = null;
        if (row != null) {
            accountId = row.getLong(ACCOUNT_ID_FIELD);
        }
        return accountId;
    }

    public void delete(long id) {
        AccountInfo accountInfo = get(id);
        LOG.debug("delete: {}", accountInfo);
        if (accountInfo != null) {
            com.datastax.driver.core.querybuilder.Delete query = com.datastax.driver.core.querybuilder.QueryBuilder.delete().from(EXT_ID_INDEX_TABLE.getTableName());
            query.where(eq(BANK_ID_FIELD, accountInfo.getBankId()))
                    .and(eq(EXTERNAL_ID_FIELD, accountInfo.getExternalId()));
            execute(query, " delete accountExtIndex");
        }
        deleteItem(id);
    }

    //return pair: extUserId, accountId
    public List<Pair<String, Long>> getExtAccountIdsPair(long bankId) {
        long now = System.currentTimeMillis();
        ArrayList<Pair<String, Long>> result = new ArrayList<>();
        com.datastax.driver.core.querybuilder.Select query = com.datastax.driver.core.querybuilder.QueryBuilder.select(EXTERNAL_ID_FIELD, ACCOUNT_ID_FIELD).from(EXT_ID_CF_INDEX);
        query.where(eq(BANK_ID_FIELD, bankId));
        try {
            com.datastax.driver.core.ResultSet resultSet = execute(query, "getExtAccountIdsPair");
            for (com.datastax.driver.core.Row row : resultSet) {
                String extId = row.getString(EXTERNAL_ID_FIELD);
                long accountId = row.getLong(ACCOUNT_ID_FIELD);
                result.add(new Pair<>(extId, accountId));
            }
            StatisticsManager.getInstance().updateRequestStatistics("getExtAccountIdsPair",
                    System.currentTimeMillis() - now);
        } catch (com.datastax.driver.core.exceptions.QueryExecutionException e) {
            getLog().error(getClass().getSimpleName() + ":getExtAccountIdsPair execute com.datastax.driver.core.exceptions.QueryExecutionException, " +
                    "duration ms=" + (System.currentTimeMillis() - now), e);
            throw e;
        }
        return result;
    }


    public List<Long> getAccountIds(long bankId) {
        long now = System.currentTimeMillis();
        ArrayList<Long> result = new ArrayList<>();
        com.datastax.driver.core.querybuilder.Select query = com.datastax.driver.core.querybuilder.QueryBuilder.select(ACCOUNT_ID_FIELD).from(EXT_ID_CF_INDEX);
        query.where(eq(BANK_ID_FIELD, bankId));
        try {
            com.datastax.driver.core.ResultSet resultSet = execute(query, "getAccountIds");
            for (com.datastax.driver.core.Row row : resultSet) {
                result.add(row.getLong(ACCOUNT_ID_FIELD));

            }
            StatisticsManager.getInstance().updateRequestStatistics("getAccountIds", System.currentTimeMillis() - now);
        } catch (com.datastax.driver.core.exceptions.QueryExecutionException e) {
            getLog().error(getClass().getSimpleName() + ":getAccountIds execute com.datastax.driver.core.exceptions.QueryExecutionException, duration ms=" +
                    (System.currentTimeMillis() - now), e);
            throw e;
        }
        return result;
    }

    public void exportAccounts(ObjectOutputStream outStream, Long bankId) throws IOException {
        Iterator<com.datastax.driver.core.Row> all = bankId == null ? getAll() : getAll(eq(BANK_ID_FIELD, bankId));
        while (all.hasNext()) {
            com.datastax.driver.core.Row row = all.next();
            String json = row.getString(JSON_COLUMN_NAME);
            AccountInfo accountInfo = TABLE.deserializeFromJson(json, AccountInfo.class);

            if (accountInfo == null) {
                ByteBuffer bytes = row.getBytes(SERIALIZED_COLUMN_NAME);
                accountInfo = TABLE.deserializeFrom(bytes, AccountInfo.class);
            }
            if (accountInfo != null) {
                outStream.writeObject(new ExportableCacheEntry(accountInfo.getId(), accountInfo));
            }
        }
    }

    public boolean isSingleCasinoMode() {
        final CasinoSystemType type = ServerConfiguration.getInstance().getCasinoSystemType();
        return type.isSingleBank();
    }

    public Map<Long, AccountInfo> getByIds(Collection<Long> accountIds) {
        com.datastax.driver.core.querybuilder.Select query = getSelectColumnsQuery(KEY, SERIALIZED_COLUMN_NAME, JSON_COLUMN_NAME);
        query.where(com.datastax.driver.core.querybuilder.QueryBuilder.in(KEY, accountIds.toArray()));
        com.datastax.driver.core.ResultSet resultSet = execute(query, "getByIds");
        Map<Long, AccountInfo> result = new HashMap<>();
        for (com.datastax.driver.core.Row row : resultSet) {
            long accountId = row.getLong(KEY);
            String json = row.getString(JSON_COLUMN_NAME);
            AccountInfo ai = TABLE.deserializeFromJson(json, AccountInfo.class);
            
            if (ai == null) {
                ByteBuffer bytes = row.getBytes(SERIALIZED_COLUMN_NAME);
                ai = TABLE.deserializeFrom(bytes, AccountInfo.class);
            }
            if (ai != null) {
                result.put(accountId, ai);
            }
        }
        return result;
    }

    @Override
    public void processAll(TableProcessor<Pair<String, AccountInfo>> tableProcessor) throws IOException {
        Iterator<com.datastax.driver.core.Row> iterator = getAll();
        while (iterator.hasNext()) {
            com.datastax.driver.core.Row row = iterator.next();
            processRow(row, tableProcessor);
        }
    }

    @Override
    public void processByCondition(TableProcessor<Pair<String, AccountInfo>> tableProcessor, String conditionName, Object... conditionValues)
            throws IOException {
        if ("byBank".equals(conditionName)) {
            Long bankId = (Long) conditionValues[0];
            List<Long> accountIds = getAccountIds(bankId);
            com.datastax.driver.core.querybuilder.Select query = getSelectColumnsQuery(KEY, SERIALIZED_COLUMN_NAME, JSON_COLUMN_NAME);
            query.where(com.datastax.driver.core.querybuilder.QueryBuilder.in(KEY, accountIds.toArray()));
            com.datastax.driver.core.ResultSet resultSet = execute(query, "AccountInfoPersister: processByCondition byIds");
            for (com.datastax.driver.core.Row row : resultSet) {
                processRow(row, tableProcessor);
            }
        }
    }

    public void persistTesterIndex(AccountInfo account) {
        com.datastax.driver.core.querybuilder.Insert indexQuery = com.datastax.driver.core.querybuilder.QueryBuilder.insertInto(BANK_TESTER_CF_INDEX);
        indexQuery.value(BANK_ID_FIELD, account.getBankId()).
                value(EXTERNAL_ID_FIELD, account.getExternalId()).
                value(ACCOUNT_ID_FIELD, account.getId());
        execute(indexQuery, "AccountInfoPersister: refreshTesterIndex:insert");
    }

    public void removeTesterIndex(AccountInfo account) {
        com.datastax.driver.core.querybuilder.Delete delete = com.datastax.driver.core.querybuilder.QueryBuilder.delete().from(BANK_TESTER_CF_INDEX);
        delete.where(com.datastax.driver.core.querybuilder.QueryBuilder.eq(BANK_ID_FIELD, account.getBankId())).
                and(com.datastax.driver.core.querybuilder.QueryBuilder.eq(EXTERNAL_ID_FIELD, account.getExternalId()));
        execute(delete, "AccountInfoPersister: refreshTesterIndex:delete");
    }

    public Set<String> getTestersExternalId(long bankId) {
        com.datastax.driver.core.querybuilder.Select select = com.datastax.driver.core.querybuilder.QueryBuilder.select(EXTERNAL_ID_FIELD).from(BANK_TESTER_CF_INDEX);
        select.where(com.datastax.driver.core.querybuilder.QueryBuilder.eq(BANK_ID_FIELD, bankId));
        com.datastax.driver.core.ResultSet resultSet = execute(select, "AccountInfoPersister: getTestersExternalId");
        Set<String> testers = new HashSet<>();
        for (com.datastax.driver.core.Row row : resultSet) {
            String externalId = row.getString(EXTERNAL_ID_FIELD);
            testers.add(externalId);
        }
        return testers;
    }

    private void processRow(com.datastax.driver.core.Row row, TableProcessor<Pair<String, AccountInfo>> tableProcessor) throws IOException {
        String accountId = String.valueOf(row.getLong(KEY));
        AccountInfo accountInfo = TABLE.deserializeFromJson(row.getString(JSON_COLUMN_NAME), AccountInfo.class);
        if (accountInfo == null) {
            accountInfo = TABLE.deserializeFrom(row.getBytes(SERIALIZED_COLUMN_NAME), AccountInfo.class);
        }
        if (accountInfo != null) {
            tableProcessor.process(new Pair<>(accountId, accountInfo));
        }
    }
}
