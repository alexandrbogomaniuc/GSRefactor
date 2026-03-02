package com.abs.casino.cassandra.persist;

import com.abs.casino.cassandra.persist.engine.AbstractCassandraPersister;
import com.abs.casino.cassandra.persist.engine.ColumnDefinition;
import com.abs.casino.cassandra.persist.engine.ICassandraPersister;
import com.abs.casino.cassandra.persist.engine.TableDefinition;
import com.abs.casino.common.cache.CacheKeyInfo;
import com.abs.casino.common.cache.IDistributedCache;
import com.abs.casino.common.cache.data.bonus.Bonus;
import com.abs.casino.common.cache.data.bonus.BonusStatus;
import com.abs.casino.common.exception.CommonException;
import com.abs.casino.common.util.CalendarUtils;
import com.abs.casino.common.web.statistics.StatisticsManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.ByteBuffer;
import java.util.*;

/**
 * User: flsh
 * Date: 16.10.13
 */
@CacheKeyInfo(description = "bonus.id")
public class CassandraBonusPersister extends AbstractCassandraPersister<Long, String>
        implements IDistributedCache<Long, Bonus> {
    private static final Logger LOG = LogManager.getLogger(CassandraBonusPersister.class);

    //main CF, key is bonusId
    public static final String BONUS_CF = "BonusCF";
    public static final String BONUS_ACC_INDX = "BonusCF_ACC";

    public static final String BONUS_ID_FIELD = "BonusId";
    //extId = bankId+extBonusId
    public static final String EXTERNAL_ID_FIELD = "ExtBonusId";
    public static final String ACCOUNT_ID_FIELD = "AccId";
    //EXPIRATION_DATE_FIELD time (long) for this is begin day 00:00:00.000
    public static final String EXPIRATION_DATE_FIELD = "ExpDate";

    private CassandraBonusArchivePersister bonusArchivePersister;

    private static final TableDefinition TABLE = new TableDefinition(BONUS_CF,
            Arrays.asList(
                    new ColumnDefinition(BONUS_ID_FIELD, com.datastax.driver.core.DataType.bigint(), false, false, true),
                    new ColumnDefinition(EXTERNAL_ID_FIELD, com.datastax.driver.core.DataType.text(), false, true, false),
                    new ColumnDefinition(EXPIRATION_DATE_FIELD, com.datastax.driver.core.DataType.bigint(), false, true, false),
                    new ColumnDefinition(SERIALIZED_COLUMN_NAME, com.datastax.driver.core.DataType.blob()),
                    new ColumnDefinition(JSON_COLUMN_NAME, com.datastax.driver.core.DataType.text())
            ), BONUS_ID_FIELD);

    private static final TableDefinition ACCOUNT_INDEX_TABLE = new TableDefinition(BONUS_ACC_INDX,
            Arrays.asList(
                    new ColumnDefinition(ACCOUNT_ID_FIELD, com.datastax.driver.core.DataType.bigint(), false, false, true),
                    new ColumnDefinition(BONUS_ID_FIELD, com.datastax.driver.core.DataType.bigint(), false, false, true)
            ), ACCOUNT_ID_FIELD);

    private CassandraBonusPersister() {
        super();
    }

    @SuppressWarnings("unused")
    private void setBonusArchivePersister(CassandraBonusArchivePersister bonusArchivePersister) {
        this.bonusArchivePersister = bonusArchivePersister;
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

    private long getExpirationDay(Bonus bonus) {
        return CalendarUtils.getStartDay(bonus.getExpirationDate()).getTimeInMillis();
    }

    @Override
    protected String getKeyColumnName() {
        return BONUS_ID_FIELD;
    }

    public void persist(Bonus bonus) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("persist bonus: " + bonus);
        }
        com.datastax.driver.core.querybuilder.Batch batch = com.datastax.driver.core.querybuilder.QueryBuilder.batch();
        com.datastax.driver.core.querybuilder.Insert query = getInsertQuery();
        String externalKey = composeKey(bonus.getBankId(), bonus.getExtId());
        String json = TABLE.serializeToJson(bonus);
        ByteBuffer byteBuffer = TABLE.serializeToBytes(bonus);
        try {
            BonusStatus bonusStatus = bonus.getStatus();
            if (bonusStatus == BonusStatus.ACTIVE || bonusStatus == BonusStatus.RELEASING) {
                query.value(BONUS_ID_FIELD, bonus.getId()).
                        value(EXTERNAL_ID_FIELD, externalKey).
                        value(EXPIRATION_DATE_FIELD, getExpirationDay(bonus)).
                        value(SERIALIZED_COLUMN_NAME, byteBuffer).
                        value(JSON_COLUMN_NAME, json);
                batch.add(query);
                com.datastax.driver.core.querybuilder.Insert indexQuery = com.datastax.driver.core.querybuilder.QueryBuilder.insertInto(BONUS_ACC_INDX);
                indexQuery.value(ACCOUNT_ID_FIELD, bonus.getAccountId()).value(BONUS_ID_FIELD, bonus.getId());
                batch.add(indexQuery);
            } else {
                bonusArchivePersister.persist(bonus);
                com.datastax.driver.core.querybuilder.Delete activeTable = com.datastax.driver.core.querybuilder.QueryBuilder.delete().from(BONUS_CF);
                activeTable.where(eq(BONUS_ID_FIELD, bonus.getId()));
                batch.add(activeTable);
            }
            execute(batch, "persist");
        } finally {
            releaseBuffer(byteBuffer);
        }
    }

    public List<Bonus> getBonuses(List<Long> bonusIds) {
        if (bonusIds == null || bonusIds.isEmpty()) {
            return Collections.emptyList();
        }
        com.datastax.driver.core.querybuilder.Select select = com.datastax.driver.core.querybuilder.QueryBuilder.select(SERIALIZED_COLUMN_NAME, JSON_COLUMN_NAME).from(BONUS_CF);
        select.where().and(com.datastax.driver.core.querybuilder.QueryBuilder.in(BONUS_ID_FIELD, bonusIds.toArray()));
        com.datastax.driver.core.ResultSet resultSet = execute(select, "getBonuses");
        Map<Long, Bonus> resultsMap = new HashMap<>(bonusIds.size());
        for (com.datastax.driver.core.Row row : resultSet) {
            String json = row.getString(JSON_COLUMN_NAME);
            Bonus bonus = TABLE.deserializeFromJson(json, Bonus.class);

            if (bonus == null) {
                ByteBuffer bytes = row.getBytes(SERIALIZED_COLUMN_NAME);
                bonus = TABLE.deserializeFrom(bytes, Bonus.class);
            }

            if (bonus != null) {
                resultsMap.put(bonus.getId(), bonus);
            }
        }
        List<Bonus> result = new ArrayList<>(bonusIds.size());
        for (Long bonusId : bonusIds) {
            Bonus bonus = resultsMap.get(bonusId);
            if (bonus != null) {
                result.add(bonus);
            } else {
                getLog().warn("Cannot find bonus, id=" + bonusId);
            }
        }
        return result;
    }

    public List<Bonus> getFinishedBonusList(Long accountId)
            throws CommonException {
        return bonusArchivePersister.getFinishedBonusList(accountId);
    }

    public List<Bonus> getActiveBonuses(Long accountId) {
        long now = System.currentTimeMillis();
        com.datastax.driver.core.querybuilder.Select query = com.datastax.driver.core.querybuilder.QueryBuilder.select(BONUS_ID_FIELD).from(BONUS_ACC_INDX);
        query.where(eq(ACCOUNT_ID_FIELD, accountId));
        com.datastax.driver.core.ResultSet rows = execute(query, "getActiveBonuses");
        List<Long> bonusIds = new ArrayList<>();
        for (com.datastax.driver.core.Row row : rows) {
            long bonusId = row.getLong(BONUS_ID_FIELD);
            if (bonusId > 0) {
                bonusIds.add(bonusId);
            }
        }
        List<Bonus> result = getBonuses(bonusIds);
        StatisticsManager.getInstance().updateRequestStatistics(getClass().getSimpleName() + " getActiveBonuses",
                System.currentTimeMillis() - now);
        return result;
    }

    public List<Long> getByExpirationDate(long expirationDate) {
        long now = System.currentTimeMillis();
        com.datastax.driver.core.querybuilder.Select query = getSelectColumnsQuery(BONUS_ID_FIELD);
        query.where(eq(EXPIRATION_DATE_FIELD, expirationDate));
        com.datastax.driver.core.ResultSet resultSet = execute(query, "getByExpirationDate");
        List<Long> ids = new ArrayList<>();
        for (com.datastax.driver.core.Row row : resultSet) {
            ids.add(row.getLong(BONUS_ID_FIELD));
        }
        StatisticsManager.getInstance().updateRequestStatistics(getClass().getSimpleName() + " getByExpirationDate",
                System.currentTimeMillis() - now);
        return ids;
    }

    public Bonus get(long id) {
        String json = getJson(id);
        Bonus bonus = TABLE.deserializeFromJson(json, Bonus.class);

        if (bonus == null) {
            ByteBuffer buffer = get(id, SERIALIZED_COLUMN_NAME);
            bonus = TABLE.deserializeFrom(buffer, Bonus.class);
        }

        return bonus;
    }

    public Bonus getByCompositeKey(long bankId, String externalId) {
        long now = System.currentTimeMillis();
        String key = composeKey(bankId, externalId);
        com.datastax.driver.core.querybuilder.Select query = getSelectColumnsQuery(SERIALIZED_COLUMN_NAME, JSON_COLUMN_NAME);
        query.where(eq(EXTERNAL_ID_FIELD, key));
        com.datastax.driver.core.ResultSet resultSet = execute(query, "getByCompositeKey");
        com.datastax.driver.core.Row row = resultSet.one();

        if (row == null) {
            return null;
        }
        String json = row.getString(JSON_COLUMN_NAME);
        Bonus bonus = TABLE.deserializeFromJson(json, Bonus.class);

        if (bonus == null) {
            ByteBuffer bytes = row.getBytes(SERIALIZED_COLUMN_NAME);
            bonus = TABLE.deserializeFrom(bytes, Bonus.class);
        }
        StatisticsManager.getInstance().updateRequestStatistics(getClass().getSimpleName() + " getByCompositeKey",
                System.currentTimeMillis() - now);
        return bonus;
    }

    public void delete(long id) {
        long now = System.currentTimeMillis();
        deleteItem(id);
        StatisticsManager.getInstance().updateRequestStatistics(getClass().getSimpleName() + " delete",
                System.currentTimeMillis() - now);
    }

    public static String composeKey(long bankId, String externalId) {
        return bankId + ICassandraPersister.ID_DELIMITER + externalId;
    }

    @Override
    public Bonus getObject(String id) {
        return get(Long.valueOf(id));
    }

    @Override
    public Map<Long, Bonus> getAllObjects() {
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
