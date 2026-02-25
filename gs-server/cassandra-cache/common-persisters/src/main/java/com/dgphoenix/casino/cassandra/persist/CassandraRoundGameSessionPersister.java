package com.dgphoenix.casino.cassandra.persist;

import com.datastax.driver.core.DataType;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.dgphoenix.casino.cassandra.persist.engine.AbstractCassandraPersister;
import com.dgphoenix.casino.cassandra.persist.engine.ColumnDefinition;
import com.dgphoenix.casino.cassandra.persist.engine.TableDefinition;
import com.dgphoenix.casino.common.cache.data.session.GameSession;
import com.dgphoenix.casino.common.util.Triple;
import com.google.common.collect.Lists;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Created by inter on 18.06.15.
 */
public class CassandraRoundGameSessionPersister extends AbstractCassandraPersister<Long, Long> {
    private static final Logger LOG = LogManager.getLogger(CassandraRoundGameSessionPersister.class);

    private static final String COLUMN_FAMILY_NAME = "RoundGameSessionCF";
    private static final String ROUND_ID_FIELD = "ROUND_ID";
    private static final String GAME_SID_FIELD = "GAME_SID";
    private static final String GAME_ID_FIELD = "GAME_ID";
    private static final String ACCOUNT_ID_FIELD = "ACCOUNT_ID";
    private static final String WRITE_TIME = "WRITE_TIME";
    private static final TableDefinition TABLE = new TableDefinition(COLUMN_FAMILY_NAME,
            Arrays.asList(
                    new ColumnDefinition(ROUND_ID_FIELD, DataType.bigint(), false, false, true),
                    new ColumnDefinition(GAME_SID_FIELD, DataType.bigint(), false, false, true),
                    new ColumnDefinition(GAME_ID_FIELD, DataType.bigint(), true, false, false),
                    new ColumnDefinition(ACCOUNT_ID_FIELD, DataType.bigint(), true, false, false),
                    new ColumnDefinition(WRITE_TIME, DataType.bigint(), false, false, false)
            ), ROUND_ID_FIELD);

    private static final Comparator<Row> sortComparator = (o1, o2) -> (int) (o1.getLong(WRITE_TIME) - o2.getLong(WRITE_TIME));

    protected CassandraRoundGameSessionPersister() {
    }

    @Override
    protected String getKeyColumnName() {
        return ROUND_ID_FIELD;
    }

    @Override
    public TableDefinition getMainTableDefinition() {
        return TABLE;
    }

    @Override
    public Logger getLog() {
        return LOG;
    }

    public void persist(long roundId, GameSession gameSessionId) {
        Statement query = getInsertQuery()
                .value(ROUND_ID_FIELD, roundId)
                .value(GAME_SID_FIELD, gameSessionId.getId())
                .value(GAME_ID_FIELD, gameSessionId.getGameId())
                .value(ACCOUNT_ID_FIELD, gameSessionId.getAccountId())
                .value(WRITE_TIME, System.currentTimeMillis());
        execute(query, "create");
    }

    public Triple<List<Long>, Long, Long> getGameSessionsByRoundId(long roundId) {
        Statement query = QueryBuilder.select()
                .column(GAME_SID_FIELD)
                .column(WRITE_TIME)
                .column(GAME_ID_FIELD)
                .column(ACCOUNT_ID_FIELD)
                .from(COLUMN_FAMILY_NAME)
                .where(QueryBuilder.eq(ROUND_ID_FIELD, roundId));
        ResultSet resultSet = execute(query, "getGameSessionsByRoundId");
        List<Row> rows = Lists.newArrayList(resultSet);
        if (!rows.isEmpty()) {
            rows.sort(sortComparator);
            List<Long> result = new ArrayList<>(rows.size());
            for (Row row : rows) {
                result.add(row.getLong(GAME_SID_FIELD));
            }
            return new Triple<>(result,
                    rows.get(0).getLong(GAME_ID_FIELD), rows.get(0).getLong(ACCOUNT_ID_FIELD));
        }
        return null;
    }

    public boolean remove(long roundId) {
        return deleteWithCheck(roundId);
    }
}
