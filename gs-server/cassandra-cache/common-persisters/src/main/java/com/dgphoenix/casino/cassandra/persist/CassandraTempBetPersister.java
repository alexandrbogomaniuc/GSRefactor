package com.abs.casino.cassandra.persist;

import com.abs.casino.cassandra.persist.engine.AbstractCassandraPersister;
import com.abs.casino.cassandra.persist.engine.ColumnDefinition;
import com.abs.casino.cassandra.persist.engine.TableDefinition;
import static com.abs.casino.cassandra.persist.engine.CassandraDataTypes.*;
import com.abs.casino.cassandra.persist.engine.configuration.Caching;
import com.abs.casino.cassandra.persist.engine.configuration.CompactionStrategy;
import com.abs.casino.common.cache.data.bet.PlayerBet;
import com.abs.casino.common.web.statistics.StatisticsManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * User: flsh
 * Date: 20.10.11
 */
public class CassandraTempBetPersister extends AbstractCassandraPersister<Long, Long> {
    public static final String COLUMN_FAMILY_NAME = "BetCfTmp";
    public static final String GAME_SESSION_ID_FIELD = "SID";
    public static final String BET_ID_FIELD = "BID";
    private static final Logger LOG = LogManager.getLogger(CassandraTempBetPersister.class);

    private static final TableDefinition TABLE = new TableDefinition(COLUMN_FAMILY_NAME,
            Arrays.asList(
                    new ColumnDefinition(GAME_SESSION_ID_FIELD, bigint(), false, false, true),
                    new ColumnDefinition(BET_ID_FIELD, cint(), false, false, true),
                    new ColumnDefinition(SERIALIZED_COLUMN_NAME, blob()),
                    new ColumnDefinition(JSON_COLUMN_NAME, text())
            ), GAME_SESSION_ID_FIELD)
            .caching(Caching.NONE)
            .compaction(CompactionStrategy.getSizeTired(true, TimeUnit.HOURS.toSeconds(1), 0.0))
            .gcGraceSeconds(TimeUnit.DAYS.toSeconds(2));

    private CassandraTempBetPersister() {
    }

    @Override
    protected String getKeyColumnName() {
        return GAME_SESSION_ID_FIELD;
    }

    public PlayerBet getPlayerBet(long gameSessionId, long betId) {
        long now = System.currentTimeMillis();
        com.datastax.driver.core.Statement query = com.datastax.driver.core.querybuilder.QueryBuilder.select(SERIALIZED_COLUMN_NAME, JSON_COLUMN_NAME)
                .from(getMainColumnFamilyName())
                .where(eq(GAME_SESSION_ID_FIELD, gameSessionId))
                .and(eq(BET_ID_FIELD, (int) betId));
        com.datastax.driver.core.ResultSet resultSet = execute(query, "getPlayerBet");
        com.datastax.driver.core.Row row = resultSet.one();

        PlayerBet playerBet = null;
        if (row != null) {
            playerBet = TABLE.deserializeFromJson(row.getString(JSON_COLUMN_NAME),
                    PlayerBet.class);
            if (playerBet == null) {
                playerBet = TABLE.deserializeFrom(row.getBytes(SERIALIZED_COLUMN_NAME),
                        PlayerBet.class);
            }
        }
        StatisticsManager.getInstance().updateRequestStatistics("CassandraTempBetPersister: getPlayerBet",
                System.currentTimeMillis() - now);
        return playerBet;
    }

    com.datastax.driver.core.ResultSet getResultSetByGameSessionId(long gameSessionId) {
        com.datastax.driver.core.Statement query = com.datastax.driver.core.querybuilder.QueryBuilder.select(BET_ID_FIELD, SERIALIZED_COLUMN_NAME, JSON_COLUMN_NAME)
                .from(COLUMN_FAMILY_NAME)
                .where(eq(GAME_SESSION_ID_FIELD, gameSessionId));
        return execute(query, "getResultSetByGameSessionId get from temp Table");
    }

    public List<PlayerBet> getOnlinePayerBets(long gameSessionId) {
        Map<Integer, PlayerBet> betsMap = new HashMap<>();
        com.datastax.driver.core.ResultSet resultSet = getResultSetByGameSessionId(gameSessionId);
        for (com.datastax.driver.core.Row row : resultSet) {
            if (row != null) {
                int betId = row.getInt(BET_ID_FIELD);
                if (!betsMap.containsKey(betId)) {
                    PlayerBet playerBet = getMainTableDefinition().
                            deserializeFromJson(row.getString(JSON_COLUMN_NAME),
                                    PlayerBet.class);

                    if (playerBet == null) {
                        playerBet = getMainTableDefinition().
                                deserializeFrom(row.getBytes(SERIALIZED_COLUMN_NAME),
                                        PlayerBet.class);
                    }
                    if (playerBet != null) {
                        betsMap.put(betId, playerBet);
                    }
                } else if (LOG.isDebugEnabled()) {
                    LOG.error("!!! Incorrect playerBet with roundId > then max (gameSession.lastPlayerBetId+1). " +
                            "BetId=" + betId + ", gameSessionId=" + gameSessionId + " playerBet=" + getMainTableDefinition().
                            deserializeFrom(row.getBytes(SERIALIZED_COLUMN_NAME), PlayerBet.class));
                }
            }
        }
        return new ArrayList<>(betsMap.values());
    }

    com.datastax.driver.core.ResultSet getResultSetByGameSessionIdAndRounds(long gameSessionId, Set<Long> betIds) {
        com.datastax.driver.core.Statement query = com.datastax.driver.core.querybuilder.QueryBuilder.select(SERIALIZED_COLUMN_NAME, JSON_COLUMN_NAME)
                .from(COLUMN_FAMILY_NAME)
                .where(eq(GAME_SESSION_ID_FIELD, gameSessionId))
                .and(com.datastax.driver.core.querybuilder.QueryBuilder.in(BET_ID_FIELD, betIds.toArray()));
        return execute(query, "getResultSetByGameSessionIdAndRounds from temp Table");
    }

    void addDeleteStatement(Map<com.datastax.driver.core.Session, List<com.datastax.driver.core.Statement>> statementsMap, long gameSessionId) {
        List<com.datastax.driver.core.Statement> statements = getOrCreateStatements(statementsMap);
        statements.add(addItemDeletion(COLUMN_FAMILY_NAME, gameSessionId));
    }

    public void prepareToPersistBet(Map<com.datastax.driver.core.Session, List<com.datastax.driver.core.Statement>> statementsMap, long gameSessionId, PlayerBet bet,
                                    List<ByteBuffer> byteBuffersCollector) {
        if (bet == null) {
            getLog().warn("persist: empty bet, gameSessionId={}", gameSessionId);
            return;
        }
        List<com.datastax.driver.core.Statement> statements = getOrCreateStatements(statementsMap);
        String json = TABLE.serializeToJson(bet);
        ByteBuffer bytes = TABLE.serializeToBytes(bet);
        byteBuffersCollector.add(bytes);
        statements.add(getInsertQuery(TABLE, null).
                value(GAME_SESSION_ID_FIELD, gameSessionId).
                value(BET_ID_FIELD, (int) bet.getId()).
                value(SERIALIZED_COLUMN_NAME, bytes).
                value(JSON_COLUMN_NAME, json));
    }

    @Override
    public TableDefinition getMainTableDefinition() {
        return TABLE;
    }

    @Override
    public Logger getLog() {
        return LOG;
    }
}
