package com.betsoft.casino.mp.data.persister;

import com.betsoft.casino.mp.model.Money;
import com.betsoft.casino.mp.service.IWeaponService;
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

public class WeaponsPersister extends AbstractCassandraPersister<Long, String> implements IWeaponService {
    private static final Logger LOG = LogManager.getLogger(WeaponsPersister.class);

    private static final String CF_NAME = "Weapons";
    private static final String BANK_ID_COLUMN = "bid";
    private static final String ACCOUNT_ID_COLUMN = "aid";
    private static final String MODE_COLUMN = "m";
    private static final String STAKE_COLUMN = "s";
    private static final String WEAPONS_COLUMN = "w";
    private static final String GAMEID_COLUMN = "g";


    private static final String SPECIAL_MODE_CF_NAME = "SMWeapons";
    //field for tournament or bonusId
    private static final String SM_ID_COLUMN = "smid";

    private static final TableDefinition TABLE = new TableDefinition(CF_NAME,
            Arrays.asList(
                    new ColumnDefinition(BANK_ID_COLUMN, DataType.bigint(), false, false, true),
                    new ColumnDefinition(ACCOUNT_ID_COLUMN, DataType.bigint(), false, false, true),
                    new ColumnDefinition(MODE_COLUMN, DataType.cint(), false, false, true),
                    new ColumnDefinition(STAKE_COLUMN, DataType.bigint(), false, false, true),
                    new ColumnDefinition(GAMEID_COLUMN, DataType.bigint(), false, false, true),
                    new ColumnDefinition(WEAPONS_COLUMN, DataType.blob()),
                    new ColumnDefinition(JSON_COLUMN_NAME, DataType.text())
            ), BANK_ID_COLUMN, ACCOUNT_ID_COLUMN, MODE_COLUMN, GAMEID_COLUMN);

    private static final TableDefinition SPECIAL_MODE_TABLE = new TableDefinition(SPECIAL_MODE_CF_NAME,
            Arrays.asList(
                    new ColumnDefinition(SM_ID_COLUMN, DataType.bigint(), false, false, true),
                    new ColumnDefinition(ACCOUNT_ID_COLUMN, DataType.bigint(), false, false, true),
                    new ColumnDefinition(MODE_COLUMN, DataType.cint(), false, false, true),
                    new ColumnDefinition(STAKE_COLUMN, DataType.bigint(), false, false, true),
                    new ColumnDefinition(GAMEID_COLUMN, DataType.bigint(), false, false, true),
                    new ColumnDefinition(WEAPONS_COLUMN, DataType.blob()),
                    new ColumnDefinition(JSON_COLUMN_NAME, DataType.text())
            ), SM_ID_COLUMN, ACCOUNT_ID_COLUMN, MODE_COLUMN, GAMEID_COLUMN);

    public void saveWeapons(long bankId, long accountId, int mode, Money stake, Map<Integer, Integer> weapons, long gameId) {
        ByteBuffer byteBuffer = TABLE.serializeWithClassToBytes(weapons);
        String json = TABLE.serializeToMapJson(weapons, Integer.class, Integer.class);
        try {
            Update.Assignments update = getUpdateQuery()
                    .where(eq(BANK_ID_COLUMN, bankId))
                    .and(eq(ACCOUNT_ID_COLUMN, accountId))
                    .and(eq(GAMEID_COLUMN, gameId))
                    .and(eq(MODE_COLUMN, mode))
                    .and(eq(STAKE_COLUMN, stake.getValue()))
                    .with(set(WEAPONS_COLUMN, byteBuffer))
                    .and(set(JSON_COLUMN_NAME, json));
            execute(update, "saveWeapons");
        } finally {
            releaseBuffer(byteBuffer);
        }
    }

    @Override
    public void saveSpecialModeWeapons(long tournamentOrBonusId, long accountId, int mode, Money stake,
                                       Map<Integer, Integer> weapons, long gameId) {
        ByteBuffer byteBuffer = SPECIAL_MODE_TABLE.serializeWithClassToBytes(weapons);
        String json = SPECIAL_MODE_TABLE.serializeToMapJson(weapons, Integer.class, Integer.class);
        try {
            ;
            Update.Assignments update = QueryBuilder.update(SPECIAL_MODE_TABLE.getTableName())
                    .where(eq(SM_ID_COLUMN, tournamentOrBonusId))
                    .and(eq(ACCOUNT_ID_COLUMN, accountId))
                    .and(eq(GAMEID_COLUMN, gameId))
                    .and(eq(MODE_COLUMN, mode))
                    .and(eq(STAKE_COLUMN, stake.getValue()))
                    .with(set(WEAPONS_COLUMN, byteBuffer))
                    .and(set(JSON_COLUMN_NAME, json));
            execute(update, "saveSpecialModeWeapons");
        } finally {
            releaseBuffer(byteBuffer);
        }
    }

    public Map<Integer, Integer> loadWeapons(long bankId, long accountId, int mode, Money stake, long gameId) {
        Select query = getSelectColumnsQuery(TABLE, WEAPONS_COLUMN, JSON_COLUMN_NAME)
                .where(eq(BANK_ID_COLUMN, bankId))
                .and(eq(ACCOUNT_ID_COLUMN, accountId))
                .and(eq(GAMEID_COLUMN, gameId))
                .and(eq(MODE_COLUMN, mode))
                .and(eq(STAKE_COLUMN, stake.getValue()))
                .limit(1);
        Row result = execute(query, "loadWeapons").one();
        if (result != null) {
            String json = result.getString(JSON_COLUMN_NAME);
            Map<Integer, Integer> weapons = null;
            if (json != null) {
                weapons = TABLE.deserializeToMapJson(json, Integer.class, Integer.class);
            }
            if (weapons == null) {
                weapons = TABLE.deserializeWithClassFrom(result.getBytes(WEAPONS_COLUMN));
            }
            return weapons;
        }
        return null;
    }

    @Override
    public Map<Integer, Integer> loadSpecialModeWeapons(long tournamentOrBonusId, long accountId, int mode,
                                                        Money stake, long gameId) {
        Select query = getSelectColumnsQuery(SPECIAL_MODE_TABLE, WEAPONS_COLUMN, JSON_COLUMN_NAME)
                .where(eq(SM_ID_COLUMN, tournamentOrBonusId))
                .and(eq(ACCOUNT_ID_COLUMN, accountId))
                .and(eq(GAMEID_COLUMN, gameId))
                .and(eq(MODE_COLUMN, mode))
                .and(eq(STAKE_COLUMN, stake.getValue()))
                .limit(1);
        Row result = execute(query, "loadSpecialModeWeapons").one();
        if (result != null) {
            Map<Integer, Integer> weapons = SPECIAL_MODE_TABLE.deserializeToMapJson(result.getString(JSON_COLUMN_NAME), Integer.class, Integer.class);
            if (weapons == null) {
                weapons = SPECIAL_MODE_TABLE.deserializeWithClassFrom(result.getBytes(WEAPONS_COLUMN));
            }
            return weapons;
        }
        return null;
    }

    public Map<Money, Map<Integer, Integer>> getAllWeapons(long bankId, long accountId, int mode, long gameId) {
        Select.Where query = getSelectColumnsQuery(TABLE, STAKE_COLUMN, WEAPONS_COLUMN, JSON_COLUMN_NAME)
                .where(eq(BANK_ID_COLUMN, bankId))
                .and(eq(ACCOUNT_ID_COLUMN, accountId))
                .and(eq(GAMEID_COLUMN, gameId))
                .and(eq(MODE_COLUMN, mode));
        Map<Money, Map<Integer, Integer>> weapons = new HashMap<>();
        ResultSet result = execute(query, "getAllWeapons");
        if (result != null) {
            result.forEach(row -> {
                Map<Integer, Integer> weaponsMap = SPECIAL_MODE_TABLE.deserializeToMapJson(row.getString(JSON_COLUMN_NAME), Integer.class, Integer.class);
                if (weaponsMap == null) {
                    weaponsMap = SPECIAL_MODE_TABLE.deserializeWithClassFrom(row.getBytes(WEAPONS_COLUMN));
                }
                weapons.put(new Money(row.getLong(STAKE_COLUMN)), weaponsMap);
            });
        }
        return weapons;
    }

    @Override
    public Map<Money, Map<Integer, Integer>> getAllSpecialModeWeapons(long tournamentOrBonusId, long accountId,
                                                                      int mode, long gameId) {
        Select.Where query = getSelectColumnsQuery(SPECIAL_MODE_TABLE, STAKE_COLUMN, WEAPONS_COLUMN, JSON_COLUMN_NAME)
                .where(eq(SM_ID_COLUMN, tournamentOrBonusId))
                .and(eq(ACCOUNT_ID_COLUMN, accountId))
                .and(eq(GAMEID_COLUMN, gameId))
                .and(eq(MODE_COLUMN, mode));
        Map<Money, Map<Integer, Integer>> weapons = new HashMap<>();
        ResultSet result = execute(query, "getAllSpecialModeWeapons");
        if (result != null) {
            result.forEach(row -> {
                Map<Integer, Integer> weaponsMap = SPECIAL_MODE_TABLE.deserializeToMapJson(row.getString(JSON_COLUMN_NAME), Integer.class, Integer.class);
                if (weaponsMap == null) {
                    weaponsMap = SPECIAL_MODE_TABLE.deserializeWithClassFrom(row.getBytes(WEAPONS_COLUMN));
                }
                weapons.put(new Money(row.getLong(STAKE_COLUMN)), weaponsMap);
            });
        }
        return weapons;
    }

    public Map<Long, Map<Integer, Integer>> getAllWeaponsLong(long bankId, long accountId, int mode, long gameId) {
        Select.Where query = getSelectColumnsQuery(TABLE, STAKE_COLUMN, WEAPONS_COLUMN, JSON_COLUMN_NAME)
                .where(eq(BANK_ID_COLUMN, bankId))
                .and(eq(ACCOUNT_ID_COLUMN, accountId))
                .and(eq(GAMEID_COLUMN, gameId))
                .and(eq(MODE_COLUMN, mode));
        Map<Long, Map<Integer, Integer>> weapons = new HashMap<>();
        ResultSet result = execute(query, "getAllWeaponsLong");
        if (result != null) {
            result.forEach(row -> {
                String string = row.getString(JSON_COLUMN_NAME);
                Map<Integer, Integer> weaponsMap = new HashMap<Integer, Integer>();
                if (string != null) {
                    weaponsMap = SPECIAL_MODE_TABLE.deserializeToMapJson(string, Integer.class, Integer.class);
                }
                if (weaponsMap == null) {
                    weaponsMap = SPECIAL_MODE_TABLE.deserializeWithClassFrom(row.getBytes(WEAPONS_COLUMN));
                }
                weapons.put(new Money(row.getLong(STAKE_COLUMN)).toCents(), weaponsMap);
            });
        }
        return weapons;
    }

    @Override
    public Map<Long, Map<Integer, Integer>> getSpecialModeAllWeaponsLong(long tournamentOrBonusId, long accountId,
                                                                         int mode, long gameId) {
        Select.Where query = getSelectColumnsQuery(SPECIAL_MODE_TABLE, STAKE_COLUMN, WEAPONS_COLUMN, JSON_COLUMN_NAME)
                .where(eq(SM_ID_COLUMN, tournamentOrBonusId))
                .and(eq(ACCOUNT_ID_COLUMN, accountId))
                .and(eq(GAMEID_COLUMN, gameId))
                .and(eq(MODE_COLUMN, mode));
        Map<Long, Map<Integer, Integer>> weapons = new HashMap<>();
        ResultSet result = execute(query, "getSpecialModeAllWeaponsLong");
        if (result != null) {
            result.forEach(row -> {
                Map<Integer, Integer> weaponsMap = SPECIAL_MODE_TABLE.deserializeToMapJson(row.getString(JSON_COLUMN_NAME), Integer.class, Integer.class);
                if (weaponsMap == null) {
                    weaponsMap = SPECIAL_MODE_TABLE.deserializeWithClassFrom(row.getBytes(WEAPONS_COLUMN));
                }
                weapons.put(new Money(row.getLong(STAKE_COLUMN)).toCents(), weaponsMap);
            });
        }
        return weapons;
    }

    @Override
    public void removeAllWeapons() {
        Truncate truncate = QueryBuilder.truncate(CF_NAME);
        execute(truncate, "removeAllWeapons");
        getLog().debug("remove all weapons");
    }

    public void removeSpecialModeAllWeapons() {
        Truncate truncate = QueryBuilder.truncate(SPECIAL_MODE_CF_NAME);
        execute(truncate, "removeSpecialModeAllWeapons");
        getLog().debug("removeSpecialModeAllWeapons");
    }

    @Override
    public TableDefinition getMainTableDefinition() {
        return TABLE;
    }

    @Override
    public List<TableDefinition> getAllTableDefinitions() {
        return Collections.unmodifiableList(Arrays.asList(TABLE, SPECIAL_MODE_TABLE));
    }

    @Override
    public Logger getLog() {
        return LOG;
    }
}
