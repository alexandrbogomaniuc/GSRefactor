package com.abs.casino.cassandra.persist.mp;

import com.abs.casino.cassandra.persist.engine.Cql;

import com.abs.casino.cassandra.persist.engine.AbstractCassandraPersister;
import com.abs.casino.cassandra.persist.engine.CassandraDataTypes;
import com.abs.casino.cassandra.persist.engine.ColumnDefinition;
import com.abs.casino.cassandra.persist.engine.TableDefinition;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.ByteBuffer;
import java.util.*;

import static com.abs.casino.cassandra.persist.engine.CassandraDataTypes.bigint;
import static com.abs.casino.cassandra.persist.engine.CassandraDataTypes.blob;
import static com.abs.casino.cassandra.persist.engine.CassandraDataTypes.cint;
import static com.abs.casino.cassandra.persist.engine.CassandraDataTypes.text;
import com.datastax.driver.core.querybuilder.Batch;
import com.datastax.driver.core.querybuilder.Delete;
import com.datastax.driver.core.querybuilder.Insert;
import com.datastax.driver.core.querybuilder.Select;
import com.abs.casino.cassandra.persist.engine.SchemaCql.Direction;


public class BattlegroundHistoryPersister extends AbstractCassandraPersister<Long, String> {
    private static final Logger LOG = LogManager.getLogger(BattlegroundHistoryPersister.class);
    private static final String COMPLETED_STATUS = "COMPLETED";
    private static final String CANCELLED_STATUS = "CANCELLED";
    private static final int PAGE_SIZE = 100;
    private static final String CF_NAME = "BattlegroundHistory";
    private static final String ACCOUNT_ID = "accountId";
    private static final String GAME_ID = "gameId";
    private static final String ROUND_ID = "roundId";
    private static final String GAMESESSION_ID = "gameSessionId";
    private static final String DATE_TIME = "dateTime";
    private static final String SID = "SID";
    private static final String ACCOUNT_IDS = "accountIds";

    private static final String CF_PARTICIPANT_NAME = "BattlegroundParticipantRoundHistory";

    private static final TableDefinition TABLE = new TableDefinition(
            CF_NAME,
            Arrays.asList(
                    new ColumnDefinition(ACCOUNT_ID, bigint(), false, false, true),
                    new ColumnDefinition(DATE_TIME, bigint(), false, false, true),
                    new ColumnDefinition(GAME_ID, cint(), false, true, false),
                    new ColumnDefinition(ROUND_ID, bigint(), false, true, false),
                    new ColumnDefinition(GAMESESSION_ID, bigint(), false, true, false),
                    new ColumnDefinition(SERIALIZED_COLUMN_NAME, blob()),
                    new ColumnDefinition(JSON_COLUMN_NAME, text())
            ), ACCOUNT_ID)
            .clusteringOrder(DATE_TIME, Direction.DESC);

    private static final TableDefinition PARTICIPANT_ROUND_TABLE = new TableDefinition(
            CF_PARTICIPANT_NAME,
            Arrays.asList(
                    new ColumnDefinition(SID, text(), false, false, true),
                    new ColumnDefinition(ROUND_ID, bigint(), false, false, true),
                    new ColumnDefinition(GAMESESSION_ID, bigint(), false, true, false),
                    new ColumnDefinition(ACCOUNT_IDS, CassandraDataTypes.set(bigint())),
                    new ColumnDefinition(SERIALIZED_COLUMN_NAME, blob()),
                    new ColumnDefinition(JSON_COLUMN_NAME, text())
            ), SID);

    public void create(long accountId, BattlegroundRound battlegroundRound) {
        String json = TABLE.serializeToJson(battlegroundRound);
        ByteBuffer buffer = TABLE.serializeToBytes(battlegroundRound);
        Insert insert = getInsertQuery()
                .value(ACCOUNT_ID, accountId)
                .value(DATE_TIME, battlegroundRound.getDateTime())
                .value(GAME_ID, battlegroundRound.getGameId())
                .value(ROUND_ID, battlegroundRound.getRoundId())
                .value(GAMESESSION_ID, battlegroundRound.getGameSessionId())
                .value(SERIALIZED_COLUMN_NAME, buffer)
                .value(JSON_COLUMN_NAME, json);
        execute(insert, "persist");
        getLog().info("create: AccountId: {}, BattlegroundRound: {}", accountId, battlegroundRound);
    }

    public void update(long accountId, BattlegroundRound battlegroundRound) {

        Select select = getSelectColumnsQuery(DATE_TIME, GAME_ID, ROUND_ID, SERIALIZED_COLUMN_NAME, JSON_COLUMN_NAME)
                .where(eq(ACCOUNT_ID, accountId))
                .and(Cql.eq(DATE_TIME, battlegroundRound.getDateTime()))
                .limit(1);

        com.abs.casino.cassandra.persist.engine.ResultSet resultSet = executeWrapped(select, "update:: select before");

        com.abs.casino.cassandra.persist.engine.Row existRow = resultSet.one();

        if (existRow != null) {

            create(accountId, battlegroundRound);

        } else {

            getLog().error("update:: round in state 'STARTED' not found, try find by roundId, accountId={}, " +
                    "battlegroundRound={}", accountId, battlegroundRound);

            select = getSelectColumnsQuery(DATE_TIME, GAME_ID, ROUND_ID, SERIALIZED_COLUMN_NAME, JSON_COLUMN_NAME)
                    .where(eq(ACCOUNT_ID, accountId))
                    .and(Cql.eq(ROUND_ID, battlegroundRound.getRoundId()))
                    .limit(1);

            resultSet = executeWrapped(select, "update:: select before");

            existRow = resultSet.one();

            if (existRow != null) {

                BattlegroundRound existRound = deserialize(existRow);
                getLog().error("update:: found existing record with other startDate (remove), existRound={}", existRound);
                remove(accountId, existRound.getDateTime());

            }

            create(accountId, battlegroundRound);
        }
    }

    public void remove(long accountId, long date) {
        Delete query = Cql.delete().from(getMainColumnFamilyName());
        query.where(eq(ACCOUNT_ID, accountId)).and(eq(DATE_TIME, date));
        execute(query, "remove");
    }

    public List<BattlegroundRound> getBattlegroundHistoryByAccountIdAndPeriod(long accountId, long startTime,
                                                                              long endTime) {
        Select select = getSelectColumnsQuery(DATE_TIME, GAME_ID, SERIALIZED_COLUMN_NAME, JSON_COLUMN_NAME)
                .where(eq(ACCOUNT_ID, accountId))
                .and(Cql.gte(DATE_TIME, startTime))
                .and(Cql.lt(DATE_TIME, endTime))
                .limit(PAGE_SIZE);
        com.abs.casino.cassandra.persist.engine.ResultSet result = executeWrapped(select, "getBattlegroundHistoryByAccountIdAndPeriod");
        List<BattlegroundRound> battlegroundRounds = new ArrayList<>();
        if (result != null) {
            for (com.abs.casino.cassandra.persist.engine.Row row : result) {
                battlegroundRounds.add(deserialize(row));
            }
        }
        return battlegroundRounds;
    }

    public List<BattlegroundRound> getBattlegroundHistoryByAccountIdAndPeriodAndGameId(long accountId, long startTime,
                                                                                       long endTime, int gameId) {
        Select select = getSelectColumnsQuery(DATE_TIME, SERIALIZED_COLUMN_NAME, JSON_COLUMN_NAME)
                .where(eq(ACCOUNT_ID, accountId))
                .and(Cql.gte(DATE_TIME, startTime))
                .and(Cql.lt(DATE_TIME, endTime))
                .and(eq(GAME_ID, gameId))
                .limit(PAGE_SIZE);
        com.abs.casino.cassandra.persist.engine.ResultSet result = executeWrapped(select, "getBattlegroundHistoryByAccountIdAndPeriodAndGameId");
        List<BattlegroundRound> battlegroundRounds = new ArrayList<>();
        if (result != null) {
            for (com.abs.casino.cassandra.persist.engine.Row row : result) {
                battlegroundRounds.add(deserialize(row));
            }
        }
        return battlegroundRounds;
    }

    public List<BattlegroundRound> getBattlegroundHistoryByGameSessionId(long gameSessionId) {
        Select select = getSelectColumnsQuery(SERIALIZED_COLUMN_NAME, JSON_COLUMN_NAME);
        select.where(eq(GAMESESSION_ID, gameSessionId));
        com.abs.casino.cassandra.persist.engine.ResultSet result = executeWrapped(select, "getBattlegroundHistoryByGameSessionId");
        List<BattlegroundRound> battlegroundRounds = new ArrayList<>();
        if (result != null) {
            for (com.abs.casino.cassandra.persist.engine.Row row : result) {
                battlegroundRounds.add(deserialize(row));
            }
        }
        return battlegroundRounds;
    }

    public long countBattlegroundHistoryByGameSessionId(long gameSessionId) {
        return count(eq(GAMESESSION_ID, gameSessionId));
    }

    @Override
    public TableDefinition getMainTableDefinition() {
        return TABLE;
    }

    @Override
    public List<TableDefinition> getAllTableDefinitions() {
        return Arrays.asList(TABLE, PARTICIPANT_ROUND_TABLE);
    }

    @Override
    public Logger getLog() {
        return LOG;
    }

    private BattlegroundRound deserialize(com.abs.casino.cassandra.persist.engine.Row row) {
        BattlegroundRound br =
                TABLE.deserializeFromJson(row.getString(JSON_COLUMN_NAME), BattlegroundRound.class);
        if (br == null) {
            br = TABLE.deserializeFrom(row.getBytes(SERIALIZED_COLUMN_NAME), BattlegroundRound.class);
        }
        return br;
    }

    public boolean isRoundFinished(long roundId) {
        List<BattlegroundRound> battlegroundHistoryByGameIdAndRoundId = getBattlegroundHistoryByGameIdAndRoundId(roundId);
        return battlegroundHistoryByGameIdAndRoundId.stream()
                .anyMatch(round -> COMPLETED_STATUS.equals(round.getStatus()) || CANCELLED_STATUS.equals(round.getStatus()));
    }

    public List<BattlegroundRound> getBattlegroundHistoryByGameIdAndRoundId(long roundId) {
        Select select = getSelectColumnsQuery(SERIALIZED_COLUMN_NAME, JSON_COLUMN_NAME);
        select.where(eq(ROUND_ID, roundId));
        com.abs.casino.cassandra.persist.engine.ResultSet result = executeWrapped(select, "getBattlegroundHistoryByGameIdAndRoundId");
        List<BattlegroundRound> battlegroundRounds = new ArrayList<>();
        if (result != null) {
            for (com.abs.casino.cassandra.persist.engine.Row row : result) {
                battlegroundRounds.add(deserialize(row));
            }
        }
        return battlegroundRounds;
    }

    public void addParticipantsWithBatch(List<BattlegroundRoundParticipant> rounds, Set<Long> accountIds) {
        if(rounds != null && !rounds.isEmpty()) {
            Batch batch = Cql.batch();
            int BATCH_SIZE = 10;
            int count = 0;

            for (BattlegroundRoundParticipant round : rounds) {
                String json = PARTICIPANT_ROUND_TABLE.serializeToJson(round);
                ByteBuffer buffer = PARTICIPANT_ROUND_TABLE.serializeToBytes(round);

                Insert insert = Cql.insertInto(PARTICIPANT_ROUND_TABLE.getTableName())
                        .value(SID, round.getSid())
                        .value(ROUND_ID, round.getRoundId())
                        .value(GAMESESSION_ID, round.getGameSessionId())
                        .value(ACCOUNT_IDS, accountIds)
                        .value(SERIALIZED_COLUMN_NAME, buffer)
                        .value(JSON_COLUMN_NAME, json);

                batch.add(insert);
                count++;

                if (count == BATCH_SIZE) {
                    execute(batch, "addParticipantsWithBatch");
                    batch = Cql.batch(); // start a new batch
                    count = 0;
                }
            }

            // execute any remaining statements
            if (count > 0) {
                execute(batch, "addParticipantsWithBatch");
            }
        }
    }

    public void addParticipants(BattlegroundRoundParticipant battlegroundRoundParticipant, Set<Long> accountIds) {
        ByteBuffer buffer = PARTICIPANT_ROUND_TABLE.serializeToBytes(battlegroundRoundParticipant);
        String json = PARTICIPANT_ROUND_TABLE.serializeToJson(battlegroundRoundParticipant);
        Insert insert = Cql.insertInto(PARTICIPANT_ROUND_TABLE.getTableName())
                .value(SID, battlegroundRoundParticipant.getSid())
                .value(ROUND_ID, battlegroundRoundParticipant.getRoundId())
                .value(GAMESESSION_ID, battlegroundRoundParticipant.getGameSessionId())
                .value(SERIALIZED_COLUMN_NAME, buffer)
                .value(JSON_COLUMN_NAME, json)
                .value(ACCOUNT_IDS, accountIds);

        execute(insert, "addParticipants");
    }

    public Set<Long> getParticipantsBySID(String sessionId) {
        Select select = Cql.select(ACCOUNT_IDS).from(PARTICIPANT_ROUND_TABLE.getTableName());
        select.where(eq(SID, sessionId));
        com.abs.casino.cassandra.persist.engine.ResultSet result = executeWrapped(select, "getParticipantsByGameSessionId");
        Set<Long> participantsIds = new HashSet<>();
        if (result != null) {
            for (com.abs.casino.cassandra.persist.engine.Row row : result) {
                participantsIds.addAll(row.getSet(ACCOUNT_IDS, Long.class));
            }
        }
        return participantsIds;
    }

    public List<BattlegroundRoundParticipant> getBattlegroundRoundParticipantByGameSessionId(long gameSessionId) {
        Select select = Cql.select(SERIALIZED_COLUMN_NAME, JSON_COLUMN_NAME)
                .from(PARTICIPANT_ROUND_TABLE.getTableName());
        select.where(eq(GAMESESSION_ID, gameSessionId));
        com.abs.casino.cassandra.persist.engine.ResultSet result = executeWrapped(select, "getParticipantsByGameSessionId");
        List<BattlegroundRoundParticipant> battlegroundRounds = new ArrayList<>();
        if (result != null) {
            for (com.abs.casino.cassandra.persist.engine.Row row : result) {
                battlegroundRounds.add(deserializeBattlegroundRoundParticipant(row));
            }
        }
        return battlegroundRounds;
    }

    private BattlegroundRoundParticipant deserializeBattlegroundRoundParticipant(com.abs.casino.cassandra.persist.engine.Row row) {
        BattlegroundRoundParticipant p = PARTICIPANT_ROUND_TABLE.deserializeFromJson(
                row.getString(JSON_COLUMN_NAME), BattlegroundRoundParticipant.class);;

        if (p == null) {
            p = PARTICIPANT_ROUND_TABLE.deserializeFrom(
                    row.getBytes(SERIALIZED_COLUMN_NAME), BattlegroundRoundParticipant.class);
        }
        return p;
    }
}
