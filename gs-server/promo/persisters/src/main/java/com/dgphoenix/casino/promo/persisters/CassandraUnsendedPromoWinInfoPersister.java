package com.dgphoenix.casino.promo.persisters;

import com.datastax.driver.core.DataType;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.dgphoenix.casino.cassandra.persist.engine.AbstractCassandraPersister;
import com.dgphoenix.casino.cassandra.persist.engine.ColumnDefinition;
import com.dgphoenix.casino.cassandra.persist.engine.TableDefinition;
import com.dgphoenix.casino.cassandra.persist.engine.configuration.Caching;
import com.dgphoenix.casino.cassandra.persist.engine.configuration.CompactionStrategy;
import com.dgphoenix.casino.common.promo.PromoWinInfo;
import com.dgphoenix.casino.common.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * User: flsh
 * Date: 27.10.2021.
 */
public class CassandraUnsendedPromoWinInfoPersister extends AbstractCassandraPersister<Long, Long> {
    private static final Logger LOG = LogManager.getLogger(CassandraUnsendedPromoWinInfoPersister.class);
    private static final String TABLE_NAME = "usendedPwiCF";
    private static final String GAME_SESSION_ID = "gameSessionId";
    private static final String WIN_DATE = "winDate";
    private static final String ROUND_ID = "roundId";

    private static final TableDefinition TABLE = new TableDefinition(TABLE_NAME,
            Arrays.asList(
                    new ColumnDefinition(GAME_SESSION_ID, DataType.bigint(), false, false, true),
                    new ColumnDefinition(WIN_DATE, DataType.bigint(), false, false, true),
                    new ColumnDefinition(ROUND_ID, DataType.bigint(), false, true, false),
                    new ColumnDefinition(SERIALIZED_COLUMN_NAME, DataType.blob()),
                    new ColumnDefinition(JSON_COLUMN_NAME, DataType.text())
            ), GAME_SESSION_ID)
            .compaction(CompactionStrategy.LEVELED)
            .gcGraceSeconds(TimeUnit.DAYS.toMillis(1))
            .caching(Caching.NONE);

    public void persist(long gameSessionId, long roundId, PromoWinInfo winInfo) {
        ByteBuffer buffer = TABLE.serializeToBytes(winInfo);
        String json = TABLE.serializeToJson(winInfo);
        try {
            execute(getInsertQuery()
                    .value(GAME_SESSION_ID, gameSessionId)
                    .value(WIN_DATE, winInfo.getDate())
                    .value(ROUND_ID, roundId)
                    .value(SERIALIZED_COLUMN_NAME, buffer)
                    .value(JSON_COLUMN_NAME, json), "persist");
        } finally {
            releaseBuffer(buffer);
        }
    }

    public List<PromoWinInfo> getForGameSession(long gameSessionId) {
        com.datastax.driver.core.ResultSet resultSet = execute(getSelectColumnsQuery(SERIALIZED_COLUMN_NAME, JSON_COLUMN_NAME)
                .where(eq(GAME_SESSION_ID, gameSessionId)), "getForGameSession");
        List<PromoWinInfo> wins = new ArrayList<>();
        for (com.datastax.driver.core.Row row : resultSet) {
            PromoWinInfo info = TABLE.deserializeFromJson(
                    row.getString(JSON_COLUMN_NAME), PromoWinInfo.class);

            if (info == null) {
                ByteBuffer bytes = row.getBytes(SERIALIZED_COLUMN_NAME);
                if (bytes != null) {
                   info = TABLE.deserializeFrom(bytes, PromoWinInfo.class);
                }
            }
            if (info != null) {
                wins.add(info);
            }
        }
        return wins;
    }

    public PromoWinInfo getByRoundId(long roundId) {
        com.datastax.driver.core.Row row = execute(getSelectColumnsQuery(SERIALIZED_COLUMN_NAME, JSON_COLUMN_NAME)
                .where(eq(ROUND_ID, roundId)), "getByRoundId").one();
        if (row != null) {
            PromoWinInfo info = 
                    TABLE.deserializeFromJson(row.getString(JSON_COLUMN_NAME), PromoWinInfo.class);
            if (info == null) {
                info = TABLE.deserializeFrom(row.getBytes(SERIALIZED_COLUMN_NAME), PromoWinInfo.class);
            }
            return info;
        }
        return null;
    }

    public void remove(long gameSessionId) {
        LOG.debug("remove: gameSessionId={}", gameSessionId);
        execute(QueryBuilder.delete()
                .from(getMainColumnFamilyName())
                .where(eq(GAME_SESSION_ID, gameSessionId)), "remove");
    }

    public void remove(long gameSessionId, long date) {
        LOG.debug("remove gameSessionId={}, date={}", gameSessionId, date);
        execute(QueryBuilder.delete()
                .from(getMainColumnFamilyName())
                .where(eq(GAME_SESSION_ID, gameSessionId))
                .and(eq(WIN_DATE, date)), "remove[date]");
    }

    public void removeByRoundId(long roundId) {
        LOG.debug("remove: roundId={}", roundId);
        com.datastax.driver.core.ResultSet resultSet = execute(getSelectColumnsQuery(GAME_SESSION_ID, WIN_DATE)
                .where(eq(ROUND_ID, roundId)), "getAllRecordsByRoundId");
        Set<Pair<Long, Long>> pairs = new HashSet<>();
        for (com.datastax.driver.core.Row row : resultSet) {
            pairs.add(new Pair<>(row.getLong(GAME_SESSION_ID), row.getLong(WIN_DATE)));
        }
        LOG.debug("removeByRoundId: found records for remove={}", pairs.size());
        for (Pair<Long, Long> pair : pairs) {
            remove(pair.getKey(), pair.getValue());
        }
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
