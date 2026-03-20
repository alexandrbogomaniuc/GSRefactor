package com.abs.casino.cassandra.persist;

import com.abs.casino.cassandra.persist.engine.Cql;

import com.abs.casino.cassandra.persist.engine.AbstractCassandraPersister;
import com.abs.casino.cassandra.persist.engine.ColumnDefinition;
import com.abs.casino.cassandra.persist.engine.TableDefinition;
import com.abs.casino.common.cache.data.account.AccountInfo;
import com.abs.casino.common.cache.data.bank.BankInfo;
import com.abs.casino.common.cache.data.payment.WalletOperationInfo;
import com.abs.casino.common.exception.CommonException;
import com.abs.casino.common.util.CalendarUtils;
import com.abs.casino.common.web.statistics.StatisticsManager;
import com.abs.casino.gs.managers.payment.wallet.CommonWalletOperation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.stream.Collectors;

import static com.abs.casino.cassandra.persist.engine.CassandraDataTypes.bigint;
import static com.abs.casino.cassandra.persist.engine.CassandraDataTypes.blob;
import static com.abs.casino.cassandra.persist.engine.CassandraDataTypes.text;
import static java.util.Collections.emptyList;

/**
 * User: flsh
 * Date: 07.06.13
 */
public class CassandraWalletOperationInfoPersister extends AbstractCassandraPersister<Long, String> {
    private static final Logger LOG = LogManager.getLogger(CassandraWalletOperationInfoPersister.class);
    public static final String COLUMN_FAMILY_NAME = "WopCF";
    private static final String GAME_SESSION_ID_FIELD = "GameSessId";
    private static final String DAY_FIELD = "Day";
    private static final String ROUND_ID_FIELD = "RoundId";

    private static final TableDefinition TABLE = new TableDefinition(COLUMN_FAMILY_NAME,
            Arrays.asList(
                    //key is walletOperation.id
                    new ColumnDefinition(KEY, bigint(), false, false, true),
                    new ColumnDefinition(GAME_SESSION_ID_FIELD, bigint(), false, false, false),
                    new ColumnDefinition(DAY_FIELD, bigint(), false, true, false),
                    new ColumnDefinition(ROUND_ID_FIELD, bigint(), false, true, false),
                    new ColumnDefinition(SERIALIZED_COLUMN_NAME, blob()),
                    new ColumnDefinition(JSON_COLUMN_NAME, text())
            ),
            Collections.singletonList(KEY));

    private CassandraWalletOperationInfoPersister() {
    }

    @Override
    public TableDefinition getMainTableDefinition() {
        return TABLE;
    }

    public void persist(WalletOperationInfo info, int ttl) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("persist: " + info);
        }
        String json = TABLE.serializeToJson(info);
        ByteBuffer byteBuffer = TABLE.serializeToBytes(info);
        try {
            com.abs.casino.cassandra.persist.engine.Statement query = com.abs.casino.cassandra.persist.engine.Statement.of(getInsertQuery(ttl).
                    value(KEY, info.getId()).
                    value(DAY_FIELD, getDay(info.getEndTime())).
                    value(GAME_SESSION_ID_FIELD, info.getGameSessionId()).
                    value(ROUND_ID_FIELD, info.getRoundId()).
                    value(SERIALIZED_COLUMN_NAME, byteBuffer).
                    value(JSON_COLUMN_NAME, json));
            execute(query, "persist");
        } finally {
            releaseBuffer(byteBuffer);
        }
    }

    @Override
    public Logger getLog() {
        return LOG;
    }

    public WalletOperationInfo getById(long id) {
        return get(id, WalletOperationInfo.class);
    }

    public List<WalletOperationInfo> getByRoundId(long roundId) {
        com.abs.casino.cassandra.persist.engine.Statement query = com.abs.casino.cassandra.persist.engine.Statement.of(getSelectColumnsQuery(SERIALIZED_COLUMN_NAME, JSON_COLUMN_NAME)
                .where(eq(ROUND_ID_FIELD, roundId)));
        com.abs.casino.cassandra.persist.engine.ResultSet resultSet = executeWrapped(query, "getByRoundId");
        if (resultSet.isExhausted()) {
            return emptyList();
        }
        return resultSet.all().stream()
                .filter(Objects::nonNull)
                .map(row -> {
                    WalletOperationInfo woi = TABLE.deserializeFromJson(row.getString(JSON_COLUMN_NAME), WalletOperationInfo.class);
                    if (woi == null) {
                        woi = TABLE.deserializeFrom(row.getBytes(SERIALIZED_COLUMN_NAME), WalletOperationInfo.class);
                    }
                    return woi;
                })
                .collect(Collectors.toList());
    }

    //first min, second max
/*
    public WalletOperationInfo[] getMinMaxOperations(long startTime, long endTime, long subCasinoId) {
        long now = System.currentTimeMillis();
        CqlRows<Long, String, byte[]> rows = executeCQL("select " + SERIALIZED_COLUMN_NAME + " from " +
                        COLUMN_FAMILY_NAME + " where " +
                        (getDateNameField() + " >= " + startTime + " AND ") +
                        (getDateNameField() + " <= " + endTime + " AND ") +
                        (SUBCASINO_ID_FIELD + " = " + subCasinoId) +
                        " limit " + 101111111,
                KeySpaceManager.bytesArraySerializer);
        WalletOperationInfo[] result = new WalletOperationInfo[2];
        if(rows != null) {
            List<com.abs.casino.cassandra.persist.engine.Row> rowsList = rows.getList();
            for (com.abs.casino.cassandra.persist.engine.Row row : rowsList) {
                ColumnSlice<String, byte[]> rowColumnSlice = row.getColumnSlice();
                List<HColumn<String, byte[]>> columns = rowColumnSlice.getColumns();
                for (HColumn<String, byte[]> column : columns) {
                    if(column.getName().equalsIgnoreCase(SERIALIZED_COLUMN_NAME)) {
                        WalletOperationInfo info = deserializeFrom(column.getValue(),
                                WalletOperationInfo.class);
                        if(result[0] == null) {
                            result[0] = info;
                            result[1] = info;
                        }
                        if(result[0].getEndTime() > info.getEndTime()) {
                            result[0] = info;
                        }
                        if(result[1].getEndTime() < info.getEndTime()) {
                            result[1] = info;
                        }
                    }
                }
            }
        }
        StatisticsManager.getInstance().updateRequestStatistics(getClass().getSimpleName() + " getRecords",
                System.currentTimeMillis() - now);
        return result;
    }

    public Integer getRecordsCount(Date startDate, Date endDate) {
        String cql = "select count(1) from " + getMainColumnFamilyName() + " where "
                + FAKE_FIELD + "=" + FAKE_FIELD_VALUE + " AND " +
                (startDate == null ? "" : getDateNameField() + ">=" + startDate.getTime() + " AND ") +
                (endDate == null ? "" : getDateNameField() + "<=" + endDate.getTime()) +
                " limit " + 101111111;
        LOG.debug("getRecordsCount: cql=" + cql);
        return executeAsCountCQL(cql, true);
    }

    public Collection<WalletOperationInfo> getRecords(Date startDate, Date endDate, int from, int count)
            throws CommonException {
        long now = System.currentTimeMillis();
        String cql = "select " + SERIALIZED_COLUMN_NAME + " from " +
                getMainColumnFamilyName() + " where "
                + FAKE_FIELD + "=" + FAKE_FIELD_VALUE + " AND " +
                (startDate == null ? "" : getDateNameField() + ">=" + startDate.getTime() + " AND ") +
                (endDate == null ? "" : getDateNameField() + "<=" + endDate.getTime()) +
                " limit " + 101111111;
        LOG.debug("getRecords: cql=" + cql);
        CqlRows<Long, String, byte[]> rows = executeCQL(cql, KeySpaceManager.bytesArraySerializer);
        List<WalletOperationInfo> result = new ArrayList<WalletOperationInfo>();
        if(rows != null) {
            List<com.abs.casino.cassandra.persist.engine.Row> rowsList = rows.getList();
            LOG.debug("getRecords: rowsList.size()" + rowsList.size());
            for (com.abs.casino.cassandra.persist.engine.Row row : rowsList) {
                ColumnSlice<String, byte[]> rowColumnSlice = row.getColumnSlice();
                List<HColumn<String, byte[]>> columns = rowColumnSlice.getColumns();
                for (HColumn<String, byte[]> column : columns) {
                    if(column.getName().equalsIgnoreCase(SERIALIZED_COLUMN_NAME)) {
                        WalletOperationInfo info = deserializeFrom(column.getValue(),
                                WalletOperationInfo.class);
                        result.add(info);
                    } else {
                        LOG.warn("getRecords: unexpected column name: " + column.getName() +
                                ", key=" + row.getKey());
                    }
                }
            }
        }
        StatisticsManager.getInstance().updateRequestStatistics(getClass().getSimpleName() + " getRecords",
                System.currentTimeMillis() - now);
        return result;
    }
*/

    public Iterable<WalletOperationInfo> getRecordsByDay(Date day)
            throws CommonException {
        long now = System.currentTimeMillis();

        Iterable<WalletOperationInfo> result =
                getAsIterableSkipNull(new String[] { SERIALIZED_COLUMN_NAME, JSON_COLUMN_NAME }, WalletOperationInfo.class, "getRecordsByDay",
                        eq(DAY_FIELD, getDay(day)));
        StatisticsManager.getInstance().updateRequestStatistics(getClass().getSimpleName() + " getRecords",
                System.currentTimeMillis() - now);
        return result;
    }

    public void persist(AccountInfo accountInfo, CommonWalletOperation operation) {
        WalletOperationInfo info = new WalletOperationInfo(operation.getId(), operation.getAccountId(),
                accountInfo.getBankId(), accountInfo.getSubCasinoId(), operation.getGameSessionId(),
                operation.getRoundId(), operation.getAmount(), operation.getStartTime(), operation.getEndTime(),
                operation.getType(), operation.getExternalStatus(), operation.getInternalStatus(),
                operation.getExternalTransactionId(), operation.getExternalRoundId(), operation.getExternalSessionId(),
                operation.getDescription(), operation.getNegativeBet());
        persist(info, getTtl());
    }

    public void persist(BankInfo bankInfo, AccountInfo accountInfo, CommonWalletOperation operation, long gameId, long balance) {
        WalletOperationInfo info = new WalletOperationInfo(operation.getId(), operation.getAccountId(),
                accountInfo.getBankId(), accountInfo.getSubCasinoId(), operation.getGameSessionId(),
                operation.getRoundId(), operation.getAmount(), operation.getStartTime(), operation.getEndTime(),
                operation.getType(), operation.getExternalStatus(), operation.getInternalStatus(),
                operation.getExternalTransactionId(), operation.getExternalRoundId(), operation.getExternalSessionId(),
                operation.getDescription(), operation.getNegativeBet());
        info.setGameId(gameId);
        info.setBalance(balance);
        Integer ttl = bankInfo.getWalletOperationTtl();
        persist(info, ttl == null ? getTtl() : ttl);
    }


    public void update(BankInfo bankInfo, AccountInfo accountInfo, CommonWalletOperation operation) {
        WalletOperationInfo stored = getById(operation.getId());
        if (stored != null) {
            WalletOperationInfo info = new WalletOperationInfo(operation.getId(), operation.getAccountId(),
                    accountInfo.getBankId(), accountInfo.getSubCasinoId(), operation.getGameSessionId(),
                    operation.getRoundId(), operation.getAmount(), operation.getStartTime(), operation.getEndTime(),
                    operation.getType(), operation.getExternalStatus(), operation.getInternalStatus(),
                    operation.getExternalTransactionId(), operation.getExternalRoundId(), operation.getExternalSessionId(),
                    operation.getDescription(), operation.getNegativeBet());
            info.setGameId(stored.getGameId());
            info.setBalance(stored.getBalance());
            info.setHasRefund(stored.hasRefund());
            Integer ttl = bankInfo.getWalletOperationTtl();
            persist(info, ttl == null ? getTtl() : ttl);
        } else {
            LOG.warn("Unable to update operation with id={}", operation.getId());
        }
    }

    public void update(BankInfo bankInfo, AccountInfo accountInfo, CommonWalletOperation operation, boolean refunded) {
        WalletOperationInfo stored = getById(operation.getId());
        if (stored != null) {
            WalletOperationInfo info = new WalletOperationInfo(operation.getId(), operation.getAccountId(),
                    accountInfo.getBankId(), accountInfo.getSubCasinoId(), operation.getGameSessionId(),
                    operation.getRoundId(), operation.getAmount(), operation.getStartTime(), operation.getEndTime(),
                    operation.getType(), operation.getExternalStatus(), operation.getInternalStatus(),
                    operation.getExternalTransactionId(), operation.getExternalRoundId(), operation.getExternalSessionId(),
                    operation.getDescription(), operation.getNegativeBet());
            info.setGameId(stored.getGameId());
            info.setBalance(stored.getBalance());
            info.setHasRefund(refunded);
            Integer ttl = bankInfo.getWalletOperationTtl();
            persist(info, ttl == null ? getTtl() : ttl);
        } else {
            LOG.warn("Unable to update operation with id={}", operation.getId());
        }
    }

    public void delete(long walletOperationId) {
        super.deleteWithCheck(walletOperationId);
    }

    public void delete(long... walletOperationIds) {
        if (walletOperationIds.length == 0) {
            return;
        }
        com.abs.casino.cassandra.persist.engine.Statement query =
                com.abs.casino.cassandra.persist.engine.Statement.of(Cql.delete().
                        from(getMainColumnFamilyName()).
                        where(Cql.in(KEY, walletOperationIds)));
        execute(query, "delete walletOperations");
    }


    private long getDay(long time) {
        return getDay(new Date(time));
    }

    private long getDay(Date time) {
        return CalendarUtils.getEndDay(time, "GMT").getTimeInMillis();
    }
}
