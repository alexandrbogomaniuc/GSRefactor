package com.abs.casino.cassandra.persist;

import com.dgphoenix.casino.cassandra.persist.engine.AbstractCassandraPersister;
import com.dgphoenix.casino.cassandra.persist.engine.ColumnDefinition;
import com.dgphoenix.casino.cassandra.persist.engine.ICassandraPersister;
import com.dgphoenix.casino.cassandra.persist.engine.TableDefinition;
import com.dgphoenix.casino.common.cache.IDistributedCache;
import com.dgphoenix.casino.common.cache.data.bonus.Bonus;
import com.abs.casino.common.util.CalendarUtils;
import com.dgphoenix.casino.common.web.statistics.StatisticsManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.ByteBuffer;
import java.util.*;

import static com.google.common.base.Preconditions.checkState;

/**
 * User: flsh
 * Date: 16.10.13
 */
public class CassandraBonusArchivePersister extends AbstractCassandraPersister<Long, String>
        implements IDistributedCache<Long, Bonus> {
    private static final Logger LOG = LogManager.getLogger(CassandraBonusArchivePersister.class);

    //main CF, key is bonusId
    public static final String BONUS_ARCH_CF = "BonusArchCF";
    public static final String BONUS_ID_FIELD = "BonusId";
    //extId = bankId+extBonusId
    public static final String EXTERNAL_ID_FIELD = "ExtBonusId";
    public static final String ACCOUNT_ID_FIELD = "AccId";
    public static final String STATUS_FIELD = "StatusId";
    //EXPIRATION_DATE_FIELD time (long) for this is begin day 00:00:00.000
    public static final String AWARD_TIME_FIELD = "AwardTime";
    private static final String PERSIST_DAY = "PersistDay";

    //BONUS_ARCHIVE_TABLE used for BonusHistory reports, all exclude ACTIVE
    //Primary key (accountId), timeAwarded clustered order by timeAwarded desc
    private static final TableDefinition BONUS_ARCHIVE_TABLE = new TableDefinition(BONUS_ARCH_CF,
            Arrays.asList(
                    new ColumnDefinition(ACCOUNT_ID_FIELD, com.datastax.driver.core.DataType.bigint(), false, false, true),
                    new ColumnDefinition(AWARD_TIME_FIELD, com.datastax.driver.core.DataType.bigint(), false, false, true),
                    new ColumnDefinition(STATUS_FIELD, com.datastax.driver.core.DataType.cint(), false, false, false),
                    new ColumnDefinition(BONUS_ID_FIELD, com.datastax.driver.core.DataType.bigint(), false, true, false),
                    new ColumnDefinition(EXTERNAL_ID_FIELD, com.datastax.driver.core.DataType.text(), false, true, false),
                    new ColumnDefinition(PERSIST_DAY, com.datastax.driver.core.DataType.bigint(), false, true, false),
                    new ColumnDefinition(SERIALIZED_COLUMN_NAME, com.datastax.driver.core.DataType.blob()),
                    new ColumnDefinition(JSON_COLUMN_NAME, com.datastax.driver.core.DataType.text())
            ), ACCOUNT_ID_FIELD)
            .clusteringOrder(AWARD_TIME_FIELD, com.datastax.driver.core.schemabuilder.SchemaBuilder.Direction.DESC);

    private CassandraBonusArchivePersister() {
        super();
    }

    @Override
    public TableDefinition getMainTableDefinition() {
        return BONUS_ARCHIVE_TABLE;
    }

    @Override
    public Logger getLog() {
        return LOG;
    }

    @Override
    protected String getKeyColumnName() {
        return BONUS_ID_FIELD;
    }

    @SuppressWarnings("Duplicates")
    public void persist(Bonus bonus) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("persist bonus: " + bonus);
        }
        String externalKey = composeKey(bonus.getBankId(), bonus.getExtId());
        ByteBuffer byteBuffer = BONUS_ARCHIVE_TABLE.serializeToBytes(bonus);
        String json = BONUS_ARCHIVE_TABLE.serializeToJson(bonus);
        try {
            com.datastax.driver.core.Statement archInsert = com.datastax.driver.core.querybuilder.QueryBuilder.insertInto(BONUS_ARCH_CF)
                    .value(ACCOUNT_ID_FIELD, bonus.getAccountId()).
                    value(AWARD_TIME_FIELD, bonus.getTimeAwarded()).
                    value(STATUS_FIELD, bonus.getStatus().ordinal()).
                    value(BONUS_ID_FIELD, bonus.getId()).
                    value(EXTERNAL_ID_FIELD, externalKey).
                    value(PERSIST_DAY, CalendarUtils.getStartDay(System.currentTimeMillis()).getTimeInMillis()).
                    value(SERIALIZED_COLUMN_NAME, byteBuffer).
                    value(JSON_COLUMN_NAME, json);
            execute(archInsert, "persist");
        } finally {
            releaseBuffer(byteBuffer);
        }
    }

    public List<Bonus> getFinishedBonusList(Long accountId) {
        com.datastax.driver.core.Statement select = com.datastax.driver.core.querybuilder.QueryBuilder
                .select(BONUS_ID_FIELD, SERIALIZED_COLUMN_NAME, JSON_COLUMN_NAME)
                .from(BONUS_ARCH_CF)
                .where(eq(ACCOUNT_ID_FIELD, accountId));
        com.datastax.driver.core.ResultSet rows = execute(select, "getFinishedBonusList");
        return getBonusesFromResultSet(rows);
    }

    public Bonus get(long id) {
        String json = getJson(id);
        Bonus bonus = BONUS_ARCHIVE_TABLE.deserializeFromJson(json, Bonus.class);

        if (bonus == null) {
            ByteBuffer buffer = get(id, SERIALIZED_COLUMN_NAME);
            bonus = BONUS_ARCHIVE_TABLE.deserializeFrom(buffer, Bonus.class);
        }
        return bonus;
    }

    public List<Bonus> getByCompositeKey(long bankId, String externalId) {
        long now = System.currentTimeMillis();
        String key = composeKey(bankId, externalId);
        com.datastax.driver.core.Statement query = getSelectColumnsQuery(SERIALIZED_COLUMN_NAME, JSON_COLUMN_NAME)
                .where(eq(EXTERNAL_ID_FIELD, key));
        com.datastax.driver.core.ResultSet resultSet = execute(query, "getByCompositeKey");
        List<Bonus> result = getBonusesFromResultSet(resultSet);
        StatisticsManager.getInstance().updateRequestStatistics(getClass().getSimpleName() + " getByCompositeKey",
                System.currentTimeMillis() - now);
        return result;
    }

    public List<Bonus> getRecordsByDay(long date) {
        com.datastax.driver.core.Statement query = getSelectColumnsQuery(SERIALIZED_COLUMN_NAME, JSON_COLUMN_NAME)
                .where(eq(PERSIST_DAY, date));
        com.datastax.driver.core.ResultSet resultSet = execute(query, "getRecordsByDay");
        return getBonusesFromResultSet(resultSet);
    }

    private List<Bonus> getBonusesFromResultSet(com.datastax.driver.core.ResultSet resultSet) {
        List<Bonus> result = new ArrayList<>();
        for (com.datastax.driver.core.Row row : resultSet) {
            String json = row.getString(JSON_COLUMN_NAME);
            Bonus bonus = BONUS_ARCHIVE_TABLE.deserializeFromJson(json, Bonus.class);

            if (bonus == null) {
                ByteBuffer bytes = row.getBytes(SERIALIZED_COLUMN_NAME);
                bonus = BONUS_ARCHIVE_TABLE.deserializeFrom(bytes, Bonus.class);
            }
            if (bonus != null) {
                result.add(bonus);
            }
        }
        return result;
    }

    @SuppressWarnings("Duplicates")
    public void setPersistDay() {
        com.datastax.driver.core.Statement select = com.datastax.driver.core.querybuilder.QueryBuilder.select()
                .column(ACCOUNT_ID_FIELD)
                .column(AWARD_TIME_FIELD)
                .column(BONUS_ID_FIELD)
                .column(PERSIST_DAY)
                .writeTime(BONUS_ID_FIELD)
                .from(BONUS_ARCH_CF);
        com.datastax.driver.core.ResultSet resultSet = execute(select, "setPersistDay");

        long currentPersistTime = CalendarUtils.getStartDay(System.currentTimeMillis()).getTimeInMillis();
        Long awardTime;
        Long accountId;
        Long bonusId;
        long bonusPersistTime;
        for (com.datastax.driver.core.Row row : resultSet) {
            awardTime = row.getLong(AWARD_TIME_FIELD);
            accountId = row.getLong(ACCOUNT_ID_FIELD);
            bonusId = row.getLong(BONUS_ID_FIELD);
            bonusPersistTime = row.getLong(PERSIST_DAY);

            if (bonusPersistTime == 0) {
                com.datastax.driver.core.Statement update = com.datastax.driver.core.querybuilder.QueryBuilder.update(BONUS_ARCH_CF)
                        .with(set(PERSIST_DAY, currentPersistTime))
                        .where(eq(ACCOUNT_ID_FIELD, accountId))
                        .and(eq(AWARD_TIME_FIELD, awardTime));
                execute(update, "setPersistDay");
                checkState(resultSet.wasApplied(), "Cannot set persist day value for bonusId={}", bonusId);
                LOG.info("AwardTime: {}, Persisted date: {}, for bonusId={}, accountId={}",
                        awardTime, new Date(currentPersistTime), bonusId, accountId);
            }
        }
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
