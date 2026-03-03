package com.abs.casino.cassandra.persist;

import com.abs.casino.cassandra.persist.engine.AbstractCassandraPersister;
import com.abs.casino.cassandra.persist.engine.ColumnDefinition;
import com.abs.casino.cassandra.persist.engine.ICassandraPersister;
import com.abs.casino.cassandra.persist.engine.TableDefinition;
import com.abs.casino.common.cache.IDistributedCache;
import com.abs.casino.common.cache.data.bonus.FRBonus;
import com.abs.casino.common.util.CalendarUtils;
import com.abs.casino.common.web.statistics.StatisticsManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.ByteBuffer;
import java.util.*;

import static com.abs.casino.cassandra.persist.engine.CassandraDataTypes.bigint;
import static com.abs.casino.cassandra.persist.engine.CassandraDataTypes.blob;
import static com.abs.casino.cassandra.persist.engine.CassandraDataTypes.cint;
import static com.abs.casino.cassandra.persist.engine.CassandraDataTypes.text;
import static com.google.common.base.Preconditions.checkState;

/**
 * User: flsh
 * Date: 16.10.13
 */
public class CassandraFrBonusArchivePersister extends AbstractCassandraPersister<Long, String>
        implements IDistributedCache<Long, FRBonus> {
    public static final String FRBONUS_ARCH_CF = "FrBonusArchCF";
    public static final String BONUS_ID_FIELD = "FrBonusId";
    //extId = bankId+extBonusId
    public static final String EXTERNAL_ID_FIELD = "ExtFrBonusId";
    public static final String ACCOUNT_ID_FIELD = "AccId";
    public static final String STATUS_FIELD = "StatusId";
    //EXPIRATION_DATE_FIELD time (long) for this is begin day 00:00:00.000
    public static final String EXPIRATION_DATE_FIELD = "ExpDate";
    public static final String PERSIST_DAY = "PersistDay";
    public static final String AWARD_TIME_FIELD = "AwardTime";
    private static final Logger LOG = LogManager.getLogger(CassandraFrBonusArchivePersister.class);

    //BONUS_ARCHIVE_TABLE used for BonusHistory reports, all exclude ACTIVE
    //Primary key (accountId), timeAwarded clustered order by timeAwarded desc
    private static final TableDefinition BONUS_ARCHIVE_TABLE = new TableDefinition(FRBONUS_ARCH_CF,
            Arrays.asList(
                    new ColumnDefinition(ACCOUNT_ID_FIELD, bigint(), false, false, true),
                    new ColumnDefinition(AWARD_TIME_FIELD, bigint(), false, false, true),
                    new ColumnDefinition(STATUS_FIELD, cint(), false, false, false),
                    new ColumnDefinition(BONUS_ID_FIELD, bigint(), false, true, false),
                    new ColumnDefinition(EXTERNAL_ID_FIELD, text(), false, true, false),
                    new ColumnDefinition(PERSIST_DAY, bigint(), false, true, false),
                    new ColumnDefinition(SERIALIZED_COLUMN_NAME, blob()),
                    new ColumnDefinition(JSON_COLUMN_NAME, text())
            ), ACCOUNT_ID_FIELD)
            .clusteringOrder(AWARD_TIME_FIELD, com.datastax.driver.core.schemabuilder.SchemaBuilder.Direction.DESC);

    private CassandraFrBonusArchivePersister() {
        super();
    }

    public static String composeKey(long bankId, String externalId) {
        return bankId + ICassandraPersister.ID_DELIMITER + externalId;
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
    public void persist(FRBonus bonus) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("persist bonus: " + bonus);
        }
        String externalKey = composeKey(bonus.getBankId(), bonus.getExtId());
        ByteBuffer byteBuffer = BONUS_ARCHIVE_TABLE.serializeToBytes(bonus);
        String json = BONUS_ARCHIVE_TABLE.serializeToJson(bonus);
        try {
            com.datastax.driver.core.Statement archInsert = com.datastax.driver.core.querybuilder.QueryBuilder.insertInto(FRBONUS_ARCH_CF)
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

    public List<FRBonus> getFinishedFRBonusList(Long accountId) {
        com.datastax.driver.core.Statement select = com.datastax.driver.core.querybuilder.QueryBuilder.select(BONUS_ID_FIELD, SERIALIZED_COLUMN_NAME)
                .from(FRBONUS_ARCH_CF)
                .where(eq(ACCOUNT_ID_FIELD, accountId));
        com.datastax.driver.core.ResultSet rows = execute(select, "getFinishedFRBonusList");
        List<FRBonus> result = new ArrayList<>();
        for (com.datastax.driver.core.Row row : rows) {
            String json = row.getString(JSON_COLUMN_NAME);
            FRBonus bonus = BONUS_ARCHIVE_TABLE.deserializeFromJson(json, FRBonus.class);

            if (bonus == null) {
                ByteBuffer bytes = row.getBytes(SERIALIZED_COLUMN_NAME);
                bonus = BONUS_ARCHIVE_TABLE.deserializeFrom(bytes, FRBonus.class);
            }
            if (bonus != null) {
                result.add(bonus);
            }
        }
        return result;
    }

    @Override
    public String getMainColumnFamilyName() {
        return FRBONUS_ARCH_CF;
    }

    public List<FRBonus> getRecordsByDay(long date) {
        com.datastax.driver.core.Statement query = getSelectColumnsQuery(SERIALIZED_COLUMN_NAME, JSON_COLUMN_NAME)
                .where(eq(PERSIST_DAY, date));
        com.datastax.driver.core.ResultSet resultSet = execute(query, "getRecordsByDay");
        return getFRBonusesFromResultSet(resultSet);
    }

    @SuppressWarnings("Duplicates")
    public void setPersistDay() {
        com.datastax.driver.core.Statement select = com.datastax.driver.core.querybuilder.QueryBuilder.select()
                .column(ACCOUNT_ID_FIELD)
                .column(AWARD_TIME_FIELD)
                .column(BONUS_ID_FIELD)
                .column(PERSIST_DAY)
                .writeTime(BONUS_ID_FIELD)
                .from(FRBONUS_ARCH_CF);
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
                com.datastax.driver.core.Statement update = com.datastax.driver.core.querybuilder.QueryBuilder.update(FRBONUS_ARCH_CF)
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

    public FRBonus get(long id) {
        return get(id, FRBonus.class);
    }

    public List<FRBonus> getByExtId(String key) {
        long now = System.currentTimeMillis();
        com.datastax.driver.core.Statement query = getSelectColumnsQuery(SERIALIZED_COLUMN_NAME, JSON_COLUMN_NAME)
                .where(eq(EXTERNAL_ID_FIELD, key));
        com.datastax.driver.core.ResultSet resultSet = execute(query, "getByExtId");
        List<FRBonus> result = getFRBonusesFromResultSet(resultSet);
        StatisticsManager.getInstance().updateRequestStatistics(getClass().getSimpleName() + " getByExtId",
                System.currentTimeMillis() - now);
        return result;
    }

    private List<FRBonus> getFRBonusesFromResultSet(com.datastax.driver.core.ResultSet resultSet) {
        List<FRBonus> result = new ArrayList<>();
        for (com.datastax.driver.core.Row row : resultSet) {
            String json = row.getString(JSON_COLUMN_NAME);
            FRBonus bonus = BONUS_ARCHIVE_TABLE.deserializeFromJson(json, FRBonus.class);
            if (bonus == null) {
                ByteBuffer bytes = row.getBytes(SERIALIZED_COLUMN_NAME);
                bonus = BONUS_ARCHIVE_TABLE.deserializeFrom(bytes, FRBonus.class);
            }
            if (bonus != null) {
                result.add(bonus);
            }
        }
        return result;
    }

    public void delete(long id) {
        deleteItem(id);
    }

    @Override
    public FRBonus getObject(String id) {
        return get(Long.valueOf(id));
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
