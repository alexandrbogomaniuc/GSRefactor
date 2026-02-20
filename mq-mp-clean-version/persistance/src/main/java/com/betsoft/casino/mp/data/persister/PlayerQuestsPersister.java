package com.betsoft.casino.mp.data.persister;

import com.betsoft.casino.mp.model.Money;
import com.betsoft.casino.mp.model.PlayerQuests;
import com.betsoft.casino.mp.model.quests.IQuest;
import com.betsoft.casino.mp.service.IPlayerQuestsService;
import com.datastax.driver.core.DataType;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.datastax.driver.core.querybuilder.Truncate;
import com.datastax.driver.core.querybuilder.Update;
import com.dgphoenix.casino.cassandra.persist.engine.AbstractCassandraPersister;
import com.dgphoenix.casino.cassandra.persist.engine.ColumnDefinition;
import com.dgphoenix.casino.cassandra.persist.engine.TableDefinition;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.ByteBuffer;
import java.util.*;

public class PlayerQuestsPersister extends AbstractCassandraPersister<Long, String> implements IPlayerQuestsService<PlayerQuests> {
    private static final Logger LOG = LogManager.getLogger(PlayerQuestsPersister.class);

    private static final String CF_NAME = "PlayerQuests";
    private static final String BANK_ID_COLUMN = "bid";
    private static final String ACCOUNT_ID_COLUMN = "aid";
    private static final String GAME_ID_COLUMN = "gid";
    private static final String STAKE_COLUMN = "s";
    private static final String MODE_COLUMN = "m";

    private static final String SPECIAL_MODE_CF_NAME = "SMQuests";
    //field for tournament or bonusId
    private static final String SM_ID_COLUMN = "smid";


    private static final TableDefinition TABLE = new TableDefinition(CF_NAME,
            Arrays.asList(
                    new ColumnDefinition(BANK_ID_COLUMN, DataType.bigint(), false, false, true),
                    new ColumnDefinition(ACCOUNT_ID_COLUMN, DataType.bigint(), false, false, true),
                    new ColumnDefinition(GAME_ID_COLUMN, DataType.bigint(), false, false, true),
                    new ColumnDefinition(MODE_COLUMN, DataType.cint(), false, false, true),
                    new ColumnDefinition(STAKE_COLUMN, DataType.bigint(), false, false, true),
                    new ColumnDefinition(SERIALIZED_COLUMN_NAME, DataType.blob()),
                    new ColumnDefinition(JSON_COLUMN_NAME, DataType.text())
            ), BANK_ID_COLUMN, ACCOUNT_ID_COLUMN, GAME_ID_COLUMN, MODE_COLUMN);

    private static final TableDefinition SPECIAL_MODE_TABLE = new TableDefinition(SPECIAL_MODE_CF_NAME,
            Arrays.asList(
                    new ColumnDefinition(SM_ID_COLUMN, DataType.bigint(), false, false, true),
                    new ColumnDefinition(BANK_ID_COLUMN, DataType.bigint(), false, false, true),
                    new ColumnDefinition(ACCOUNT_ID_COLUMN, DataType.bigint(), false, false, true),
                    new ColumnDefinition(GAME_ID_COLUMN, DataType.bigint(), false, false, true),
                    new ColumnDefinition(MODE_COLUMN, DataType.cint(), false, false, true),
                    new ColumnDefinition(STAKE_COLUMN, DataType.bigint(), false, false, true),
                    new ColumnDefinition(SERIALIZED_COLUMN_NAME, DataType.blob()),
                    new ColumnDefinition(JSON_COLUMN_NAME, DataType.text())
            ), SM_ID_COLUMN, BANK_ID_COLUMN, ACCOUNT_ID_COLUMN, GAME_ID_COLUMN, MODE_COLUMN);

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
        return Collections.unmodifiableList(Arrays.asList(TABLE, SPECIAL_MODE_TABLE));
    }

    @Override
    public Logger getLog() {
        return LOG;
    }

    @Override
    public PlayerQuests load(long bankId, long gameId, long accountId, Money stake, int mode) {
        Select query = getSelectColumnsQuery(TABLE, SERIALIZED_COLUMN_NAME, GAME_ID_COLUMN, JSON_COLUMN_NAME);
        query.where()
                .and(eq(BANK_ID_COLUMN, bankId))
                .and(eq(ACCOUNT_ID_COLUMN, accountId))
                .and(eq(GAME_ID_COLUMN, gameId))
                .and(eq(MODE_COLUMN, mode))
                .and(eq(STAKE_COLUMN, stake.getValue()))
                .limit(1);
        Row result = execute(query, "load").one();

        if (result == null) {
            return new PlayerQuests(new HashSet<>());
        }

        PlayerQuests playerQuests = 
                TABLE.deserializeFromJson(result.getString(JSON_COLUMN_NAME), PlayerQuests.class);

        if (playerQuests == null) {
            playerQuests = 
                    TABLE.deserializeFrom(result.getBytes(SERIALIZED_COLUMN_NAME), PlayerQuests.class);
        }

        LOG.debug("load result: playerQuests size: {}", playerQuests.getQuests().size());
        return playerQuests;
    }

    @Override
    public PlayerQuests loadSpecialModeQuests(long tournamentOrBonusId, long bankId, long gameId, long accountId,
                                              Money stake, int mode) {
        LOG.debug("loadSpecialModeQuests: specialId={}, bankId={}, accountId={}, gameId={}, stake(cents)={}, mode={} ",
                tournamentOrBonusId, bankId, accountId, gameId, stake.toFloatCents(), mode);
        Select query = getSelectColumnsQuery(SPECIAL_MODE_TABLE, SERIALIZED_COLUMN_NAME, GAME_ID_COLUMN, JSON_COLUMN_NAME);
        query.where()
                .and(eq(SM_ID_COLUMN, tournamentOrBonusId))
                .and(eq(BANK_ID_COLUMN, bankId))
                .and(eq(ACCOUNT_ID_COLUMN, accountId))
                .and(eq(GAME_ID_COLUMN, gameId))
                .and(eq(MODE_COLUMN, mode))
                .and(eq(STAKE_COLUMN, stake.getValue()))
                .limit(1);
        Row result = execute(query, "loadSpecialModeQuests").one();
        if (result == null) {
            return new PlayerQuests(new HashSet<>());
        }

        PlayerQuests playerQuests = 
                SPECIAL_MODE_TABLE
                    .deserializeFromJson(result.getString(JSON_COLUMN_NAME), PlayerQuests.class);

        if (playerQuests == null) {
            playerQuests = 
                    SPECIAL_MODE_TABLE
                        .deserializeFrom(result.getBytes(SERIALIZED_COLUMN_NAME), PlayerQuests.class);
        }

        LOG.debug("loadSpecialModeQuests result: size={}", playerQuests.getQuests().size());
        return playerQuests;
    }

    @Override
    public Set<IQuest> getAllQuests(long bankId, long accountId, int mode, long gameId) {
        LOG.debug("getAllQuests: bankId: {}, accountId: {}, mode: {}, gameId: {}", bankId, accountId, mode, gameId);
        Select.Where query = getSelectColumnsQuery(TABLE, SERIALIZED_COLUMN_NAME, GAME_ID_COLUMN, JSON_COLUMN_NAME)
                .where(eq(BANK_ID_COLUMN, bankId))
                .and(eq(ACCOUNT_ID_COLUMN, accountId))
                .and(eq(GAME_ID_COLUMN, gameId))
                .and(eq(MODE_COLUMN, mode));

        Set<IQuest> quests = new HashSet<>();
        ResultSet result = execute(query, "getAllQuests");
        if (result != null) {
            for (Row row : result) {
                PlayerQuests playerQuests = TABLE.deserializeFromJson(row.getString(JSON_COLUMN_NAME),
                        PlayerQuests.class);
                if (playerQuests == null) {
                    playerQuests = TABLE.deserializeFrom(row.getBytes(SERIALIZED_COLUMN_NAME),
                            PlayerQuests.class);
                }
                if (playerQuests != null) {
                    quests.addAll(playerQuests.getQuests());
                }
            }
        }
        return quests;
    }

    @Override
    public Set<IQuest> getAllSpecialModeQuests(long tournamentOrBonusId, long bankId, long accountId, long gameId, int mode) {
        LOG.debug("getAllSpecialModeQuests: tournamentOrBonusId={}, bankId={}, accountId={}, gameId={}, mode={}",
                tournamentOrBonusId, bankId, accountId, gameId, mode);
        Select.Where query = getSelectColumnsQuery(SPECIAL_MODE_TABLE, SERIALIZED_COLUMN_NAME, GAME_ID_COLUMN, JSON_COLUMN_NAME)
                .where(eq(SM_ID_COLUMN, tournamentOrBonusId))
                .and(eq(BANK_ID_COLUMN, bankId))
                .and(eq(ACCOUNT_ID_COLUMN, accountId))
                .and(eq(GAME_ID_COLUMN, gameId))
                .and(eq(MODE_COLUMN, mode));

        Set<IQuest> quests = new HashSet<>();
        ResultSet result = execute(query, "getAllSpecialModeQuests");
        if (result != null) {
            for (Row row : result) {
                PlayerQuests tournamentQuests = SPECIAL_MODE_TABLE
                        .deserializeFromJson(row.getString(JSON_COLUMN_NAME),
                        PlayerQuests.class);
                if (tournamentQuests == null) {
                    tournamentQuests = SPECIAL_MODE_TABLE
                            .deserializeFrom(row.getBytes(SERIALIZED_COLUMN_NAME),
                            PlayerQuests.class);
                }
                if (tournamentQuests != null) {
                    quests.addAll(tournamentQuests.getQuests());
                }
            }
        }
        return quests;
    }

    @Override
    public PlayerQuests updateQuests(long bankId, long gameId, long accountId, Set<IQuest> quests, Money stake,
                                     int mode) {
        PlayerQuests playerQuests = new PlayerQuests(quests);
        long cents = stake.toCents();
        boolean isWrongQuests = quests.stream().noneMatch(quest -> quest.getRoomCoin() == cents);
        if (!isWrongQuests) {
            save(bankId, accountId, playerQuests, gameId, stake, mode);
        } else {
            LOG.debug("wrong save quests: for bankId: {} , accountId: {}, gameId: {}, quests: {}, stake(cents): {} ",
                    bankId, accountId, gameId, quests, stake.toFloatCents());
        }
        return playerQuests;
    }

    private void save(long bankId, long accountId, PlayerQuests quests, long gameId, Money stake, int mode) {
        LOG.debug("save quest: for bankId: {}, accountId: {}, gameId: {}, quests size: {}, stake(cents): {} ",
                bankId, accountId, gameId, quests.getQuests().size(), stake.toFloatCents());
        ByteBuffer buffer = TABLE.serializeToBytes(quests);
        String json = TABLE.serializeToJson(quests);;
        try {
            Update.Assignments update = getUpdateQuery()
                    .where(eq(BANK_ID_COLUMN, bankId))
                    .and(eq(ACCOUNT_ID_COLUMN, accountId))
                    .and(eq(MODE_COLUMN, mode))
                    .and(eq(GAME_ID_COLUMN, gameId))
                    .and(eq(STAKE_COLUMN, stake.getValue()))
                    .with(set(SERIALIZED_COLUMN_NAME, buffer))
                    .and(set(JSON_COLUMN_NAME, json));
            execute(update, "save");
        } finally {
            releaseBuffer(buffer);
        }
    }

    @Override
    public PlayerQuests updateSpecialModeQuests(long tournamentOrBonusId, long bankId, long gameId,
                                                long accountId, Set<IQuest> quests, Money stake, int mode) {
        PlayerQuests playerQuests = new PlayerQuests(quests);
        long cents = stake.toCents();
        boolean isWrongQuests = quests.stream().noneMatch(quest -> quest.getRoomCoin() == cents);
        if (!isWrongQuests) {
            saveSpecialModeQuests(tournamentOrBonusId, bankId, accountId, playerQuests, gameId, stake, mode);
        } else {
            LOG.debug("wrong save quests: for tournamentId: {}, bankId: {}, accountId: {}, gameId: {}, quests: {}, stake(cents): {} ",
                    tournamentOrBonusId, bankId, accountId, gameId, quests, stake.toFloatCents());
        }
        return playerQuests;
    }

    private void saveSpecialModeQuests(long tournamentOrBonusId, long bankId, long accountId, PlayerQuests quests,
                                       long gameId, Money stake, int mode) {
        LOG.debug("save quest: for tournamentOrBonusId: {}, bankId: {}, accountId: {}, gameId: {}, quests size: {}, " +
                        "stake(cents): {}, mode={}", tournamentOrBonusId, bankId, accountId, gameId,
                quests.getQuests().size(), stake.toFloatCents(), mode);
        ByteBuffer buffer = SPECIAL_MODE_TABLE.serializeToBytes(quests);
        String json = SPECIAL_MODE_TABLE.serializeToJson(quests);
        try {
            Update.Assignments update = QueryBuilder.update(SPECIAL_MODE_CF_NAME)
                    .where(eq(SM_ID_COLUMN, tournamentOrBonusId))
                    .and(eq(BANK_ID_COLUMN, bankId))
                    .and(eq(ACCOUNT_ID_COLUMN, accountId))
                    .and(eq(GAME_ID_COLUMN, gameId))
                    .and(eq(MODE_COLUMN, mode))
                    .and(eq(STAKE_COLUMN, stake.getValue()))
                    .with(set(SERIALIZED_COLUMN_NAME, buffer))
                    .and(set(JSON_COLUMN_NAME, json));
            execute(update, "saveSpecialModeQuests");
        } finally {
            releaseBuffer(buffer);
        }
    }

    @Override
    public void removeAllQuests(long gameId) {
        Truncate truncate = QueryBuilder.truncate(CF_NAME);
        execute(truncate, "removeAllQuests");
        getLog().debug("remove all quests");
    }

    public void removeSpecialModeAllQuests() {
        Truncate truncate = QueryBuilder.truncate(SPECIAL_MODE_CF_NAME);
        execute(truncate, "removeSpecialModeAllQuests");
        getLog().debug("removeSpecialModeAllQuests");
    }

}
