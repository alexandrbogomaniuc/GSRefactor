package com.abs.casino.cassandra.persist;

import com.abs.casino.cassandra.persist.engine.Cql;

import com.abs.casino.cassandra.persist.engine.AbstractCassandraPersister;
import com.abs.casino.cassandra.persist.engine.ColumnDefinition;
import com.abs.casino.cassandra.persist.engine.ICassandraPersister;
import com.abs.casino.cassandra.persist.engine.TableDefinition;
import com.abs.casino.common.cache.CacheKeyInfo;
import com.abs.casino.common.cache.IDistributedCache;
import com.abs.casino.common.cache.data.bonus.BonusStatus;
import com.abs.casino.common.cache.data.bonus.FRBonus;
import com.abs.casino.common.util.CalendarUtils;
import com.abs.casino.common.web.statistics.StatisticsManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.function.Supplier;

import static com.abs.casino.cassandra.persist.engine.CassandraDataTypes.bigint;
import static com.abs.casino.cassandra.persist.engine.CassandraDataTypes.blob;
import static com.abs.casino.cassandra.persist.engine.CassandraDataTypes.text;
import com.datastax.driver.core.querybuilder.Batch;
import com.datastax.driver.core.querybuilder.Delete;
import com.datastax.driver.core.querybuilder.Insert;
import com.datastax.driver.core.querybuilder.Select;


/**
 * User: flsh
 * Date: 16.10.13
 */
@CacheKeyInfo(description = "frBonus.id")
public class CassandraFrBonusPersister extends AbstractCassandraPersister<Long, String>
        implements IDistributedCache<Long, FRBonus> {
    //main CF, key is bonusId
    public static final String FRBONUS_CF = "FrBonusCF";
    public static final String FRBONUS_ACC_INDX = "FrBonusCF_ACC";
    public static final String BONUS_ID_FIELD = "FrBonusId";
    //extId = bankId+extBonusId
    public static final String EXTERNAL_ID_FIELD = "ExtFrBonusId";
    public static final String ACCOUNT_ID_FIELD = "AccId";
    //EXPIRATION_DATE_FIELD time (long) for this is begin day 00:00:00.000
    public static final String EXPIRATION_DATE_FIELD = "ExpDate";
    private static final Logger LOG = LogManager.getLogger(CassandraFrBonusPersister.class);

    private CassandraFrBonusArchivePersister frBonusArchivePersister;

    //table for active bonuses
    private static final TableDefinition TABLE = new TableDefinition(FRBONUS_CF,
            Arrays.asList(
                    new ColumnDefinition(BONUS_ID_FIELD, bigint(), false, false, true),
                    new ColumnDefinition(EXTERNAL_ID_FIELD, text(), false, true, false),
                    new ColumnDefinition(EXPIRATION_DATE_FIELD, bigint(), false, true, false),
                    new ColumnDefinition(SERIALIZED_COLUMN_NAME, blob()),
                    new ColumnDefinition(JSON_COLUMN_NAME, text())
            ), BONUS_ID_FIELD);

    private static final TableDefinition ACCOUNT_INDEX_TABLE = new TableDefinition(FRBONUS_ACC_INDX,
            Arrays.asList(
                    new ColumnDefinition(ACCOUNT_ID_FIELD, bigint(), false, false, true),
                    new ColumnDefinition(BONUS_ID_FIELD, bigint(), false, false, true)
            ), ACCOUNT_ID_FIELD);

    private CassandraFrBonusPersister() {
        super();
    }

    @SuppressWarnings("unused")
    private void setFrBonusArchivePersister(CassandraFrBonusArchivePersister frBonusArchivePersister) {
        this.frBonusArchivePersister = frBonusArchivePersister;
    }

    public static String composeKey(long bankId, String externalId) {
        return bankId + ICassandraPersister.ID_DELIMITER + externalId;
    }

    @Override
    public TableDefinition getMainTableDefinition() {
        return TABLE;
    }

    @Override
    public List<TableDefinition> getAllTableDefinitions() {
        return Arrays.asList(TABLE, ACCOUNT_INDEX_TABLE);
    }

    @Override
    public Logger getLog() {
        return LOG;
    }

    private Long getExpirationDay(FRBonus bonus) {
        return bonus.getExpirationDate() == null ? null :
                CalendarUtils.getStartDay(bonus.getExpirationDate()).getTimeInMillis();
    }

    @Override
    protected String getKeyColumnName() {
        return BONUS_ID_FIELD;
    }

    public void persist(FRBonus bonus) {
        persist(bonus, null);
    }

    /**
     * Save bonus in db
     * @param bonus will be saved
     * @param externalBonusIdComposer provides external id which will be used to get this bonus.
     *                                If not present will be used {@link #composeKey(long, String) Default}
     */
    public void persist(FRBonus bonus, @Nullable Supplier<String> externalBonusIdComposer) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("persist bonus: " + bonus);
        }
        Batch batch = Cql.batch();
        Insert query = getInsertQuery();
        String externalKey = externalBonusIdComposer == null
                ? composeKey(bonus.getBankId(), bonus.getExtId())
                : externalBonusIdComposer.get();
        ByteBuffer byteBuffer = TABLE.serializeToBytes(bonus);
        String json = TABLE.serializeToJson(bonus);
        try {
            if (bonus.getStatus() == BonusStatus.ACTIVE) {
                query.value(BONUS_ID_FIELD, bonus.getId()).
                        value(EXTERNAL_ID_FIELD, externalKey).
                        value(EXPIRATION_DATE_FIELD, getExpirationDay(bonus)).
                        value(SERIALIZED_COLUMN_NAME, byteBuffer).
                        value(JSON_COLUMN_NAME, json);
                batch.add(query);
                Insert indexQuery = Cql.insertInto(FRBONUS_ACC_INDX);
                indexQuery.value(ACCOUNT_ID_FIELD, bonus.getAccountId()).value(BONUS_ID_FIELD, bonus.getId());
                batch.add(indexQuery);
            } else {
                frBonusArchivePersister.persist(bonus);
                Delete indexQuery = Cql.delete().from(FRBONUS_ACC_INDX);
                indexQuery.where(eq(ACCOUNT_ID_FIELD, bonus.getAccountId())).and(eq(BONUS_ID_FIELD, bonus.getId()));
                batch.add(indexQuery);
                Delete activeTable = Cql.delete().from(FRBONUS_CF);
                activeTable.where(eq(BONUS_ID_FIELD, bonus.getId()));
                batch.add(activeTable);
            }
            execute(batch, "persist");
        } finally {
            releaseBuffer(byteBuffer);
        }
    }

    public List<FRBonus> getBonuses(List<Long> bonusIds) {
        if (bonusIds == null || bonusIds.isEmpty()) {
            return Collections.emptyList();
        }
        Select select = Cql.select(SERIALIZED_COLUMN_NAME, JSON_COLUMN_NAME).from(FRBONUS_CF);
        select.where().and(Cql.in(BONUS_ID_FIELD, bonusIds.toArray()));
        com.abs.casino.cassandra.persist.engine.ResultSet resultSet = executeWrapped(select, "getFRBonuses");
        List<FRBonus> result = new ArrayList<>(bonusIds.size());
        for (com.abs.casino.cassandra.persist.engine.Row row : resultSet) {
            String json = row.getString(JSON_COLUMN_NAME);
            FRBonus bonus = TABLE.deserializeFromJson(json, FRBonus.class);

            if (bonus == null) {
                ByteBuffer bytes = row.getBytes(SERIALIZED_COLUMN_NAME);
                bonus = TABLE.deserializeFrom(bytes, FRBonus.class);
            }
            if (bonus != null) {
                result.add(bonus);
            }
        }
        return result;
    }

    public List<FRBonus> getFinishedFRBonusList(Long accountId) {
        return frBonusArchivePersister.getFinishedFRBonusList(accountId);
    }

    public List<FRBonus> getActiveBonuses(Long accountId) {
        long now = System.currentTimeMillis();
        Select query = Cql.select(BONUS_ID_FIELD).from(FRBONUS_ACC_INDX);
        query.where(eq(ACCOUNT_ID_FIELD, accountId));
        com.abs.casino.cassandra.persist.engine.ResultSet rows = executeWrapped(query, "getActiveBonuses");
        List<Long> bonusIds = new ArrayList<>();
        for (com.abs.casino.cassandra.persist.engine.Row row : rows) {
            long bonusId = row.getLong(BONUS_ID_FIELD);
            if (bonusId > 0) {
                bonusIds.add(bonusId);
            }
        }
        List<FRBonus> result = getBonuses(bonusIds);
        StatisticsManager.getInstance().updateRequestStatistics(getClass().getSimpleName() + " getActiveBonuses",
                System.currentTimeMillis() - now);
        return result;
    }

    public List<Long> getByExpirationDate(long expirationDate) {
        long now = System.currentTimeMillis();
        Select query = getSelectColumnsQuery(BONUS_ID_FIELD);
        query.where(eq(EXPIRATION_DATE_FIELD, expirationDate));
        com.abs.casino.cassandra.persist.engine.ResultSet resultSet = executeWrapped(query, "getByExpirationDate");
        List<Long> ids = new ArrayList<>();
        for (com.abs.casino.cassandra.persist.engine.Row row : resultSet) {
            ids.add(row.getLong(BONUS_ID_FIELD));
        }
        StatisticsManager.getInstance().updateRequestStatistics(getClass().getSimpleName() + " getByExpirationDate",
                System.currentTimeMillis() - now);
        return ids;
    }

    public FRBonus get(long id) {
        return get(id, FRBonus.class);
    }

    public FRBonus getByExtId(String key) {
        long now = System.currentTimeMillis();
        Select query = getSelectColumnsQuery(SERIALIZED_COLUMN_NAME, JSON_COLUMN_NAME);
        query.where(eq(EXTERNAL_ID_FIELD, key));
        com.abs.casino.cassandra.persist.engine.ResultSet resultSet = executeWrapped(query, "getByExtId", 3);
        com.abs.casino.cassandra.persist.engine.Row row = resultSet.one();
        StatisticsManager.getInstance().updateRequestStatistics(getClass().getSimpleName() + " getByExtId",
                System.currentTimeMillis() - now);
        if (row == null) {
            return null;
        }

        String json = row.getString(JSON_COLUMN_NAME);
        FRBonus bonus = TABLE.deserializeFromJson(json, FRBonus.class);

        if (bonus == null) {
            ByteBuffer bytes = row.getBytes(SERIALIZED_COLUMN_NAME);
            bonus = TABLE.deserializeFrom(bytes, FRBonus.class);
        }
        return bonus;
    }

    public void delete(long id) {
        deleteItem(id);
    }

    @Override
    public FRBonus getObject(String id) {
        return get(Long.parseLong(id));
    }

    @Override
    public Map<Long, FRBonus> getAllObjects() {
        //too large, may be implement later
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
}
