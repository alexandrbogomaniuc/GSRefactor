package com.abs.casino.cassandra.persist.mp;

import com.abs.casino.cassandra.persist.engine.AbstractCassandraPersister;
import com.abs.casino.cassandra.persist.engine.ColumnDefinition;
import com.abs.casino.cassandra.persist.engine.ResultSet;
import com.abs.casino.cassandra.persist.engine.Row;
import com.abs.casino.cassandra.persist.engine.TableDefinition;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.abs.casino.cassandra.persist.engine.CassandraDataTypes.bigint;
import static com.abs.casino.cassandra.persist.engine.CassandraDataTypes.varchar;

public class LeaderboardResultPersister extends AbstractCassandraPersister<Long, String> {
    private static final Logger LOG = LogManager.getLogger(LeaderboardResultPersister.class);

    private static final String CF_NAME = "LeaderboardResult";
    private static final String BANK_ID_COLUMN = "b";
    private static final String LEADERBOARD_ID_COLUMN = "l";
    private static final String START_DATE_COLUMN = "s";
    private static final String END_DATE_COLUMN = "e";
    private static final String RESULT_COLUMN = "r";

    private static final TableDefinition RESULTS_TABLE = new TableDefinition(CF_NAME,
            Arrays.asList(
                    new ColumnDefinition(BANK_ID_COLUMN, bigint(), false, false, true),
                    new ColumnDefinition(LEADERBOARD_ID_COLUMN, bigint(), false, false, true),
                    new ColumnDefinition(END_DATE_COLUMN, bigint(), false, true, false),
                    new ColumnDefinition(START_DATE_COLUMN, bigint()),
                    new ColumnDefinition(RESULT_COLUMN, varchar())
            ), BANK_ID_COLUMN);

    public void persist(long leaderboardId, long bankId, long startDate, long endDate, String result) {
        com.datastax.driver.core.Statement query = getInsertQuery()
                .value(BANK_ID_COLUMN, bankId)
                .value(LEADERBOARD_ID_COLUMN, leaderboardId)
                .value(START_DATE_COLUMN, startDate)
                .value(END_DATE_COLUMN, endDate)
                .value(RESULT_COLUMN, result);

        execute(query, "persist");
    }

    public List<LeaderboardInfo> getLeaderboards(long bankId) {
        com.datastax.driver.core.Statement query = getSelectColumnsQuery(LEADERBOARD_ID_COLUMN, START_DATE_COLUMN, END_DATE_COLUMN)
                .where(eq(BANK_ID_COLUMN, bankId));
        ResultSet result = executeWrapped(query, "getLeaderboardIds");
        List<LeaderboardInfo> leaderboards = new ArrayList<>();
        if (result != null) {
            for (Row row : result) {
                leaderboards.add(new LeaderboardInfo(
                        row.getLong(LEADERBOARD_ID_COLUMN),
                        row.getLong(START_DATE_COLUMN),
                        row.getLong(END_DATE_COLUMN)));
            }
        }
        return leaderboards;
    }

    public String getLeaderboardResult(long bankId, long leaderboardId) {
        com.datastax.driver.core.Statement query = getSelectColumnsQuery(RESULT_COLUMN)
                .where(eq(BANK_ID_COLUMN, bankId))
                .and(eq(LEADERBOARD_ID_COLUMN, leaderboardId))
                .limit(1);
        Row row = executeWrapped(query, "getLeaderboardResult").one();
        if (row != null) {
            return row.getString(RESULT_COLUMN);
        }
        return "";
    }

    @Override
    public TableDefinition getMainTableDefinition() {
        return RESULTS_TABLE;
    }

    @Override
    public Logger getLog() {
        return LOG;
    }
}
