package com.abs.casino.cassandra.persist;

import com.abs.casino.cassandra.persist.engine.Cql;

import com.abs.casino.cassandra.persist.engine.AbstractCassandraPersister;
import com.abs.casino.cassandra.persist.engine.ColumnDefinition;
import com.abs.casino.cassandra.persist.engine.ICassandraPersister;
import com.abs.casino.cassandra.persist.engine.TableDefinition;
import static com.abs.casino.cassandra.persist.engine.CassandraDataTypes.*;
import com.abs.casino.common.cache.CacheKeyInfo;
import com.abs.casino.common.cache.IDistributedCache;
import com.abs.casino.common.cache.data.account.LasthandInfo;
import com.abs.casino.common.cache.data.bonus.BonusSystemType;
import com.abs.casino.common.util.Pair;
import com.abs.casino.common.util.string.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.regex.Pattern;

/**
 * User: flsh
 * Date: 3/28/12
 */
@CacheKeyInfo(description = "account.id + game.id [ + bonus.id + bonusSystem.type ]")
public class CassandraLasthandPersister extends AbstractCassandraPersister<String, String>
        implements IDistributedCache<String, LasthandInfo> {
    public static final String REAL_LASTHAND_CF = "lasthand_cf";
    public static final String BONUS_LASTHAND_CF = "lasthand_bonus_cf";
    public static final String ACCOUNT_ID_FIELD = "AccountId";
    public static final String GAME_ID_FIELD = "GameId";
    public static final String BONUS_ID_FIELD = "BonusId";
    public static final String BONUS_TYPE_FIELD = "BonusType";
    public static final String LASTHAND_DATA_FIELD = "LH";
    public static final String FRB_SYSTEM_FLAG = "FRB";
    private static final Logger LOG = LogManager.getLogger(CassandraLasthandPersister.class);

    private static final TableDefinition REAL_TABLE = new TableDefinition(REAL_LASTHAND_CF,
            Arrays.asList(
                    new ColumnDefinition(ACCOUNT_ID_FIELD, bigint(), false, false, true),
                    new ColumnDefinition(GAME_ID_FIELD, bigint(), false, false, true),
                    new ColumnDefinition(LASTHAND_DATA_FIELD, text())
            ), ACCOUNT_ID_FIELD);

    private static final TableDefinition BONUS_TABLE = new TableDefinition(BONUS_LASTHAND_CF,
            Arrays.asList(
                    new ColumnDefinition(ACCOUNT_ID_FIELD, bigint(), false, false, true),
                    new ColumnDefinition(BONUS_TYPE_FIELD, cint(), false, false, true),
                    new ColumnDefinition(BONUS_ID_FIELD, bigint(), false, false, true),
                    new ColumnDefinition(GAME_ID_FIELD, bigint(), false, false, true),
                    new ColumnDefinition(LASTHAND_DATA_FIELD, text())
            ), ACCOUNT_ID_FIELD, BONUS_TYPE_FIELD, BONUS_ID_FIELD);

    private CassandraLasthandPersister() {
        super();
    }

    @Override
    public TableDefinition getMainTableDefinition() {
        return REAL_TABLE;
    }

    @Override
    public List<TableDefinition> getAllTableDefinitions() {
        return Arrays.asList(REAL_TABLE, BONUS_TABLE);
    }

    @Override
    public Logger getLog() {
        return LOG;
    }

    public void persist(long accountId, long gameId, Long bonusId, String lasthandData,
                        BonusSystemType bonusSystemType) {
        execute(getPersistStatement(accountId, gameId, bonusSystemType, bonusId, lasthandData), "persist");
        if (LOG.isDebugEnabled()) {
            LOG.debug("persist: accountId={}, gameId={}, bonusType={}, bonusId={}, lasthand={}",
                    accountId, gameId, bonusSystemType, bonusId, lasthandData);
        }
    }

    public void prepareToPersist(Map<com.abs.casino.cassandra.persist.engine.Session, List<com.datastax.driver.core.Statement>> statementsMap, long accountId, long gameId, Long bonusId, String lasthandData,
                                 BonusSystemType bonusSystemType) {
        List<com.datastax.driver.core.Statement> statements = getOrCreateStatements(statementsMap);
        statements.add(getPersistStatement(accountId, gameId, bonusSystemType, bonusId, lasthandData));
        if (LOG.isDebugEnabled()) {
            LOG.debug("prepareToPersist: accountId={}, gameId={}, bonusType={}, bonusId={}, lasthand={}",
                    accountId, gameId, bonusSystemType, bonusId, lasthandData);
        }
    }

    protected com.datastax.driver.core.Statement getPersistStatement(long accountId, long gameId, BonusSystemType bonusSystemType, Long bonusId, String lasthandData) {
        if (bonusSystemType == null) {
            return getInsertQuery(REAL_TABLE, getTtl() > 0 ? getTtl() : null).
                    value(ACCOUNT_ID_FIELD, accountId).
                    value(GAME_ID_FIELD, gameId).
                    value(LASTHAND_DATA_FIELD, lasthandData);
        }
        assert bonusId != null : "BonusId is null for bonusSystemType=" + bonusSystemType;
        return getInsertQuery(BONUS_TABLE, getTtl() > 0 ? getTtl() : null).
                value(ACCOUNT_ID_FIELD, accountId).
                value(BONUS_TYPE_FIELD, bonusSystemType.ordinal()).
                value(BONUS_ID_FIELD, bonusId).
                value(GAME_ID_FIELD, gameId).
                value(LASTHAND_DATA_FIELD, lasthandData);
    }

    public void delete(long accountId, long gameId, Long bonusId, BonusSystemType bonusSystemType) {
        persist(accountId, gameId, bonusId, "", bonusSystemType);
    }

    public void prepareToDeletion(Map<com.abs.casino.cassandra.persist.engine.Session, List<com.datastax.driver.core.Statement>> statementsMap, long accountId, long gameId,
                                  Long bonusId, BonusSystemType bonusSystemType) {
        prepareToPersist(statementsMap, accountId, gameId, bonusId, "", bonusSystemType);
    }

    public void delete(long accountId, BonusSystemType bonusSystemType, Long bonusId) {
        execute(addItemDeletion(BONUS_LASTHAND_CF,
                eq(ACCOUNT_ID_FIELD, accountId),
                eq(BONUS_TYPE_FIELD, bonusSystemType.ordinal()),
                eq(BONUS_ID_FIELD, bonusId)), "delete by bonusType&bonusId");
    }

    //key: gameId, value is pair writetime/lasthand
    public Map<Long, Pair<Long, String>> getRealModeLasthandsWithWriteTime(long accountId) {
        com.datastax.driver.core.Statement query = Cql.select().column(LASTHAND_DATA_FIELD).column(GAME_ID_FIELD).writeTime(LASTHAND_DATA_FIELD)
                .from(REAL_TABLE.getTableName()).where().and(eq(ACCOUNT_ID_FIELD, accountId)).limit(10000);
        com.abs.casino.cassandra.persist.engine.ResultSet rows = executeWrapped(query, "getRealModeLasthandsWithWriteTime");
        Map<Long, Pair<Long, String>> result = new HashMap<>();
        for (com.abs.casino.cassandra.persist.engine.Row row : rows) {
            String lasthand = row.getString(LASTHAND_DATA_FIELD);
            Long writeTime = row.getLong("writetime(" + LASTHAND_DATA_FIELD + ")");
            if (!StringUtils.isTrimmedEmpty(lasthand)) {
                Long gameId = row.getLong(GAME_ID_FIELD);
                result.put(gameId, new Pair<Long, String>(writeTime, lasthand));
            }
        }
        return result;
    }

    public Map<Long, String> getRealModeLasthands(long accountId) {
        com.datastax.driver.core.Statement query = getSelectColumnsQuery(REAL_TABLE, LASTHAND_DATA_FIELD, GAME_ID_FIELD)
                .where().and(eq(ACCOUNT_ID_FIELD, accountId)).limit(10000);
        com.abs.casino.cassandra.persist.engine.ResultSet rows = executeWrapped(query, "get from REAL");
        Map<Long, String> result = new HashMap<>();
        for (com.abs.casino.cassandra.persist.engine.Row row : rows) {
            String lasthand = row.getString(LASTHAND_DATA_FIELD);
            if (!StringUtils.isTrimmedEmpty(lasthand)) {
                Long gameId = row.getLong(GAME_ID_FIELD);
                result.put(gameId, lasthand);
            }
        }
        return result;
    }

    public String get(long accountId, long gameId, Long bonusId, BonusSystemType bonusSystemType) {
        if (bonusSystemType == null) {
            com.datastax.driver.core.Statement query = getSelectColumnsQuery(REAL_TABLE, LASTHAND_DATA_FIELD)
                    .where()
                    .and(eq(ACCOUNT_ID_FIELD, accountId))
                    .and(eq(GAME_ID_FIELD, gameId))
                    .limit(1);
            com.abs.casino.cassandra.persist.engine.Row row = executeWrapped(query, "get from REAL").one();
            return row == null ? null : row.getString(LASTHAND_DATA_FIELD);
        }
        assert bonusId != null : "BonusId is null for bonusSystemType=" + bonusSystemType;
        com.datastax.driver.core.Statement query = getSelectColumnsQuery(BONUS_TABLE, LASTHAND_DATA_FIELD)
                .where()
                .and(eq(ACCOUNT_ID_FIELD, accountId))
                .and(eq(BONUS_TYPE_FIELD, bonusSystemType.ordinal()))
                .and(eq(BONUS_ID_FIELD, bonusId))
                .and(eq(GAME_ID_FIELD, gameId))
                .limit(1);
        com.abs.casino.cassandra.persist.engine.Row row = executeWrapped(query, "get from BONUS").one();
        return row == null ? null : row.getString(LASTHAND_DATA_FIELD);
    }

    public String composeKey(long accountId, long gameId, Long bonusId, BonusSystemType bonusSystemType) {
        if (bonusId == null) {
            return String.valueOf(accountId) + ICassandraPersister.ID_DELIMITER + String.valueOf(gameId);
        } else {
            if (bonusSystemType == BonusSystemType.ORDINARY_SYSTEM) {
                return String.valueOf(accountId) + ICassandraPersister.ID_DELIMITER + String.valueOf(gameId) +
                        ICassandraPersister.ID_DELIMITER + String.valueOf(bonusId);
            } else {
                return String.valueOf(accountId) + ICassandraPersister.ID_DELIMITER + FRB_SYSTEM_FLAG +
                        ICassandraPersister.ID_DELIMITER + String.valueOf(gameId) +
                        ICassandraPersister.ID_DELIMITER + String.valueOf(bonusId);
            }
        }
    }

    @Override
    public LasthandInfo getObject(String id) {
        if (StringUtils.isTrimmedEmpty(id)) {
            return null;
        }
        String[] parts = id.split(Pattern.quote(ICassandraPersister.ID_DELIMITER));
        int length = parts.length;
        if (length < 2) {
            return null;
        }
        long accountId;
        long gameId;
        Long bonusId;
        BonusSystemType bonusSystemType;
        accountId = Long.parseLong(parts[0]);
        if (length == 2) {
            gameId = Long.parseLong(parts[1]);
            bonusId = null;
            bonusSystemType = null;
        } else if (length == 3) {
            gameId = Long.parseLong(parts[1]);
            bonusId = Long.valueOf(parts[2]);
            bonusSystemType = BonusSystemType.ORDINARY_SYSTEM;
        } else {
            gameId = Long.parseLong(parts[2]);
            bonusId = Long.valueOf(parts[3]);
            bonusSystemType = BonusSystemType.FRB_SYSTEM;
        }
        String lastHandString = get(accountId, gameId, bonusId, bonusSystemType);
        return lastHandString == null ? null : new LasthandInfo(gameId, lastHandString);
    }


    @Override
    public Map<String, LasthandInfo> getAllObjects() {
        return Collections.emptyMap();
    }

    @Override
    public String getAdditionalInfo() {
        return "key: <accountId>+<gameId> OR <accountId>+<gameId>+<bonusId> OR <accountId>+FRB+<gameId>+<bonusId>";
    }

    @Override
    public String printDebug() {
        return "";
    }
}
