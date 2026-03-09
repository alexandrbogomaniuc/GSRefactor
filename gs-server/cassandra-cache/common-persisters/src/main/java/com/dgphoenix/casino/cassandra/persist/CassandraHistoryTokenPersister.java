package com.abs.casino.cassandra.persist;

import com.abs.casino.cassandra.persist.engine.Cql;

import com.abs.casino.cassandra.persist.engine.AbstractCassandraPersister;
import com.abs.casino.cassandra.persist.engine.ColumnDefinition;
import com.abs.casino.cassandra.persist.engine.ResultSet;
import com.abs.casino.cassandra.persist.engine.Row;
import com.abs.casino.cassandra.persist.engine.TableDefinition;
import com.abs.casino.common.cache.BankInfoCache;
import com.abs.casino.common.cache.data.bank.BankInfo;
import com.abs.casino.common.web.statistics.StatisticsManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import static com.abs.casino.cassandra.persist.engine.CassandraDataTypes.bigint;
import static com.abs.casino.cassandra.persist.engine.CassandraDataTypes.text;

public class CassandraHistoryTokenPersister extends AbstractCassandraPersister<String, String> {

    private static final Logger LOG = LogManager.getLogger(CassandraHistoryTokenPersister.class);

    private static final String CF_NAME = "HistoryTokenCF";
    private static final String TOKEN_FIELD = "HistoryToken";
    private static final String ROUND_ID_FIELD = "RoundId";
    private static final String EXP_TIME = "ExpTime";


    private static final TableDefinition TABLE = new TableDefinition(CF_NAME,
            Arrays.asList(
                    new ColumnDefinition(TOKEN_FIELD, text(), false, false, true),
                    new ColumnDefinition(ROUND_ID_FIELD, bigint(), false, false, false),
                    new ColumnDefinition(EXP_TIME, bigint(), false, false, false)
            ), TOKEN_FIELD);

    @Override
    public TableDefinition getMainTableDefinition() {
        return TABLE;
    }

    @Override
    public Logger getLog() {
        return LOG;
    }

    public void persist(long bankId, long roundId, String token) {
        persist(bankId, roundId, token, null);
    }

    public void persist(long bankId, long roundId, String token, Integer ttlSeconds) {
        long startTime = System.currentTimeMillis();
        BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(bankId);
        int ttl = (ttlSeconds != null) ? (int) TimeUnit.SECONDS.toMillis(ttlSeconds) : bankInfo.getHistoryTokenTTL();
        com.datastax.driver.core.Statement insert = Cql.insertInto(TABLE.getTableName())
                .value(TOKEN_FIELD, token)
                .value(ROUND_ID_FIELD, roundId)
                .value(EXP_TIME, ((ttl != 0) ? ttl + startTime : Long.MAX_VALUE));
        execute(insert, "persist");
        StatisticsManager.getInstance().updateRequestStatistics(getClass().getSimpleName() + " persist",
                System.currentTimeMillis() - startTime);
    }

    public Long getRoundId(String token) {
        long now = System.currentTimeMillis();
        com.datastax.driver.core.Statement select = getSelectColumnsQuery(ROUND_ID_FIELD, EXP_TIME)
                .where(eq(TOKEN_FIELD, token));
        ResultSet resultSet = executeWrapped(select, "getRoundId");
        Row row = resultSet.one();
        if (row != null) {
            long expTime = row.getLong(EXP_TIME);
            if (expTime > now) {
                StatisticsManager.getInstance().updateRequestStatistics(getClass().getSimpleName() + " getRoundId",
                        System.currentTimeMillis() - now);
                return row.getLong(ROUND_ID_FIELD);
            }
        }
        StatisticsManager.getInstance().updateRequestStatistics(getClass().getSimpleName() + " getRoundId",
                System.currentTimeMillis() - now);
        return null;
    }
}
