package com.dgphoenix.casino.cassandra.persist;

import com.dgphoenix.casino.cassandra.persist.engine.AbstractCassandraPersister;
import com.dgphoenix.casino.cassandra.persist.engine.ColumnDefinition;
import com.dgphoenix.casino.cassandra.persist.engine.TableDefinition;
import com.dgphoenix.casino.cassandra.persist.engine.configuration.Caching;
import com.dgphoenix.casino.cassandra.persist.engine.configuration.CompactionStrategy;
import com.dgphoenix.casino.cassandra.persist.engine.configuration.Compression;
import com.abs.casino.common.cache.CacheKeyInfo;
import com.dgphoenix.casino.common.cache.CurrencyCache;
import com.dgphoenix.casino.common.cache.IDistributedCache;
import com.dgphoenix.casino.common.cache.data.bonus.BonusStatus;
import com.dgphoenix.casino.common.cache.data.currency.Currency;
import com.dgphoenix.casino.common.cache.data.session.ClientType;
import com.dgphoenix.casino.common.cache.data.session.GameSession;
import com.abs.casino.common.util.LongPair;
import com.abs.casino.common.util.StreamUtils;
import com.dgphoenix.casino.common.util.*;
import com.dgphoenix.casino.common.util.string.StringUtils;
import com.dgphoenix.casino.common.web.statistics.StatisticsManager;

import org.apache.commons.lang.mutable.MutableInt;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


/**
 * User: flsh
 * Date: 26.06.13
 */
@CacheKeyInfo(description = "gameSession.id")
public class CassandraGameSessionPersister extends AbstractCassandraPersister<Long, String> implements
        IDistributedCache<String, GameSession> {
    public static final String GAME_SESSION_CF = "GameSessionCF";
    public static final String GAME_SESSION_AG_INDX = "GameSessionCF_AG";
    public static final String GAME_SESSION_AGM_INDX = "GameSessionCF_AGM";
    public static final String BANK_GAME_SESSION_AG_INDX = "GameSessionCF_BAG_idx";
    private static final Logger LOG = LogManager.getLogger(CassandraGameSessionPersister.class);

    public static final long ALL_GAMES_ID = -1;
    public static final int MODE_ALL = 0;
    public static final int MODE_REAL = 1;
    public static final int MODE_BONUS = 2;
    public static final int MODE_FR_BONUS = 3;

    private static final String GAME_SESSION_ID_FIELD = "GSId";
    private static final String ACCOUNT_ID_FIELD = "AccId";
    private static final String GAME_ID_FIELD = "GameId";
    //mode: 1 - real, 2 - bonus, 3 - frBonus
    private static final String MODE_FIELD = "Mode";
    private static final String START_TIME_FIELD = "ST";
    private static final String END_TIME_FIELD = "ET";
    private static final String INCOME_FIELD = "Income";
    private static final String PAYOUT_FIELD = "Payout";
    private static final String NEGATIVE_BET_FIELD = "NB";
    private static final String BETS_COUNT_FIELD = "Bets";
    private static final String ROUNDS_COUNT_FIELD = "RC";
    private static final String LAST_PLAYER_BET_ID_FIELD = "LBId";
    private static final String REAL_MONEY_FIELD = "Real";
    private static final String PCR_SUM_FIELD = "PCR";
    private static final String BCR_SUM_FIELD = "BCR";
    private static final String CURRENCY_FIELD = "Curr";
    private static final String CURRENCY_FRACTION_FIELD = "Fraction";
    private static final String BONUS_ID_FIELD = "Bonus";
    private static final String FR_BONUS_ID_FIELD = "FrBonus";
    private static final String BONUS_STATUS_FIELD = "BonSt";
    private static final String FR_BONUS_STATUS_FIELD = "FrBonSt";
    private static final String EXT_SESSION_ID_FIELD = "ExtId";
    private static final String START_BALANCE_FIELD = "StBalance";
    private static final String START_BONUS_BALANCE_FIELD = "StBBalance";
    private static final String END_BONUS_BALANCE_FIELD = "EnBBalance";             //new
    private static final String LANG_FIELD = "Lang";
    private static final String CLIENT_TYPE_FIELD = "ClType";
    private static final String BANK_ID_FIELD = "bId";
    private static final String BONUS_BET_FIELD = "bonusBet";                       //new
    private static final String BONUS_WIN_FIELD = "bonusWin";                       //new
    private static final String UNJ_ID_FIELD = "unjId";                             //new
    private static final String UNJ_SUM_CONNTRIBUTION_FIELD = "unjSC";              //new
    private static final String UNJ_SUM_WIN_FIELD = "unjSW";
    private static final String PREV_GAME_SESSION_ID_FIELD = "PrevId";              //new, -1 - not found
    private static final String NEXT_GAME_SESSION_ID_FIELD = "NextId";              //new, -1 - not found
    private static final String GAME_SERIAL_NUMBER_FIELD = "GSN";
    private static final String PROMO_IDS_FIELD = "promoIds";

    private static final String DAY_FIELD = "Day";

    private static final String ENTER_DATE_FIELD = "enterDate";
    private static final String CONTRIBUTIONS_JP_FIELD = "contribJP";
    private static final String CONTRIBUTIONS_JP_FIELD_JSON = CONTRIBUTIONS_JP_FIELD + "_json";
    private static final String DBL_UP_ROUNDS_COUNT_FIELD = "dblUpRC";
    private static final String DBL_UP_INCOME_COUNT_FIELD = "dblUpI";
    private static final String DBL_UP_PAYOUT_COUNT_FIELD = "dblUpP";
    private static final String MODEL_FIELD = "mdl";

    private static final TableDefinition MAIN_TABLE = new TableDefinition(GAME_SESSION_CF,
            Arrays.asList(
                    new ColumnDefinition(GAME_SESSION_ID_FIELD, com.datastax.driver.core.DataType.bigint(), false, false, true),
                    new ColumnDefinition(ACCOUNT_ID_FIELD, com.datastax.driver.core.DataType.bigint()),
                    new ColumnDefinition(GAME_ID_FIELD, com.datastax.driver.core.DataType.bigint()),
                    new ColumnDefinition(END_TIME_FIELD, com.datastax.driver.core.DataType.bigint()),
                    new ColumnDefinition(START_TIME_FIELD, com.datastax.driver.core.DataType.bigint()),
                    new ColumnDefinition(INCOME_FIELD, com.datastax.driver.core.DataType.bigint()),
                    new ColumnDefinition(PAYOUT_FIELD, com.datastax.driver.core.DataType.bigint()),
                    new ColumnDefinition(NEGATIVE_BET_FIELD, com.datastax.driver.core.DataType.bigint()),
                    new ColumnDefinition(BETS_COUNT_FIELD, com.datastax.driver.core.DataType.cint()),
                    new ColumnDefinition(ROUNDS_COUNT_FIELD, com.datastax.driver.core.DataType.cint()),
                    new ColumnDefinition(LAST_PLAYER_BET_ID_FIELD, com.datastax.driver.core.DataType.bigint()),
                    new ColumnDefinition(REAL_MONEY_FIELD, com.datastax.driver.core.DataType.cboolean()),
                    new ColumnDefinition(PCR_SUM_FIELD, com.datastax.driver.core.DataType.cdouble()),
                    new ColumnDefinition(BCR_SUM_FIELD, com.datastax.driver.core.DataType.cdouble()),
                    new ColumnDefinition(CURRENCY_FIELD, com.datastax.driver.core.DataType.text()),
                    new ColumnDefinition(BONUS_ID_FIELD, com.datastax.driver.core.DataType.bigint(), false, false, false),
                    new ColumnDefinition(FR_BONUS_ID_FIELD, com.datastax.driver.core.DataType.bigint(), false, false, false),
                    new ColumnDefinition(BONUS_STATUS_FIELD, com.datastax.driver.core.DataType.text()),
                    new ColumnDefinition(FR_BONUS_STATUS_FIELD, com.datastax.driver.core.DataType.text()),
                    new ColumnDefinition(EXT_SESSION_ID_FIELD, com.datastax.driver.core.DataType.text(), false, true, false),
                    new ColumnDefinition(START_BALANCE_FIELD, com.datastax.driver.core.DataType.bigint()),
                    new ColumnDefinition(START_BONUS_BALANCE_FIELD, com.datastax.driver.core.DataType.bigint()),
                    new ColumnDefinition(END_BONUS_BALANCE_FIELD, com.datastax.driver.core.DataType.bigint()),
                    new ColumnDefinition(LANG_FIELD, com.datastax.driver.core.DataType.text()),
                    new ColumnDefinition(CLIENT_TYPE_FIELD, com.datastax.driver.core.DataType.text()),
                    new ColumnDefinition(BANK_ID_FIELD, com.datastax.driver.core.DataType.bigint()),
                    new ColumnDefinition(BONUS_BET_FIELD, com.datastax.driver.core.DataType.bigint()),
                    new ColumnDefinition(BONUS_WIN_FIELD, com.datastax.driver.core.DataType.bigint()),
                    new ColumnDefinition(UNJ_ID_FIELD, com.datastax.driver.core.DataType.bigint()),
                    new ColumnDefinition(UNJ_SUM_CONNTRIBUTION_FIELD, com.datastax.driver.core.DataType.cdouble()),
                    new ColumnDefinition(UNJ_SUM_WIN_FIELD, com.datastax.driver.core.DataType.bigint()),
                    new ColumnDefinition(PREV_GAME_SESSION_ID_FIELD, com.datastax.driver.core.DataType.bigint(), false, true, false),
                    new ColumnDefinition(NEXT_GAME_SESSION_ID_FIELD, com.datastax.driver.core.DataType.bigint(), false, true, false),
                    new ColumnDefinition(DAY_FIELD, com.datastax.driver.core.DataType.bigint(), false, true, false),
                    new ColumnDefinition(CURRENCY_FRACTION_FIELD, com.datastax.driver.core.DataType.text()),
                    new ColumnDefinition(PROMO_IDS_FIELD, com.datastax.driver.core.DataType.list(com.datastax.driver.core.DataType.bigint())),
                    new ColumnDefinition(ENTER_DATE_FIELD, com.datastax.driver.core.DataType.bigint()),
                    new ColumnDefinition(CONTRIBUTIONS_JP_FIELD, com.datastax.driver.core.DataType.blob()),
                    new ColumnDefinition(CONTRIBUTIONS_JP_FIELD_JSON, com.datastax.driver.core.DataType.text()),
                    new ColumnDefinition(DBL_UP_ROUNDS_COUNT_FIELD, com.datastax.driver.core.DataType.cint()),
                    new ColumnDefinition(DBL_UP_INCOME_COUNT_FIELD, com.datastax.driver.core.DataType.bigint()),
                    new ColumnDefinition(DBL_UP_PAYOUT_COUNT_FIELD, com.datastax.driver.core.DataType.bigint()),
                    new ColumnDefinition(MODEL_FIELD, com.datastax.driver.core.DataType.cdouble())
            ), GAME_SESSION_ID_FIELD)
            .caching(Caching.NONE)
            .compaction(CompactionStrategy.LEVELED)
            .gcGraceSeconds(TimeUnit.DAYS.toSeconds(1))
            .compression(Compression.DEFLATE);


    //Primary key (accountId, gameId), endTime clustered order by endTime desc
    //this table used for query without mode, if gameId=-1 - this all games index
    private static final TableDefinition GAME_INDEX_TABLE = new TableDefinition(GAME_SESSION_AG_INDX,
            Arrays.asList(
                    new ColumnDefinition(ACCOUNT_ID_FIELD, com.datastax.driver.core.DataType.bigint(), false, false, true),
                    new ColumnDefinition(GAME_ID_FIELD, com.datastax.driver.core.DataType.bigint(), false, false, true),
                    new ColumnDefinition(END_TIME_FIELD, com.datastax.driver.core.DataType.bigint(), false, false, true),
                    new ColumnDefinition(GAME_SESSION_ID_FIELD, com.datastax.driver.core.DataType.bigint()),
                    new ColumnDefinition(GAME_SERIAL_NUMBER_FIELD, com.datastax.driver.core.DataType.bigint(), false, true, false)
            ), ACCOUNT_ID_FIELD, GAME_ID_FIELD)
            .caching(Caching.NONE)
            .compaction(CompactionStrategy.LEVELED)
            .gcGraceSeconds(TimeUnit.DAYS.toSeconds(1))
            .compression(Compression.DEFLATE)
            .clusteringOrder(END_TIME_FIELD, com.datastax.driver.core.schemabuilder.SchemaBuilder.Direction.DESC);

    //Primary key (accountId, mode, gameId), endTime clustered order by endTime desc
    //this table used for query with Mode, if gameId=-1 - this all games index
    private static final TableDefinition GAME_MODE_INDEX_TABLE = new TableDefinition(GAME_SESSION_AGM_INDX,
            Arrays.asList(
                    new ColumnDefinition(ACCOUNT_ID_FIELD, com.datastax.driver.core.DataType.bigint(), false, false, true),
                    new ColumnDefinition(MODE_FIELD, com.datastax.driver.core.DataType.cint(), false, false, true),
                    new ColumnDefinition(GAME_ID_FIELD, com.datastax.driver.core.DataType.bigint(), false, false, true),
                    new ColumnDefinition(END_TIME_FIELD, com.datastax.driver.core.DataType.bigint(), false, false, true),
                    new ColumnDefinition(GAME_SESSION_ID_FIELD, com.datastax.driver.core.DataType.bigint()),
                    new ColumnDefinition(GAME_SERIAL_NUMBER_FIELD, com.datastax.driver.core.DataType.bigint(), false, true, false)
            ), ACCOUNT_ID_FIELD, MODE_FIELD, GAME_ID_FIELD)
            .caching(Caching.NONE)
            .compaction(CompactionStrategy.LEVELED)
            .gcGraceSeconds(TimeUnit.DAYS.toSeconds(1))
            .compression(Compression.DEFLATE)
            .clusteringOrder(END_TIME_FIELD, com.datastax.driver.core.schemabuilder.SchemaBuilder.Direction.DESC);

    //Primary ke:y (bankId, gameId), endTime clustered order by endTime desc
    //this table used for query without mode, if gameId=-1 - this all games index

    //note: for bugfix collision
    //CREATE TABLE casinoks.GameSessionCF_BAG_idx (bid bigint, gameid bigint, et bigint, accId bigint,  gsid bigint,
    // PRIMARY KEY ((bid, gameid), et, accId));
    //insert into  GameSessionCF_BAG_idx (bid, gameid, et, accId, gsId) values (600, -1, 1442880280001, 100, 2726195600);
    //insert into  GameSessionCF_BAG_idx (bid, gameid, et, accId, gsId) values (600, -1, 1442880280001, 101, 2726195601);
    //select * from GameSessionCF_BAG_idx where bid=600 and gameid=-1 and et>=1442880280000 and et<=1442880282000  limit 1;
    private static final TableDefinition BANK_GAME_INDEX_TABLE = new TableDefinition(BANK_GAME_SESSION_AG_INDX,
            Arrays.asList(
                    new ColumnDefinition(BANK_ID_FIELD, com.datastax.driver.core.DataType.bigint(), false, false, true),
                    new ColumnDefinition(GAME_ID_FIELD, com.datastax.driver.core.DataType.bigint(), false, false, true),
                    new ColumnDefinition(END_TIME_FIELD, com.datastax.driver.core.DataType.bigint(), false, false, true),
                    new ColumnDefinition(ACCOUNT_ID_FIELD, com.datastax.driver.core.DataType.bigint(), false, false, true),
                    new ColumnDefinition(GAME_SESSION_ID_FIELD, com.datastax.driver.core.DataType.bigint())
            ), BANK_ID_FIELD, GAME_ID_FIELD)
            .caching(Caching.NONE)
            .compaction(CompactionStrategy.LEVELED)
            .gcGraceSeconds(TimeUnit.DAYS.toSeconds(1))
            .compression(Compression.DEFLATE)
            .clusteringOrder(END_TIME_FIELD, com.datastax.driver.core.schemabuilder.SchemaBuilder.Direction.DESC);

    private CassandraGameSessionPersister() {
    }

    @Override
    protected String getKeyColumnName() {
        return GAME_SESSION_ID_FIELD;
    }

    @Override
    public Logger getLog() {
        return LOG;
    }

    @Override
    public TableDefinition getMainTableDefinition() {
        return MAIN_TABLE;
    }

    @Override
    public List<TableDefinition> getAllTableDefinitions() {
        return Arrays.asList(MAIN_TABLE, GAME_INDEX_TABLE, GAME_MODE_INDEX_TABLE, BANK_GAME_INDEX_TABLE);
    }

    //first - gameSessionId, second - serialNumber
    public LongPair getSerialNumber(long accountId, Long gameId, Integer mode) {
        com.datastax.driver.core.querybuilder.Select query = getSelectAllColumnsQuery(mode == null ? GAME_INDEX_TABLE : GAME_MODE_INDEX_TABLE);
        query.where().and(eq(ACCOUNT_ID_FIELD, accountId)).
                and(eq(GAME_ID_FIELD, gameId == null || gameId <= 0 ? ALL_GAMES_ID : gameId));
        if (mode != null) {
            query.where().and(eq(MODE_FIELD, mode));
        }
        query.orderBy(com.datastax.driver.core.querybuilder.QueryBuilder.desc(END_TIME_FIELD));
        query.limit(1);
        com.datastax.driver.core.ResultSet rows = execute(query, "getSerialNumber");
        com.datastax.driver.core.Row row = rows.one();
        LongPair result;
        if (row == null) {
            getLog().info("getSerialNumber: row is empty: accountId={}, gameId={}, mode={}", accountId, gameId, mode);
            result = new LongPair(-1, 0);
        } else {
            long gameSessionId = row.getLong(GAME_SESSION_ID_FIELD);
            long serialNumber = row.getLong(GAME_SERIAL_NUMBER_FIELD);
            result = new LongPair(gameSessionId, serialNumber);
        }
        return result;
    }

    public void persist(GameSession session) {
        if (!session.isRealMoney()) {
            LOG.warn("Cannot persist free mode session: {}", session);
            return;
        }
        long bankId = session.getBankId();
        com.datastax.driver.core.querybuilder.Insert query = getInsertQuery();
        if (session.getEndTime() == null) {
            getLog().warn("persist: may be error, endTime is null: {}", session);
            long endTime = NtpTimeProvider.getInstance().getTime();
            if (session.getStartTime() >= endTime) {
                endTime = session.getStartTime() + 1;
            }
            session.setEndTime(endTime);
        }
        query.value(GAME_SESSION_ID_FIELD, session.getId());
        query.value(ACCOUNT_ID_FIELD, session.getAccountId());
        query.value(GAME_ID_FIELD, session.getGameId());
        int mode;
        if (session.getFrbonusId() != null) {
            mode = MODE_FR_BONUS;
        } else if (session.getBonusId() != null) {
            mode = MODE_BONUS;
        } else {
            mode = MODE_REAL;
        }
        query.value(START_TIME_FIELD, session.getStartTime());
        query.value(END_TIME_FIELD, session.getEndTime());
        query.value(INCOME_FIELD, session.getIncome());
        query.value(PAYOUT_FIELD, session.getPayout());
        query.value(NEGATIVE_BET_FIELD, session.getNegativeBet());
        query.value(BETS_COUNT_FIELD, (int) session.getBetsCount());
        query.value(ROUNDS_COUNT_FIELD, (int) session.getRoundsCount());
        query.value(LAST_PLAYER_BET_ID_FIELD, session.getLastPlayerBetId());
        query.value(REAL_MONEY_FIELD, session.isRealMoney());
        query.value(PCR_SUM_FIELD, session.getPcrSum());
        query.value(BCR_SUM_FIELD, session.getBcrSum());
        query.value(CURRENCY_FIELD, session.getCurrency().getCode());
        if (session.getCurrencyFraction() != null) {
            query.value(CURRENCY_FRACTION_FIELD, session.getCurrencyFraction());
        }
        query.value(FR_BONUS_ID_FIELD, session.getFrbonusId() == null ? -1L : session.getFrbonusId());
        query.value(BONUS_ID_FIELD, session.getBonusId() == null ? -1L : session.getBonusId());
        if (session.getFrbonusStatus() != null) {
            query.value(FR_BONUS_STATUS_FIELD, session.getFrbonusStatus().name());
        }
        if (session.getBonusStatus() != null) {
            query.value(BONUS_STATUS_FIELD, session.getBonusStatus().name());
        }
        if (!StringUtils.isTrimmedEmpty(session.getExternalSessionId())) {
            query.value(EXT_SESSION_ID_FIELD, session.getExternalSessionId());
        }
        query.value(START_BALANCE_FIELD, session.getStartBalance());
        query.value(START_BONUS_BALANCE_FIELD, session.getStartBonusBalance());
        query.value(END_BONUS_BALANCE_FIELD, session.getEndBonusBalance());
        if (!StringUtils.isTrimmedEmpty(session.getLang())) {
            query.value(LANG_FIELD, session.getLang());
        }
        if (session.getClientType() != null) {
            query.value(CLIENT_TYPE_FIELD, session.getClientType().name());
        }
        query.value(BANK_ID_FIELD, bankId);
        query.value(BONUS_BET_FIELD, session.getBonusBet());
        query.value(BONUS_WIN_FIELD, session.getBonusWin());
        if (session.getUnjId() != null) {
            query.value(UNJ_ID_FIELD, session.getUnjId());
        }
        query.value(UNJ_SUM_CONNTRIBUTION_FIELD, session.getUnjSummaryContribution());
        query.value(UNJ_SUM_WIN_FIELD, session.getUnjSummaryWin());
        Long lastGameSessionId = getLastGameSession(session.getAccountId(), session.getGameId());
        query.value(PREV_GAME_SESSION_ID_FIELD, lastGameSessionId == null ? -1L : lastGameSessionId);
        query.value(NEXT_GAME_SESSION_ID_FIELD, -1L);
        query.value(DAY_FIELD, getDay(session));
        query.value(PROMO_IDS_FIELD, session.getPromoCampaignIds());
        query.value(ENTER_DATE_FIELD, session.getEnterDate());
        HashMap<Long, Double> contributionsJP = session.getContributionsJP();
        ByteBuffer byteBuffer = MAIN_TABLE.serializeToBytes(contributionsJP);
        String json = MAIN_TABLE.serializeToMapJson(contributionsJP, Long.class, Double.class);
        try {
            query.value(CONTRIBUTIONS_JP_FIELD, byteBuffer);
            query.value(CONTRIBUTIONS_JP_FIELD_JSON, json);
            query.value(DBL_UP_ROUNDS_COUNT_FIELD, session.getDblUpRoundsCount());
            query.value(DBL_UP_INCOME_COUNT_FIELD, session.getDblUpIncome());
            query.value(DBL_UP_PAYOUT_COUNT_FIELD, session.getDblUpPayout());
            query.value(MODEL_FIELD, session.getModel());
            com.datastax.driver.core.querybuilder.Batch batch = com.datastax.driver.core.querybuilder.QueryBuilder.batch();
            batch.add(query);

            com.datastax.driver.core.querybuilder.Insert concreteGameIndexQuery = getInsertQuery(GAME_INDEX_TABLE, null);
            concreteGameIndexQuery.value(GAME_SESSION_ID_FIELD, session.getId());
            concreteGameIndexQuery.value(ACCOUNT_ID_FIELD, session.getAccountId());
            concreteGameIndexQuery.value(GAME_ID_FIELD, session.getGameId());
            LongPair gameSerialNumber = getSerialNumber(session.getAccountId(), session.getGameId(), null);
            concreteGameIndexQuery.value(GAME_SERIAL_NUMBER_FIELD, gameSerialNumber.getSecond() + 1);
            concreteGameIndexQuery.value(END_TIME_FIELD, session.getEndTime());
            batch.add(concreteGameIndexQuery);

            com.datastax.driver.core.querybuilder.Insert allGamesIndexQuery = getInsertQuery(GAME_INDEX_TABLE, null);
            allGamesIndexQuery.value(GAME_SESSION_ID_FIELD, session.getId());
            allGamesIndexQuery.value(ACCOUNT_ID_FIELD, session.getAccountId());
            allGamesIndexQuery.value(GAME_ID_FIELD, ALL_GAMES_ID);
            gameSerialNumber = getSerialNumber(session.getAccountId(), ALL_GAMES_ID, null);
            allGamesIndexQuery.value(GAME_SERIAL_NUMBER_FIELD, gameSerialNumber.getSecond() + 1);
            allGamesIndexQuery.value(END_TIME_FIELD, session.getEndTime());
            batch.add(allGamesIndexQuery);

            com.datastax.driver.core.querybuilder.Insert concreteBankGameIndexQuery = getInsertQuery(BANK_GAME_INDEX_TABLE, null);
            concreteBankGameIndexQuery.value(BANK_ID_FIELD, bankId);
            concreteBankGameIndexQuery.value(GAME_ID_FIELD, session.getGameId());
            concreteBankGameIndexQuery.value(GAME_SESSION_ID_FIELD, session.getId());
            concreteBankGameIndexQuery.value(END_TIME_FIELD, session.getEndTime());
            concreteBankGameIndexQuery.value(ACCOUNT_ID_FIELD, session.getAccountId());
            batch.add(concreteBankGameIndexQuery);

            com.datastax.driver.core.querybuilder.Insert allBankGamesIndexQuery = getInsertQuery(BANK_GAME_INDEX_TABLE, null);
            allBankGamesIndexQuery.value(BANK_ID_FIELD, bankId);
            allBankGamesIndexQuery.value(GAME_ID_FIELD, ALL_GAMES_ID);
            allBankGamesIndexQuery.value(GAME_SESSION_ID_FIELD, session.getId());
            allBankGamesIndexQuery.value(END_TIME_FIELD, session.getEndTime());
            allBankGamesIndexQuery.value(ACCOUNT_ID_FIELD, session.getAccountId());
            batch.add(allBankGamesIndexQuery);

            com.datastax.driver.core.querybuilder.Insert concreteGameAndModeIndexQuery = getInsertQuery(GAME_MODE_INDEX_TABLE, null);
            concreteGameAndModeIndexQuery.value(GAME_SESSION_ID_FIELD, session.getId());
            concreteGameAndModeIndexQuery.value(ACCOUNT_ID_FIELD, session.getAccountId());
            concreteGameAndModeIndexQuery.value(MODE_FIELD, mode);
            concreteGameAndModeIndexQuery.value(GAME_ID_FIELD, session.getGameId());
            gameSerialNumber = getSerialNumber(session.getAccountId(), session.getGameId(), mode);
            concreteGameAndModeIndexQuery.value(GAME_SERIAL_NUMBER_FIELD, gameSerialNumber.getSecond() + 1);
            concreteGameAndModeIndexQuery.value(END_TIME_FIELD, session.getEndTime());
            batch.add(concreteGameAndModeIndexQuery);

            com.datastax.driver.core.querybuilder.Insert allGamesAndModeIndexQuery = getInsertQuery(GAME_MODE_INDEX_TABLE, null);
            allGamesAndModeIndexQuery.value(GAME_SESSION_ID_FIELD, session.getId());
            allGamesAndModeIndexQuery.value(ACCOUNT_ID_FIELD, session.getAccountId());
            allGamesAndModeIndexQuery.value(MODE_FIELD, mode);
            allGamesAndModeIndexQuery.value(GAME_ID_FIELD, ALL_GAMES_ID);
            gameSerialNumber = getSerialNumber(session.getAccountId(), ALL_GAMES_ID, mode);
            allGamesAndModeIndexQuery.value(GAME_SERIAL_NUMBER_FIELD, gameSerialNumber.getSecond() + 1);
            allGamesAndModeIndexQuery.value(END_TIME_FIELD, session.getEndTime());
            batch.add(allGamesAndModeIndexQuery);

            //update prevGameSession
            if (lastGameSessionId != null) {
                com.datastax.driver.core.querybuilder.Update prevQuery = getUpdateQuery();
                prevQuery.where().and(eq(GAME_SESSION_ID_FIELD, lastGameSessionId));
                prevQuery.with(com.datastax.driver.core.querybuilder.QueryBuilder.set(NEXT_GAME_SESSION_ID_FIELD, session.getId()));
                batch.add(prevQuery);
            }
            execute(batch, "persist");
        } finally {
            releaseBuffer(byteBuffer);
        }
    }

    public void rebuidBankIndex() {
        LOG.info("rebuidBankIndex: started");
        int count = 0;
        int sessionWithoutEndTimeCount = 0;
        Map<String, MutableInt> timeToCounter = new HashMap<>();

        com.datastax.driver.core.querybuilder.Select query = getSelectAllColumnsQuery();
        com.datastax.driver.core.ResultSet resultSet = execute(query, "rebuidBankIndex");
        for (com.datastax.driver.core.Row row : resultSet) {
            GameSession currentGameSession = convert(row);
            if (currentGameSession == null) {
                continue;
            }

            if (currentGameSession.getEndTime() == null) {
                LOG.warn("rebuidBankIndex: gameSession without endTime: {}", currentGameSession);
                sessionWithoutEndTimeCount++;
                continue;
            }

            if (currentGameSession.getEndTime() % 1000 != 0l) {
                continue;
            }

            com.datastax.driver.core.querybuilder.Select selectExistIdForConcreteGame = getSelectColumnsQuery(BANK_GAME_INDEX_TABLE, GAME_SESSION_ID_FIELD).
                    where(eq(BANK_ID_FIELD, currentGameSession.getBankId())).
                    and(eq(GAME_ID_FIELD, currentGameSession.getGameId())).
                    and(eq(END_TIME_FIELD, currentGameSession.getEndTime())).
                    and(eq(ACCOUNT_ID_FIELD, currentGameSession.getAccountId())).limit(1);
            com.datastax.driver.core.Row concreteSelectResult = execute(selectExistIdForConcreteGame,
                    "rebuildBankIndex: select in bankIndexTable exist game session id").one();

            long gameSessionId;
            MutableInt timeCounter = null;
            if (concreteSelectResult == null || (gameSessionId = concreteSelectResult.getLong(GAME_SESSION_ID_FIELD)) == 0l ||
                    gameSessionId != currentGameSession.getId()) {

                Long endTime = currentGameSession.getEndTime();
                String key = Long.toString(currentGameSession.getAccountId()) + Long.toString(endTime);
                timeCounter = timeToCounter.get(key);
                if (timeCounter == null) {
                    timeCounter = new MutableInt(1);
                    timeToCounter.put(key, timeCounter);
                }

                endTime += timeCounter.intValue();

                LOG.debug("Not exist for concrete game session={}", currentGameSession);
                LOG.debug("New end time={}", endTime);

                com.datastax.driver.core.querybuilder.Insert concreteBankGameIndexQuery = getInsertQuery(BANK_GAME_INDEX_TABLE, null);
                concreteBankGameIndexQuery.value(BANK_ID_FIELD, currentGameSession.getBankId());
                concreteBankGameIndexQuery.value(GAME_ID_FIELD, currentGameSession.getGameId());
                concreteBankGameIndexQuery.value(GAME_SESSION_ID_FIELD, currentGameSession.getId());
                concreteBankGameIndexQuery.value(END_TIME_FIELD, endTime);
                concreteBankGameIndexQuery.value(ACCOUNT_ID_FIELD, currentGameSession.getAccountId());
                execute(concreteBankGameIndexQuery, "rebuidBankIndex: concreteBankGameIndexQuery");
            }


            com.datastax.driver.core.querybuilder.Select selectExistIdForAllGames = getSelectColumnsQuery(BANK_GAME_INDEX_TABLE, GAME_SESSION_ID_FIELD).
                    where(eq(BANK_ID_FIELD, currentGameSession.getBankId())).
                    and(eq(GAME_ID_FIELD, ALL_GAMES_ID)).
                    and(eq(END_TIME_FIELD, currentGameSession.getEndTime())).
                    and(eq(ACCOUNT_ID_FIELD, currentGameSession.getAccountId())).limit(1);
            com.datastax.driver.core.Row forAllSelectResult = execute(selectExistIdForAllGames,
                    "rebuildBankIndex: select in bankIndexTable exist game session id for all games").one();

            if (forAllSelectResult == null || (gameSessionId = forAllSelectResult.getLong(GAME_SESSION_ID_FIELD)) == 0l ||
                    gameSessionId != currentGameSession.getId()) {

                Long endTime = currentGameSession.getEndTime();
                String key = Long.toString(currentGameSession.getAccountId()) + Long.toString(endTime);
                timeCounter = timeToCounter.get(key);
                if (timeCounter == null) {
                    timeCounter = new MutableInt(1);
                    timeToCounter.put(key, timeCounter);
                }

                endTime += timeCounter.intValue();

                LOG.debug("Not exist for all games session={}", currentGameSession);
                LOG.debug("New end time={}", endTime);

                com.datastax.driver.core.querybuilder.Insert allBankGamesIndexQuery = getInsertQuery(BANK_GAME_INDEX_TABLE, null);
                allBankGamesIndexQuery.value(BANK_ID_FIELD, currentGameSession.getBankId());
                allBankGamesIndexQuery.value(GAME_ID_FIELD, ALL_GAMES_ID);
                allBankGamesIndexQuery.value(GAME_SESSION_ID_FIELD, currentGameSession.getId());
                allBankGamesIndexQuery.value(END_TIME_FIELD, endTime);
                allBankGamesIndexQuery.value(ACCOUNT_ID_FIELD, currentGameSession.getAccountId());
                execute(allBankGamesIndexQuery, "rebuidBankIndex: allBankGamesIndexQuery");
            }

            if (timeCounter != null) {
                timeCounter.increment();
                if (timeCounter.intValue() == 999) {
                    timeCounter.setValue(1);
                }
            }

            count++;
            if (count % 1000 == 0) {
                LOG.debug("rebuidBankIndex: current count={}", count);
            }
        }
        LOG.info("rebuidBankIndex: finished, total count={}, sessionWithoutEndTimeCount={}", count,
                sessionWithoutEndTimeCount);
    }

    public void prepareToPersist(Map<com.datastax.driver.core.Session, List<com.datastax.driver.core.Statement>> statementsMap, GameSession session) {
        if (!session.isRealMoney()) {
            LOG.error("Cannot persist free mode session: {}", session);
            return;
        }
        List<com.datastax.driver.core.Statement> statements = getOrCreateStatements(statementsMap);
        com.datastax.driver.core.querybuilder.Batch batch = batch();
        statements.add(batch);
        long bankId = session.getBankId();
        com.datastax.driver.core.querybuilder.Insert query = getInsertQuery();
        if (session.getEndTime() == null) {
            getLog().warn("persist: may be error, endTime is null: {}", session);
            long endTime = NtpTimeProvider.getInstance().getTime();
            if (session.getStartTime() >= endTime) {
                endTime = session.getStartTime() + 1;
            }
            session.setEndTime(endTime);
        }
        query.value(GAME_SESSION_ID_FIELD, session.getId());
        query.value(ACCOUNT_ID_FIELD, session.getAccountId());
        query.value(GAME_ID_FIELD, session.getGameId());
        int mode;
        if (session.getFrbonusId() != null) {
            mode = MODE_FR_BONUS;
        } else if (session.getBonusId() != null) {
            mode = MODE_BONUS;
        } else {
            mode = MODE_REAL;
        }
        query.value(START_TIME_FIELD, session.getStartTime());
        query.value(END_TIME_FIELD, session.getEndTime());
        query.value(INCOME_FIELD, session.getIncome());
        query.value(PAYOUT_FIELD, session.getPayout());
        query.value(NEGATIVE_BET_FIELD, session.getNegativeBet());
        query.value(BETS_COUNT_FIELD, (int) session.getBetsCount());
        query.value(ROUNDS_COUNT_FIELD, (int) session.getRoundsCount());
        query.value(LAST_PLAYER_BET_ID_FIELD, session.getLastPlayerBetId());
        query.value(REAL_MONEY_FIELD, session.isRealMoney());
        query.value(PCR_SUM_FIELD, session.getPcrSum());
        query.value(BCR_SUM_FIELD, session.getBcrSum());
        query.value(CURRENCY_FIELD, session.getCurrency().getCode());
        if (session.getCurrencyFraction() != null) {
            query.value(CURRENCY_FRACTION_FIELD, session.getCurrencyFraction());
        }
        query.value(FR_BONUS_ID_FIELD, session.getFrbonusId() == null ? -1L : session.getFrbonusId());
        query.value(BONUS_ID_FIELD, session.getBonusId() == null ? -1L : session.getBonusId());
        if (session.getFrbonusStatus() != null) {
            query.value(FR_BONUS_STATUS_FIELD, session.getFrbonusStatus().name());
        }
        if (session.getBonusStatus() != null) {
            query.value(BONUS_STATUS_FIELD, session.getBonusStatus().name());
        }
        if (!StringUtils.isTrimmedEmpty(session.getExternalSessionId())) {
            query.value(EXT_SESSION_ID_FIELD, session.getExternalSessionId());
        }
        query.value(START_BALANCE_FIELD, session.getStartBalance());
        query.value(START_BONUS_BALANCE_FIELD, session.getStartBonusBalance());
        query.value(END_BONUS_BALANCE_FIELD, session.getEndBonusBalance());
        if (!StringUtils.isTrimmedEmpty(session.getLang())) {
            query.value(LANG_FIELD, session.getLang());
        }
        if (session.getClientType() != null) {
            query.value(CLIENT_TYPE_FIELD, session.getClientType().name());
        }
        query.value(BANK_ID_FIELD, bankId);
        query.value(BONUS_BET_FIELD, session.getBonusBet());
        query.value(BONUS_WIN_FIELD, session.getBonusWin());
        if (session.getUnjId() != null) {
            query.value(UNJ_ID_FIELD, session.getUnjId());
        }
        query.value(UNJ_SUM_CONNTRIBUTION_FIELD, session.getUnjSummaryContribution());
        query.value(UNJ_SUM_WIN_FIELD, session.getUnjSummaryWin());
        Long lastGameSessionId = getLastGameSession(session.getAccountId(), session.getGameId());
        query.value(PREV_GAME_SESSION_ID_FIELD, lastGameSessionId == null ? -1L : lastGameSessionId);
        query.value(NEXT_GAME_SESSION_ID_FIELD, -1L);
        query.value(DAY_FIELD, getDay(session));
        query.value(PROMO_IDS_FIELD, session.getPromoCampaignIds());
        query.value(ENTER_DATE_FIELD, session.getEnterDate());
        HashMap<Long, Double> contributionsJP = session.getContributionsJP();
        ByteBuffer byteBuffer = MAIN_TABLE.serializeToBytes(contributionsJP);
        String json = MAIN_TABLE.serializeToMapJson(contributionsJP, Long.class, Double.class);
        query.value(CONTRIBUTIONS_JP_FIELD, byteBuffer);
        query.value(CONTRIBUTIONS_JP_FIELD_JSON, json);
        query.value(DBL_UP_ROUNDS_COUNT_FIELD, session.getDblUpRoundsCount());
        query.value(DBL_UP_INCOME_COUNT_FIELD, session.getDblUpIncome());
        query.value(DBL_UP_PAYOUT_COUNT_FIELD, session.getDblUpPayout());
        query.value(MODEL_FIELD, session.getModel());
        batch.add(query);

        com.datastax.driver.core.querybuilder.Insert concreteGameIndexQuery = getInsertQuery(GAME_INDEX_TABLE, null);
        concreteGameIndexQuery.value(GAME_SESSION_ID_FIELD, session.getId());
        concreteGameIndexQuery.value(ACCOUNT_ID_FIELD, session.getAccountId());
        concreteGameIndexQuery.value(GAME_ID_FIELD, session.getGameId());
        LongPair gameSerialNumber = getSerialNumber(session.getAccountId(), session.getGameId(), null);
        concreteGameIndexQuery.value(GAME_SERIAL_NUMBER_FIELD, gameSerialNumber.getSecond() + 1);
        concreteGameIndexQuery.value(END_TIME_FIELD, session.getEndTime());
        batch.add(concreteGameIndexQuery);

        com.datastax.driver.core.querybuilder.Insert allGamesIndexQuery = getInsertQuery(GAME_INDEX_TABLE, null);
        allGamesIndexQuery.value(GAME_SESSION_ID_FIELD, session.getId());
        allGamesIndexQuery.value(ACCOUNT_ID_FIELD, session.getAccountId());
        allGamesIndexQuery.value(GAME_ID_FIELD, ALL_GAMES_ID);
        gameSerialNumber = getSerialNumber(session.getAccountId(), ALL_GAMES_ID, null);
        allGamesIndexQuery.value(GAME_SERIAL_NUMBER_FIELD, gameSerialNumber.getSecond() + 1);
        allGamesIndexQuery.value(END_TIME_FIELD, session.getEndTime());
        batch.add(allGamesIndexQuery);

        com.datastax.driver.core.querybuilder.Insert concreteBankGameIndexQuery = getInsertQuery(BANK_GAME_INDEX_TABLE, null);
        concreteBankGameIndexQuery.value(BANK_ID_FIELD, bankId);
        concreteBankGameIndexQuery.value(GAME_ID_FIELD, session.getGameId());
        concreteBankGameIndexQuery.value(GAME_SESSION_ID_FIELD, session.getId());
        concreteBankGameIndexQuery.value(END_TIME_FIELD, session.getEndTime());
        concreteBankGameIndexQuery.value(ACCOUNT_ID_FIELD, session.getAccountId());
        batch.add(concreteBankGameIndexQuery);

        com.datastax.driver.core.querybuilder.Insert allBankGamesIndexQuery = getInsertQuery(BANK_GAME_INDEX_TABLE, null);
        allBankGamesIndexQuery.value(BANK_ID_FIELD, bankId);
        allBankGamesIndexQuery.value(GAME_ID_FIELD, ALL_GAMES_ID);
        allBankGamesIndexQuery.value(GAME_SESSION_ID_FIELD, session.getId());
        allBankGamesIndexQuery.value(END_TIME_FIELD, session.getEndTime());
        allBankGamesIndexQuery.value(ACCOUNT_ID_FIELD, session.getAccountId());
        batch.add(allBankGamesIndexQuery);

        com.datastax.driver.core.querybuilder.Insert concreteGameAndModeIndexQuery = getInsertQuery(GAME_MODE_INDEX_TABLE, null);
        concreteGameAndModeIndexQuery.value(GAME_SESSION_ID_FIELD, session.getId());
        concreteGameAndModeIndexQuery.value(ACCOUNT_ID_FIELD, session.getAccountId());
        concreteGameAndModeIndexQuery.value(MODE_FIELD, mode);
        concreteGameAndModeIndexQuery.value(GAME_ID_FIELD, session.getGameId());
        gameSerialNumber = getSerialNumber(session.getAccountId(), session.getGameId(), mode);
        concreteGameAndModeIndexQuery.value(GAME_SERIAL_NUMBER_FIELD, gameSerialNumber.getSecond() + 1);
        concreteGameAndModeIndexQuery.value(END_TIME_FIELD, session.getEndTime());
        batch.add(concreteGameAndModeIndexQuery);

        com.datastax.driver.core.querybuilder.Insert allGamesAndModeIndexQuery = getInsertQuery(GAME_MODE_INDEX_TABLE, null);
        allGamesAndModeIndexQuery.value(GAME_SESSION_ID_FIELD, session.getId());
        allGamesAndModeIndexQuery.value(ACCOUNT_ID_FIELD, session.getAccountId());
        allGamesAndModeIndexQuery.value(MODE_FIELD, mode);
        allGamesAndModeIndexQuery.value(GAME_ID_FIELD, ALL_GAMES_ID);
        gameSerialNumber = getSerialNumber(session.getAccountId(), ALL_GAMES_ID, mode);
        allGamesAndModeIndexQuery.value(GAME_SERIAL_NUMBER_FIELD, gameSerialNumber.getSecond() + 1);
        allGamesAndModeIndexQuery.value(END_TIME_FIELD, session.getEndTime());
        batch.add(allGamesAndModeIndexQuery);

        //update prevGameSession
        if (lastGameSessionId != null) {
            com.datastax.driver.core.querybuilder.Update prevQuery = getUpdateQuery();
            prevQuery.where().and(eq(GAME_SESSION_ID_FIELD, lastGameSessionId));
            prevQuery.with(com.datastax.driver.core.querybuilder.QueryBuilder.set(NEXT_GAME_SESSION_ID_FIELD, session.getId()));
            batch.add(prevQuery);
        }
    }

    public Integer getRecordsCount(Date startDate, Date endDate) {
        return (int) count(com.datastax.driver.core.querybuilder.QueryBuilder.gte(END_TIME_FIELD, startDate == null ? 0L : startDate.getTime()),
                com.datastax.driver.core.querybuilder.QueryBuilder.lte(END_TIME_FIELD, endDate == null ? Long.MAX_VALUE : endDate.getTime()));
    }

    public List<GameSession> getRecords(Date startDate, Date endDate, int from, int count) {
        long now = System.currentTimeMillis();
        com.datastax.driver.core.querybuilder.Select query = getSelectAllColumnsQuery();
        com.datastax.driver.core.querybuilder.Select.Where where = query.where();
        where.and(com.datastax.driver.core.querybuilder.QueryBuilder.gte(END_TIME_FIELD, startDate.getTime()));
        where.and(com.datastax.driver.core.querybuilder.QueryBuilder.lte(END_TIME_FIELD, endDate.getTime()));
        query.orderBy(com.datastax.driver.core.querybuilder.QueryBuilder.asc(END_TIME_FIELD));
        query.limit(count);
        com.datastax.driver.core.ResultSet resultSet = execute(query, "getRecords");
        List<GameSession> result = new ArrayList<>();
        for (com.datastax.driver.core.Row row : resultSet) {
            GameSession session = convert(row);
            if (session == null) {
                continue;
            }
            result.add(session);
        }
        StatisticsManager.getInstance().updateRequestStatistics(getClass().getSimpleName() + " getRecords",
                System.currentTimeMillis() - now);
        return result;
    }

    public Iterable<GameSession> getRecordsByDay(Date day) {
        long now = System.currentTimeMillis();

        com.datastax.driver.core.querybuilder.Select query = getSelectAllColumnsQuery();
        com.datastax.driver.core.querybuilder.Select.Where where = query.where();
        where.and(eq(DAY_FIELD, getDay(day)));
        getLog().debug("getRecordsByDay: before tune fetchSize={}, readReadTimeout={}", query.getFetchSize(),
                query.getReadTimeoutMillis() / 1000);
        //need override, by default 90 sec
        query.setReadTimeoutMillis((int) TimeUnit.MINUTES.toMillis(60));
        query.setFetchSize(1000);
        getLog().debug("getRecordsByDay: after tune fetchSize={}, readReadTimeout={}", query.getFetchSize(),
                query.getReadTimeoutMillis() / 1000);

        com.datastax.driver.core.ResultSet resultSet = execute(query, "getRecordsByDay", 5);
        List<GameSession> result = new ArrayList<>();
        for (com.datastax.driver.core.Row row : resultSet) {
            GameSession session = convert(row);
            if (session == null) {
                continue;
            }
            result.add(session);
        }
        StatisticsManager.getInstance().updateRequestStatistics(getClass().getSimpleName() + " getRecords",
                System.currentTimeMillis() - now);
        return result;
    }

    private List<com.datastax.driver.core.querybuilder.Clause> getPlayerHistoryClauses(long accountId, List<Long> gameIds, Date startDate,
                                                 Date endDate, int mode) {
        List<com.datastax.driver.core.querybuilder.Clause> clauses = new ArrayList<>(8);
        clauses.add(eq(ACCOUNT_ID_FIELD, accountId));
        if (CollectionUtils.isEmpty(gameIds)) {
            clauses.add(eq(GAME_ID_FIELD, ALL_GAMES_ID));
        } else if (gameIds.size() == 1) {
            clauses.add(eq(GAME_ID_FIELD, gameIds.get(0)));
        } else {
            getLog().warn("getPlayerHistoryClauses: found 'IN' condition for gameIds, this may be dangerous or " +
                            "don't work!!! accountId={}, games={}, startDate={}, endDate={}, mode={}",
                    accountId, gameIds, startDate, endDate, mode);
            clauses.add(com.datastax.driver.core.querybuilder.QueryBuilder.in(GAME_ID_FIELD, gameIds.toArray()));
        }
        if (mode != MODE_ALL) {
            clauses.add(eq(MODE_FIELD, mode));
        }
        clauses.add(com.datastax.driver.core.querybuilder.QueryBuilder.gte(END_TIME_FIELD, startDate != null ? startDate.getTime() : 0));
        clauses.add(com.datastax.driver.core.querybuilder.QueryBuilder.lte(END_TIME_FIELD, endDate != null ? endDate.getTime() : Long.MAX_VALUE));
        return clauses;
    }

    public long getGameSessionsCount(long accountId, List<Long> gameIds, Date startDate,
                                     Date endDate, int mode) {
        List<com.datastax.driver.core.querybuilder.Clause> clauses = getPlayerHistoryClauses(accountId, gameIds, startDate, endDate, mode);
        if (mode == MODE_ALL) {
            return count(GAME_INDEX_TABLE, clauses);
        } else {
            return count(GAME_MODE_INDEX_TABLE, clauses);
        }
    }

    private GameSession getRefferedSession(long gameSessionId, String refferedField) {
        com.datastax.driver.core.querybuilder.Select query = getSelectColumnsQuery(refferedField);
        query.where().and(eq(getKeyColumnName(), gameSessionId));
        com.datastax.driver.core.ResultSet rows = execute(query, "getPrevSession");
        com.datastax.driver.core.Row row = rows.one();
        if (row == null || row.isNull(refferedField)) {
            return null;
        }
        long refferedId = row.getLong(refferedField);
        return refferedId <= 0 ? null : get(refferedId);
    }

    public GameSession getPrevSession(long gameSessionId) {
        return getRefferedSession(gameSessionId, PREV_GAME_SESSION_ID_FIELD);
    }

    public GameSession getNextSession(long gameSessionId) {
        return getRefferedSession(gameSessionId, NEXT_GAME_SESSION_ID_FIELD);
    }

    public Long getLastGameSession(long accountId, long gameId) {
        com.datastax.driver.core.querybuilder.Select query = getSelectAllColumnsQuery(GAME_INDEX_TABLE);
        query.where().and(eq(GAME_ID_FIELD, gameId)).and(eq(ACCOUNT_ID_FIELD, accountId)).limit(1);
        query.orderBy(com.datastax.driver.core.querybuilder.QueryBuilder.desc(END_TIME_FIELD));
        com.datastax.driver.core.ResultSet resultSet = execute(query, "getLastGameSession");
        com.datastax.driver.core.Row row = resultSet.one();
        Long gameSessionId = null;
        if (row != null) {
            gameSessionId = row.getLong(GAME_SESSION_ID_FIELD);
        }
        return gameSessionId == null || gameSessionId <= 0 ? null : gameSessionId;
    }

    public List<GameSession> getGameSessions(List<Long> gameSessionIds) {
        final List<GameSession> result = new ArrayList<>(gameSessionIds.size());
        processGameSessions(gameSessionIds, result::add);
        return result;
    }

    public void processGameSessions(List<Long> gameSessionIds, IGameSessionProcessor processor) {
        int chunkSize = IN_CLAUSE_SIZE;
        int chunkCount = gameSessionIds.size() / chunkSize + (gameSessionIds.size() % chunkSize != 0 ? 1 : 0);
        for (int i = 0; i < chunkCount; i++) {
            int startIndex = i * chunkSize;
            int endIndex = startIndex + chunkSize;
            if (i == chunkCount - 1) {
                endIndex = gameSessionIds.size();
            }
            com.datastax.driver.core.querybuilder.Select query = getSelectAllColumnsQuery();
            query.where().and(com.datastax.driver.core.querybuilder.QueryBuilder.in(GAME_SESSION_ID_FIELD, gameSessionIds.subList(startIndex, endIndex).toArray()));
            com.datastax.driver.core.ResultSet resultSet = execute(query, "getGameSessionList");
            for (com.datastax.driver.core.Row row : resultSet) {
                GameSession gameSession = convert(row);
                if (gameSession != null) {
                    processor.process(gameSession);
                }
            }
        }
    }

    public void processGameSessionsForAccount(long accountId, List<Long> gameIds, Date startDate, Date endDate,
                                              int mode, IGameSessionProcessor processor) {
        com.datastax.driver.core.querybuilder.Select selectFromIndexTable = getSelect(accountId, gameIds, startDate, endDate, mode);
        selectFromIndexTable.setFetchSize(1000);
        com.datastax.driver.core.ResultSet result = execute(selectFromIndexTable, "selectAccountGameSessions");
        List<Long> gameSessionIds = StreamUtils.asStream(result)
                .map(record -> record.getLong(GAME_SESSION_ID_FIELD))
                .collect(Collectors.toList());
        processGameSessions(gameSessionIds, processor);
    }

    public List<GameSession> getGameSessionList(long accountId, List<Long> gameIds, Date startDate,
                                                Date endDate, int from, int count, int mode) {
        long now = System.currentTimeMillis();
        getLog().debug("getGameSessionList: accountId={}, gameIds={}, startDate={}, endDate={}, from={}, count={}, mode={}",
                accountId, gameIds, startDate, endDate, from, count, mode);
        Date correctedEndDate = null;
        com.datastax.driver.core.querybuilder.Select lastRecordSelect = getSelect(accountId, gameIds, startDate, endDate, mode);
        lastRecordSelect.limit(1);
        com.datastax.driver.core.Row lastRow = execute(lastRecordSelect, "getGameSessionList: lastRecordSelect").one();
        if (lastRow != null) {
            long lastNumber = lastRow.getLong(GAME_SERIAL_NUMBER_FIELD);
            long lastGameSessionId = lastRow.getLong(GAME_SESSION_ID_FIELD);
            getLog().debug("getGameSessionList: lastGameSessionId={}, lastNumber={}", lastGameSessionId, lastNumber);
            if (lastNumber > from) {
                com.datastax.driver.core.querybuilder.Select query = getSelectColumnsQuery(mode == MODE_ALL ? GAME_INDEX_TABLE : GAME_MODE_INDEX_TABLE,
                        GAME_SESSION_ID_FIELD, END_TIME_FIELD);
                query.where().and(eq(ACCOUNT_ID_FIELD, accountId));
                if (CollectionUtils.isEmpty(gameIds)) {
                    query.where().and(eq(GAME_ID_FIELD, ALL_GAMES_ID));
                } else {
                    query.where().and(eq(GAME_ID_FIELD, gameIds.get(0)));
                }
                if (mode != MODE_ALL) {
                    query.where().and(eq(MODE_FIELD, mode));
                }
                long newLastNumber = lastNumber - from;
                query.where().and(eq(GAME_SERIAL_NUMBER_FIELD, newLastNumber));
                com.datastax.driver.core.Row newLastRow = execute(query, "getGameSessionList: find newLastRow").one();
                if (newLastRow != null) {
                    long newLastGameSessionId = newLastRow.getLong(GAME_SESSION_ID_FIELD);
                    long newLastEndDate = newLastRow.getLong(END_TIME_FIELD);
                    getLog().debug("getGameSessionList: findNewLast: newLastGameSessionId={}, newLastEndDate={}, " +
                            "newLastNumber={}", newLastGameSessionId, newLastEndDate, newLastNumber);
                    if (newLastEndDate > 0) {
                        correctedEndDate = new Date(newLastEndDate);
                    }
                } else {
                    getLog().warn("getGameSessionList: cannot find newLastRow: accountId={}, gameIds={}, " +
                                    "from={}, count={}, mode={}, newLastNumber={}, lastNumber={}",
                            accountId, Arrays.toString(gameIds.toArray()), from, count, mode, newLastNumber, lastNumber);
                }
            }
        }
        com.datastax.driver.core.querybuilder.Select mainQuery = getSelect(accountId, gameIds, startDate,
                correctedEndDate != null ? correctedEndDate : endDate, mode);
        mainQuery.limit(count);
        com.datastax.driver.core.ResultSet resultSet = execute(mainQuery, "getGameSessionList");
        List<Long> gameSessionIds = new ArrayList<>(count);
        for (com.datastax.driver.core.Row row : resultSet) {
            long gameSessionId = row.getLong(GAME_SESSION_ID_FIELD);
            gameSessionIds.add(gameSessionId);
        }
        List<GameSession> result = getGameSessions(gameSessionIds);
        StatisticsManager.getInstance().updateRequestStatistics("CassandraGameSessionPersister getGameSessionList",
                System.currentTimeMillis() - now);
        return result;
    }

    private com.datastax.driver.core.querybuilder.Select getSelect(long accountId, List<Long> gameIds, Date startDate, Date endDate, int mode) {
        com.datastax.driver.core.querybuilder.Select query = getSelectColumnsQuery(mode == MODE_ALL ? GAME_INDEX_TABLE : GAME_MODE_INDEX_TABLE,
                GAME_SESSION_ID_FIELD, GAME_SERIAL_NUMBER_FIELD);
        com.datastax.driver.core.querybuilder.Select.Where where = query.where();
        List<com.datastax.driver.core.querybuilder.Clause> clauses = getPlayerHistoryClauses(accountId, gameIds, startDate, endDate, mode);
        for (com.datastax.driver.core.querybuilder.Clause clause : clauses) {
            where.and(clause);
        }
        query.orderBy(com.datastax.driver.core.querybuilder.QueryBuilder.desc(END_TIME_FIELD));
        return query;
    }

    public List<GameSession> getAccountGameSessionList(Long accountId, Date startDate, Date endDate) {
        long now = System.currentTimeMillis();
        List<GameSession> result = getGameSessionList(accountId, null, startDate, endDate, 0, 100000, MODE_ALL);
        StatisticsManager.getInstance().updateRequestStatistics("CassandraGameSessionPersister: " +
                "getAccountGameSessionList", System.currentTimeMillis() - now);
        return result;
    }

    public Iterable<Long> getAllAccountGameSessionsIds(long accountId) {
        com.datastax.driver.core.querybuilder.Select select = getSelectColumnsQuery(GAME_INDEX_TABLE, GAME_SESSION_ID_FIELD);
        select.where(eq(GAME_ID_FIELD, ALL_GAMES_ID))
                .and(eq(ACCOUNT_ID_FIELD, accountId));

        return StreamUtils.asStream(execute(select, "com.datastax.driver.core.querybuilder.Select all accounts game sessions"))
                .map(row -> row.getLong(GAME_SESSION_ID_FIELD))
                .collect(Collectors.toList());
    }

    public List<Long> getBankGameSessionsIds(long bankId, Long gameId, Date startDate, Date endDate) {
        long now = System.currentTimeMillis();
        com.datastax.driver.core.querybuilder.Select query = getSelectColumnsQuery(BANK_GAME_INDEX_TABLE, GAME_SESSION_ID_FIELD);
        com.datastax.driver.core.querybuilder.Select.Where where = query.where();
        where.and(eq(BANK_ID_FIELD, bankId));
        where.and(eq(GAME_ID_FIELD, gameId != null ? gameId : ALL_GAMES_ID));
        where.and(com.datastax.driver.core.querybuilder.QueryBuilder.gte(END_TIME_FIELD, startDate != null ? startDate.getTime() : 0));
        where.and(com.datastax.driver.core.querybuilder.QueryBuilder.lte(END_TIME_FIELD, endDate != null ? endDate.getTime() : Long.MAX_VALUE));
        query.orderBy(com.datastax.driver.core.querybuilder.QueryBuilder.desc(END_TIME_FIELD));
        com.datastax.driver.core.ResultSet resultSet = execute(query, "getBankGameSessionsIds");
        List<Long> gameSessionIds = new ArrayList<>();
        for (com.datastax.driver.core.Row row : resultSet) {
            long gameSessionId = row.getLong(GAME_SESSION_ID_FIELD);
            gameSessionIds.add(gameSessionId);
        }

        StatisticsManager.getInstance().updateRequestStatistics("CassandraGameSessionPersister: " +
                "getBankGameSessionsIds", System.currentTimeMillis() - now);
        return gameSessionIds;
    }

    public List<Long> getBankActiveAccountIds(long bankId, Long gameId, Date startDate, Date endDate) {
        long now = System.currentTimeMillis();
        int count = (int) count(BANK_GAME_INDEX_TABLE, eq(BANK_ID_FIELD, bankId),
                eq(GAME_ID_FIELD, gameId != null ? gameId : ALL_GAMES_ID),
                com.datastax.driver.core.querybuilder.QueryBuilder.gte(END_TIME_FIELD, startDate != null ? startDate.getTime() : 0),
                com.datastax.driver.core.querybuilder.QueryBuilder.lte(END_TIME_FIELD, endDate != null ? endDate.getTime() : Long.MAX_VALUE));
        LOG.debug("getBankActiveAccountIds: count={}", count);

        com.datastax.driver.core.querybuilder.Select query = getSelectColumnsQuery(BANK_GAME_INDEX_TABLE, ACCOUNT_ID_FIELD);
        com.datastax.driver.core.querybuilder.Select.Where where = query.where();
        where.and(eq(BANK_ID_FIELD, bankId));
        where.and(eq(GAME_ID_FIELD, gameId != null ? gameId : ALL_GAMES_ID));
        where.and(com.datastax.driver.core.querybuilder.QueryBuilder.gte(END_TIME_FIELD, startDate != null ? startDate.getTime() : 0));
        where.and(com.datastax.driver.core.querybuilder.QueryBuilder.lte(END_TIME_FIELD, endDate != null ? endDate.getTime() : Long.MAX_VALUE));
        query.orderBy(com.datastax.driver.core.querybuilder.QueryBuilder.desc(END_TIME_FIELD));
        com.datastax.driver.core.ResultSet resultSet = execute(query, "getBankActiveAccountIds");
        List<Long> accountIds = new ArrayList<>(count);
        for (com.datastax.driver.core.Row row : resultSet) {
            long accountId = row.getLong(ACCOUNT_ID_FIELD);
            accountIds.add(accountId);
        }

        StatisticsManager.getInstance().updateRequestStatistics("CassandraGameSessionPersister " +
                "getBankActiveAccountIds", System.currentTimeMillis() - now);
        return accountIds;
    }

    public GameSession get(long gameSessionId) {
        com.datastax.driver.core.querybuilder.Select query = getSelectAllColumnsQuery();
        query.where(eq(getKeyColumnName(), gameSessionId));
        com.datastax.driver.core.ResultSet resultSet = execute(query, "get");
        return convert(resultSet.one());
    }

    public GameSession getByExternalId(String externalId, long bankId) {
        long now = System.currentTimeMillis();
        com.datastax.driver.core.querybuilder.Select query = getSelectAllColumnsQuery();
        query.where(eq(EXT_SESSION_ID_FIELD, externalId));
        com.datastax.driver.core.ResultSet resultSet = execute(query, "getByExternalId");
        GameSession resultSession = null;
        for (com.datastax.driver.core.Row row : resultSet) {
            GameSession gameSession = convert(row);
            if (gameSession == null) {
                continue;
            }
            if (gameSession.getBankId() == bankId) {
                resultSession = gameSession;
                break;
            }
        }
        StatisticsManager.getInstance().updateRequestStatistics("CassandraGameSessionPersister: getByExternalId",
                System.currentTimeMillis() - now);
        return resultSession;
    }

    private GameSession convert(com.datastax.driver.core.Row row) {
        if (row == null) {
            return null;
        }

        long gameSessionId = row.getLong(GAME_SESSION_ID_FIELD);
        assert gameSessionId > 0;
        long accountId = row.getLong(ACCOUNT_ID_FIELD);
        long bankId = row.getLong(BANK_ID_FIELD);
        //for removed/expired GameSession, all fields is null
        if (accountId == 0 || bankId == 0) {
            return null;
        }
        long gameId = row.getLong(GAME_ID_FIELD);
        long startTime = row.getLong(START_TIME_FIELD);
        Long endTime = row.getLong(END_TIME_FIELD);
        if (endTime <= 0) {
            endTime = null;
        }
        long income = row.getLong(INCOME_FIELD);
        long payout = row.getLong(PAYOUT_FIELD);
        int betsCount = row.getInt(BETS_COUNT_FIELD);
        int roundsCount = row.getInt(ROUNDS_COUNT_FIELD);
        boolean realMoney = row.getBool(REAL_MONEY_FIELD);
        String currencyCode = row.getString(CURRENCY_FIELD);
        Currency currency = null;
        if (!StringUtils.isTrimmedEmpty(currencyCode)) {
            currency = CurrencyCache.getInstance().get(currencyCode);
        }
        String currencyFraction = row.getString(CURRENCY_FRACTION_FIELD);
        String externalSessionId = row.getString(EXT_SESSION_ID_FIELD);
        String lang = row.getString(LANG_FIELD);
        long negativeBet = row.getLong(NEGATIVE_BET_FIELD);
        long lastPlayerBetId = row.getLong(LAST_PLAYER_BET_ID_FIELD);
        double pcrSum = row.getDouble(PCR_SUM_FIELD);
        double bcrSum = row.getDouble(BCR_SUM_FIELD);

        Long bonusId = null;
        if (!row.isNull(BONUS_ID_FIELD)) {
            bonusId = row.getLong(BONUS_ID_FIELD);
        }

        Long frbonusId = null;
        if (!row.isNull(FR_BONUS_ID_FIELD)) {
            frbonusId = row.getLong(FR_BONUS_ID_FIELD);
        }

        BonusStatus bonusStatus = null;
        String bonusStatusCode = row.getString(BONUS_STATUS_FIELD);
        if (!StringUtils.isTrimmedEmpty(bonusStatusCode)) {
            bonusStatus = BonusStatus.valueOf(bonusStatusCode);
        }

        BonusStatus frbonusStatus = null;
        String frbonusStatusCode = row.getString(FR_BONUS_STATUS_FIELD);
        if (!StringUtils.isTrimmedEmpty(frbonusStatusCode)) {
            frbonusStatus = BonusStatus.valueOf(frbonusStatusCode);
        }
        long startBalance = row.getLong(START_BALANCE_FIELD);
        long startBonusBalance = row.getLong(START_BONUS_BALANCE_FIELD);
        long endBonusBalance = row.getLong(END_BONUS_BALANCE_FIELD);

        ClientType clientType = null;
        String clientTypeCode = row.getString(CLIENT_TYPE_FIELD);
        if (!StringUtils.isTrimmedEmpty(clientTypeCode)) {
            clientType = ClientType.valueOf(clientTypeCode);
        }
        long bonusBet = row.getLong(BONUS_BET_FIELD);
        long bonusWin = row.getLong(BONUS_WIN_FIELD);
        long unjIdWin = row.getLong(UNJ_ID_FIELD);
        double unjSumContribution = row.getDouble(UNJ_SUM_CONNTRIBUTION_FIELD);
        long unjSummaryWin = row.getLong(UNJ_SUM_WIN_FIELD);
        List<Long> promoIds = row.getList(PROMO_IDS_FIELD, Long.class);
        ByteBuffer byteBuffer = row.getBytes(CONTRIBUTIONS_JP_FIELD);
        String json = row.getString(CONTRIBUTIONS_JP_FIELD_JSON);
        Map<Long, Double> contributionsJP = MAIN_TABLE.deserializeToMapJson(json, Long.class, Double.class);

        if (contributionsJP == null) {
            contributionsJP = MAIN_TABLE.deserializeFrom(byteBuffer, HashMap.class);
        }
        long enterDate = row.getLong(ENTER_DATE_FIELD);
        int dblUpRoundsCount = row.getInt(DBL_UP_ROUNDS_COUNT_FIELD);
        long dblUpIncome = row.getLong(DBL_UP_INCOME_COUNT_FIELD);
        long dblUpPayout = row.getLong(DBL_UP_PAYOUT_COUNT_FIELD);
        Double model = row.get(MODEL_FIELD, Double.class);

        GameSession gameSession = new GameSession(gameSessionId, accountId, bankId, gameId, startTime, endTime,
                income, payout, betsCount, roundsCount, false, realMoney, currency,
                externalSessionId, lang, bonusId, frbonusId, bonusBet, bonusWin,
                unjSumContribution);
        gameSession.setNegativeBet(negativeBet);
        gameSession.setLastPlayerBetId(lastPlayerBetId);
        gameSession.setPcrSum(pcrSum);
        gameSession.setBcrSum(bcrSum);
        gameSession.setBonusStatus(bonusStatus);
        gameSession.setFrbonusStatus(frbonusStatus);
        gameSession.setStartBalance(startBalance);
        gameSession.setStartBonusBalance(startBonusBalance);
        gameSession.setEndBonusBalance(endBonusBalance);
        gameSession.setClientType(clientType);
        gameSession.setUnjId(unjIdWin);
        gameSession.setCurrencyFraction(currencyFraction);
        gameSession.incrementUnjSummaryWin(unjSummaryWin);
        gameSession.setPromoCampaignIds(promoIds);
        if (contributionsJP != null) {
            gameSession.setContributionsJP(new HashMap<>(contributionsJP));
        }
        if (!row.isNull(ENTER_DATE_FIELD)) {
            gameSession.setEnterDate(enterDate);
        }
        gameSession.setDblUpRoundsCount(dblUpRoundsCount);
        gameSession.setDblUpIncome(dblUpIncome);
        gameSession.setDblUpPayout(dblUpPayout);
        gameSession.setModel(model);
        return gameSession;
    }

    @Override
    public GameSession getObject(String id) {
        return get(Long.valueOf(id));
    }

    public GameSession getById(Long id) {
        return get(id);
    }

    @Override
    public Map<String, GameSession> getAllObjects() {
        //too large
        return Collections.emptyMap();
    }

    @Override
    public String getAdditionalInfo() {
        return null;
    }

    @Override
    public String printDebug() {
        return null;
    }

    private long getDay(GameSession session) {
        return getDay(new Date(session.getEndTime()));
    }

    private long getDay(Date time) {
        return CalendarUtils.getEndDay(time, "GMT").getTimeInMillis();
    }

    public boolean delete(GameSession gameSession) {
        long accountId = gameSession.getAccountId();
        long gameId = gameSession.getGameId();
        Long endTime = gameSession.getEndTime();
        long bankId = gameSession.getBankId();

        int mode;
        if (gameSession.getFrbonusId() != null) {
            mode = MODE_FR_BONUS;
        } else if (gameSession.getBonusId() != null) {
            mode = MODE_BONUS;
        } else {
            mode = MODE_REAL;
        }

        com.datastax.driver.core.querybuilder.Batch batch = batch();
        {
            com.datastax.driver.core.querybuilder.Delete allGamesAndModeIndex = addItemDeletion(GAME_MODE_INDEX_TABLE.getTableName(),
                    eq(ACCOUNT_ID_FIELD, accountId),
                    eq(MODE_FIELD, mode),
                    eq(GAME_ID_FIELD, ALL_GAMES_ID),
                    eq(END_TIME_FIELD, endTime)
            );
            batch.add(allGamesAndModeIndex);
            com.datastax.driver.core.querybuilder.Delete concreteGameAndModeIndexDelete = addItemDeletion(GAME_MODE_INDEX_TABLE.getTableName(),
                    eq(ACCOUNT_ID_FIELD, accountId),
                    eq(MODE_FIELD, mode),
                    eq(GAME_ID_FIELD, gameId),
                    eq(END_TIME_FIELD, endTime)
            );
            batch.add(concreteGameAndModeIndexDelete);
            com.datastax.driver.core.querybuilder.Delete allBankGamesIndexDelete = addItemDeletion(BANK_GAME_INDEX_TABLE.getTableName(),
                    eq(BANK_ID_FIELD, bankId),
                    eq(GAME_ID_FIELD, ALL_GAMES_ID),
                    eq(END_TIME_FIELD, endTime),
                    eq(ACCOUNT_ID_FIELD, gameSession.getAccountId())
            );
            batch.add(allBankGamesIndexDelete);
            com.datastax.driver.core.querybuilder.Delete concreteBankGameIndexDelete = addItemDeletion(BANK_GAME_INDEX_TABLE.getTableName(),
                    eq(BANK_ID_FIELD, bankId),
                    eq(GAME_ID_FIELD, gameId),
                    eq(END_TIME_FIELD, endTime),
                    eq(ACCOUNT_ID_FIELD, gameSession.getAccountId())
            );
            batch.add(concreteBankGameIndexDelete);
            com.datastax.driver.core.querybuilder.Delete allGamesIndexDelete = addItemDeletion(GAME_INDEX_TABLE.getTableName(),
                    eq(ACCOUNT_ID_FIELD, accountId),
                    eq(GAME_ID_FIELD, ALL_GAMES_ID),
                    eq(END_TIME_FIELD, endTime)
            );
            batch.add(allGamesIndexDelete);
            com.datastax.driver.core.querybuilder.Delete concreteGameIndexDelete = addItemDeletion(GAME_INDEX_TABLE.getTableName(),
                    eq(ACCOUNT_ID_FIELD, accountId),
                    eq(GAME_ID_FIELD, gameId),
                    eq(END_TIME_FIELD, endTime)
            );
            batch.add(concreteGameIndexDelete);
            com.datastax.driver.core.querybuilder.Delete mainDelete = addItemDeletion(
                    MAIN_TABLE.getTableName(),
                    eq(GAME_SESSION_ID_FIELD, gameSession.getId())
            );
            batch.add(mainDelete);
        }
        com.datastax.driver.core.ResultSet delete = execute(batch, "delete");
        return delete.wasApplied();
    }


    public boolean delete(ShortGameSessionInfo info) {
        long accountId = info.accountId;
        long gameId = info.gameId;
        long endTime = info.endTime;
        long bankId = info.bankId;
        long id = info.id;
        int mode = info.mode;

        com.datastax.driver.core.querybuilder.Batch batch = batch();
        {
            com.datastax.driver.core.querybuilder.Delete allGamesAndModeIndex = addItemDeletion(GAME_MODE_INDEX_TABLE.getTableName(),
                    eq(ACCOUNT_ID_FIELD, accountId),
                    eq(MODE_FIELD, mode),
                    eq(GAME_ID_FIELD, ALL_GAMES_ID),
                    eq(END_TIME_FIELD, endTime)
            );
            batch.add(allGamesAndModeIndex);
            com.datastax.driver.core.querybuilder.Delete concreteGameAndModeIndexDelete = addItemDeletion(GAME_MODE_INDEX_TABLE.getTableName(),
                    eq(ACCOUNT_ID_FIELD, accountId),
                    eq(MODE_FIELD, mode),
                    eq(GAME_ID_FIELD, gameId),
                    eq(END_TIME_FIELD, endTime)
            );
            batch.add(concreteGameAndModeIndexDelete);
            com.datastax.driver.core.querybuilder.Delete allBankGamesIndexDelete = addItemDeletion(BANK_GAME_INDEX_TABLE.getTableName(),
                    eq(BANK_ID_FIELD, bankId),
                    eq(GAME_ID_FIELD, ALL_GAMES_ID),
                    eq(END_TIME_FIELD, endTime),
                    eq(ACCOUNT_ID_FIELD, accountId)
            );
            batch.add(allBankGamesIndexDelete);
            com.datastax.driver.core.querybuilder.Delete concreteBankGameIndexDelete = addItemDeletion(BANK_GAME_INDEX_TABLE.getTableName(),
                    eq(BANK_ID_FIELD, bankId),
                    eq(GAME_ID_FIELD, gameId),
                    eq(END_TIME_FIELD, endTime),
                    eq(ACCOUNT_ID_FIELD, accountId)
            );
            batch.add(concreteBankGameIndexDelete);
            com.datastax.driver.core.querybuilder.Delete allGamesIndexDelete = addItemDeletion(GAME_INDEX_TABLE.getTableName(),
                    eq(ACCOUNT_ID_FIELD, accountId),
                    eq(GAME_ID_FIELD, ALL_GAMES_ID),
                    eq(END_TIME_FIELD, endTime)
            );
            batch.add(allGamesIndexDelete);
            com.datastax.driver.core.querybuilder.Delete concreteGameIndexDelete = addItemDeletion(GAME_INDEX_TABLE.getTableName(),
                    eq(ACCOUNT_ID_FIELD, accountId),
                    eq(GAME_ID_FIELD, gameId),
                    eq(END_TIME_FIELD, endTime)
            );
            batch.add(concreteGameIndexDelete);
            com.datastax.driver.core.querybuilder.Delete mainDelete = addItemDeletion(
                    MAIN_TABLE.getTableName(),
                    eq(GAME_SESSION_ID_FIELD, id)
            );
            batch.add(mainDelete);
        }
        com.datastax.driver.core.ResultSet delete = execute(batch, "delete");
        return delete.wasApplied();
    }

    public static class ShortGameSessionInfo {
        private final long id;
        private final long accountId;
        private final long gameId;
        private final long endTime;
        private final long bankId;
        private final int mode;

        public ShortGameSessionInfo(GameSession gameSession) {
            id = gameSession.getId();
            accountId = gameSession.getAccountId();
            gameId = gameSession.getGameId();
            endTime = gameSession.getEndTime();
            bankId = gameSession.getBankId();
            if (gameSession.getFrbonusId() != null) {
                mode = MODE_FR_BONUS;
            } else if (gameSession.getBonusId() != null) {
                mode = MODE_BONUS;
            } else {
                mode = MODE_REAL;
            }
        }
    }
}
