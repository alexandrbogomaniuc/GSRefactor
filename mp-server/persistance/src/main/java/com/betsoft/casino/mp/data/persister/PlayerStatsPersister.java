package com.betsoft.casino.mp.data.persister;

import com.betsoft.casino.mp.model.IPlayerStats;
import com.betsoft.casino.mp.model.PlayerStats;
import com.betsoft.casino.mp.service.IPlayerStatsService;
import com.dgphoenix.casino.cassandra.persist.engine.AbstractCassandraPersister;
import com.dgphoenix.casino.cassandra.persist.engine.ColumnDefinition;
import com.dgphoenix.casino.cassandra.persist.engine.TableDefinition;
import com.dgphoenix.casino.cassandra.persist.engine.configuration.CompactionStrategy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class PlayerStatsPersister extends AbstractCassandraPersister<Long, String> implements IPlayerStatsService<PlayerStats> {
    private static final Logger LOG = LogManager.getLogger(PlayerStatsPersister.class);

    private static final String CF_NAME = "Players";
    private static final String BANK_ID_COLUMN = "bid";
    private static final String GAME_ID_COLUMN = "gid";
    private static final String ACCOUNT_ID_COLUMN = "aid";
    private static final String VERSION_COLUMN = "v";
    private static final String SERIALIZED_COLUMN_NAME = "scn";

    private static final String TOURNAMENT_CF_NAME = "TournamentPlayers";
    private static final String TOURNAMENT_ID_COLUMN = "tid";

    private static final TableDefinition TABLE = new TableDefinition(CF_NAME,
            Arrays.asList(
                    new ColumnDefinition(BANK_ID_COLUMN, com.datastax.driver.core.DataType.bigint(), false, false, true),
                    new ColumnDefinition(GAME_ID_COLUMN, com.datastax.driver.core.DataType.bigint(), false, false, true),
                    new ColumnDefinition(ACCOUNT_ID_COLUMN, com.datastax.driver.core.DataType.bigint(), false, false, true),
                    new ColumnDefinition(VERSION_COLUMN, com.datastax.driver.core.DataType.bigint()),
                    new ColumnDefinition(SERIALIZED_COLUMN_NAME, com.datastax.driver.core.DataType.blob()),
                    new ColumnDefinition(JSON_COLUMN_NAME, com.datastax.driver.core.DataType.text())
            ), BANK_ID_COLUMN, GAME_ID_COLUMN, ACCOUNT_ID_COLUMN)
            .compaction(CompactionStrategy.LEVELED);

    private static final TableDefinition TOURNAMENT_TABLE = new TableDefinition(TOURNAMENT_CF_NAME,
            Arrays.asList(
                    new ColumnDefinition(TOURNAMENT_ID_COLUMN, com.datastax.driver.core.DataType.bigint(), false, false, true),
                    new ColumnDefinition(BANK_ID_COLUMN, com.datastax.driver.core.DataType.bigint(), false, false, true),
                    new ColumnDefinition(GAME_ID_COLUMN, com.datastax.driver.core.DataType.bigint(), false, false, true),
                    new ColumnDefinition(ACCOUNT_ID_COLUMN, com.datastax.driver.core.DataType.bigint(), false, false, true),
                    new ColumnDefinition(VERSION_COLUMN, com.datastax.driver.core.DataType.bigint()),
                    new ColumnDefinition(SERIALIZED_COLUMN_NAME, com.datastax.driver.core.DataType.blob()),
                    new ColumnDefinition(JSON_COLUMN_NAME, com.datastax.driver.core.DataType.text())
            ), TOURNAMENT_ID_COLUMN, BANK_ID_COLUMN, GAME_ID_COLUMN, ACCOUNT_ID_COLUMN)
            .compaction(CompactionStrategy.LEVELED);

    @Override
    public PlayerStats addStats(long bankId, long gameId, long accountId, PlayerStats diff) {
        PlayerStats stored = load(bankId, gameId, accountId);
        if (stored.getVersion() == 0 && insert(bankId, gameId, accountId, diff)) {
            return diff;
        }
        PlayerStats combined = combine(stored, diff);
        while (!update(bankId, gameId, accountId, combined)) {
            LOG.debug("Retrying stats update after collision for {}", accountId);
            stored = load(bankId, gameId, accountId);
            combined = combine(stored, diff);
        }
        return combined;
    }

    private boolean insert(long bankId, long gameId, long accountId, IPlayerStats stats) {
        ByteBuffer buffer = TABLE.serializeToBytes(stats);
        String json = TABLE.serializeToJson(stats);
        try {
            return execute(getInsertQuery()
                            .value(BANK_ID_COLUMN, bankId)
                            .value(GAME_ID_COLUMN, gameId)
                            .value(ACCOUNT_ID_COLUMN, accountId)
                            .value(VERSION_COLUMN, stats.getVersion())
                            .value(SERIALIZED_COLUMN_NAME, buffer)
                            .value(JSON_COLUMN_NAME, json)
                            .ifNotExists(),
                    "insert").wasApplied();
        } finally {
            releaseBuffer(buffer);
        }
    }

    private boolean update(long bankId, long gameId, long accountId, PlayerStats stats) {
        ByteBuffer buffer = TABLE.serializeToBytes(stats);
        String json = TABLE.serializeToJson(stats);
        try {
            return execute(getUpdateQuery()
                            .where()
                            .and(eq(BANK_ID_COLUMN, bankId))
                            .and(eq(GAME_ID_COLUMN, gameId))
                            .and(eq(ACCOUNT_ID_COLUMN, accountId))
                            .with(set(SERIALIZED_COLUMN_NAME, buffer))
                            .and(set(VERSION_COLUMN, stats.getVersion()))
                            .and(set(JSON_COLUMN_NAME, json))
                            .onlyIf(eq(VERSION_COLUMN, stats.getVersion() - 1)),
                    "update").wasApplied();
        } finally {
            releaseBuffer(buffer);
        }
    }

    @Override
    public PlayerStats addTournamentStats(long tournamentId, long bankId, long gameId, long accountId, PlayerStats diff) {
        PlayerStats stored = loadTournamentStats(tournamentId, bankId, gameId, accountId);
        if (stored.getVersion() == 0 && insertTournamentStats(tournamentId, bankId, gameId, accountId, diff)) {
            return diff;
        }
        PlayerStats combined = combine(stored, diff);
        while (!updateTournamentStats(tournamentId, bankId, gameId, accountId, combined)) {
            LOG.debug("Retrying stats update after collision for {}", accountId);
            stored = load(bankId, gameId, accountId);
            combined = combine(stored, diff);
        }
        return combined;
    }

    private boolean insertTournamentStats(long tournamentId, long bankId, long gameId, long accountId, IPlayerStats stats) {
        ByteBuffer buffer = TOURNAMENT_TABLE.serializeToBytes(stats);
        String json = TOURNAMENT_TABLE.serializeToJson(stats);
        try {
            return execute(getInsertQuery()
                            .value(TOURNAMENT_ID_COLUMN, tournamentId)
                            .value(BANK_ID_COLUMN, bankId)
                            .value(GAME_ID_COLUMN, gameId)
                            .value(ACCOUNT_ID_COLUMN, accountId)
                            .value(VERSION_COLUMN, stats.getVersion())
                            .value(SERIALIZED_COLUMN_NAME, buffer)
                            .value(JSON_COLUMN_NAME, json)
                            .ifNotExists(),
                    "insert").wasApplied();
        } finally {
            releaseBuffer(buffer);
        }
    }

    private boolean updateTournamentStats(long tournamentId, long bankId, long gameId, long accountId, PlayerStats stats) {
        ByteBuffer buffer = TOURNAMENT_TABLE.serializeToBytes(stats);
        String json = TOURNAMENT_TABLE.serializeToJson(stats);
        try {
            return execute(getUpdateQuery()
                            .where()
                            .and(eq(TOURNAMENT_ID_COLUMN, tournamentId))
                            .and(eq(BANK_ID_COLUMN, bankId))
                            .and(eq(GAME_ID_COLUMN, gameId))
                            .and(eq(ACCOUNT_ID_COLUMN, accountId))
                            .with(set(SERIALIZED_COLUMN_NAME, buffer))
                            .and(set(JSON_COLUMN_NAME, json))
                            .and(set(VERSION_COLUMN, stats.getVersion()))
                            .onlyIf(eq(VERSION_COLUMN, stats.getVersion() - 1)),
                    "update").wasApplied();
        } finally {
            releaseBuffer(buffer);
        }
    }

    @Override
    public PlayerStats load(long bankId, long gameId, long accountId) {
        com.datastax.driver.core.Row result = execute(getSelectAllColumnsQuery(TABLE)
                        .where()
                        .and(eq(BANK_ID_COLUMN, bankId))
                        .and(eq(GAME_ID_COLUMN, gameId))
                        .and(eq(ACCOUNT_ID_COLUMN, accountId))
                        .limit(1),
                "getStats").one();
        if (result == null) {
            return new PlayerStats();
        }

        PlayerStats playerStats = TABLE
                .deserializeFromJson(result.getString(JSON_COLUMN_NAME), PlayerStats.class);
        if (playerStats == null) {
            playerStats = TABLE
                    .deserializeFrom(result.getBytes(SERIALIZED_COLUMN_NAME), PlayerStats.class);
        }
        return playerStats;
    }

    @Override
    public PlayerStats loadTournamentStats(long tournamentId, long bankId, long gameId, long accountId) {
        com.datastax.driver.core.Row result = execute(getSelectAllColumnsQuery(TOURNAMENT_TABLE)
                        .where()
                        .and(eq(TOURNAMENT_ID_COLUMN, tournamentId))
                        .and(eq(BANK_ID_COLUMN, bankId))
                        .and(eq(GAME_ID_COLUMN, gameId))
                        .and(eq(ACCOUNT_ID_COLUMN, accountId))
                        .limit(1),
                "getStats").one();
        if (result == null) {
            return new PlayerStats();
        }

        PlayerStats stats = TOURNAMENT_TABLE
                .deserializeFromJson(result.getString(JSON_COLUMN_NAME), PlayerStats.class);

        if (stats == null) {
            stats = TOURNAMENT_TABLE
                    .deserializeFrom(result.getBytes(SERIALIZED_COLUMN_NAME), PlayerStats.class);
        }
        return stats;
    }

    @Override
    public TableDefinition getMainTableDefinition() {
        return TABLE;
    }

    @Override
    public String getMainColumnFamilyName() {
        return CF_NAME;
    }

    @Override
    public List<TableDefinition> getAllTableDefinitions() {
        return Collections.unmodifiableList(Arrays.asList(TABLE, TOURNAMENT_TABLE));
    }

    @Override
    public Logger getLog() {
        return LOG;
    }

    private PlayerStats combine(PlayerStats stats, PlayerStats diff) {
        stats.addScore(diff.getScore().getAmount());
        stats.setRounds(stats.getRounds() + diff.getRounds());
        Map<Integer, Long> kills = stats.getKills();
        for (Map.Entry<Integer, Long> entry : diff.getKills().entrySet()) {
            kills.put(entry.getKey(), kills.getOrDefault(entry.getKey(), 0L) + entry.getValue());
        }
        Map<Integer, Long> treasures = stats.getTreasures();
        for (Map.Entry<Integer, Long> entry : diff.getTreasures().entrySet()) {
            treasures.put(entry.getKey(), treasures.getOrDefault(entry.getKey(), 0L) + entry.getValue());
        }
        stats.incrementVersion();
        return stats;
    }
}
