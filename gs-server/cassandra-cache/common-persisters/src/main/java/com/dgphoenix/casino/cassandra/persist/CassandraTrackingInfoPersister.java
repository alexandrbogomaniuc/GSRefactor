package com.dgphoenix.casino.cassandra.persist;

import com.datastax.driver.core.ColumnDefinitions;
import com.datastax.driver.core.DataType;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.dgphoenix.casino.cassandra.persist.engine.AbstractCassandraPersister;
import com.dgphoenix.casino.cassandra.persist.engine.ColumnDefinition;
import com.dgphoenix.casino.cassandra.persist.engine.TableDefinition;
import com.dgphoenix.casino.cassandra.persist.engine.configuration.CompactionStrategy;
import com.dgphoenix.casino.common.engine.tracker.ICommonTrackingTaskDelegate;
import com.dgphoenix.casino.common.engine.tracker.TrackingInfo;
import com.dgphoenix.casino.common.util.string.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * User: flsh
 * Date: 17.10.13
 * key = trackerName+gameServerId, column name is id of tracked object: wallet=accountId, bonus=bonusId;
 * column value is lastTrackingDate
 */
public class CassandraTrackingInfoPersister extends AbstractCassandraPersister<String, String> {
    public static final String TRACKING_INFO_CF = "TrackInfoCF";
    public static final String OBJECT_ID_FIELD = "OBJECT_ID";
    private static final Logger LOG = LogManager.getLogger(CassandraTrackingInfoPersister.class);
    private static final TableDefinition TABLE = new TableDefinition(TRACKING_INFO_CF,
            Arrays.asList(
                    new ColumnDefinition(KEY, DataType.text(), false, false, true), //key is trackerName+gsId
                    new ColumnDefinition(OBJECT_ID_FIELD, DataType.text(), false, false, true),
                    new ColumnDefinition(SERIALIZED_COLUMN_NAME, DataType.blob()),
                    new ColumnDefinition(JSON_COLUMN_NAME, DataType.text())
            ), KEY)
            .compaction(CompactionStrategy.getLeveled(true, TimeUnit.HOURS.toSeconds(1)))
            .gcGraceSeconds(TimeUnit.HOURS.toSeconds(4));
    private int gameServerId = -1;

    private CassandraTrackingInfoPersister() {
        super();
    }

    public int getGameServerId() {
        assert gameServerId > 0;
        return gameServerId;
    }

    public void setGameServerId(int gameServerId) {
        if (gameServerId < 0) {
            throw new RuntimeException("gameServerId must be positive");
        }
        if (this.gameServerId == -1) {
            this.gameServerId = gameServerId;
        } else {
            throw new RuntimeException("gameServer value cannot be changed, current=" + this.gameServerId +
                    ", new =" + gameServerId);
        }
    }

    public String getKey(String trackerName) {
        return trackerName + ID_DELIMITER + getGameServerId();
    }

    public boolean isTracking(String trackerName, long trackedObjectId) {
        return isTracking(trackerName, String.valueOf(trackedObjectId));
    }

    public boolean isTracking(String trackerName, String trackedObjectId) {
        String key = getKey(trackerName);
        Statement query = getSelectAllColumnsQuery()
                .where(eq(getKeyColumnName(), key))
                .and(eq(OBJECT_ID_FIELD, trackedObjectId));
        ResultSet resultSet = execute(query, "isTracking");
        return resultSet.one() != null;
    }

    public void persist(String trackerName, long trackedObjectId) {
        persist(trackerName, String.valueOf(trackedObjectId));
    }

    public void persist(String trackerName, String trackedObjectId) {
        String key = getKey(trackerName);
        Statement query = getInsertQuery()
                .value(KEY, key)
                .value(OBJECT_ID_FIELD, trackedObjectId)
                .value(SERIALIZED_COLUMN_NAME, EMPTY_BYTE_BUFFER)
                .value(JSON_COLUMN_NAME, "");
        execute(query, "persist");
    }

    public void persistWithAdditionClassInfo(String trackerName, String trackedObjectId,
                                             ICommonTrackingTaskDelegate addition) {
        persist(trackerName, trackedObjectId, addition, true);
    }

    public void persist(String trackerName, String trackedObjectId, ICommonTrackingTaskDelegate addition) {
        persist(trackerName, trackedObjectId, addition, false);
    }

    public void persist(String trackerName, String trackedObjectId, Object addition,
                        boolean persistWithAdditionClassInfo) {
        String key = getKey(trackerName);
        String json = persistWithAdditionClassInfo 
                ? TABLE.serializeWithClassToJson(addition) 
                : TABLE.serializeToJson(addition);
        ByteBuffer byteBuffer = persistWithAdditionClassInfo ?
                TABLE.serializeWithClassToBytes(addition) : TABLE.serializeToBytes(addition);
        try {
            Statement query = getInsertQuery()
                    .value(KEY, key)
                    .value(OBJECT_ID_FIELD, trackedObjectId)
                    .value(SERIALIZED_COLUMN_NAME, byteBuffer)
                    .value(JSON_COLUMN_NAME, json);
            execute(query, "persist");
        } finally {
            releaseBuffer(byteBuffer);
        }
    }


    public void delete(String trackerName, long trackedObjectId) {
        delete(trackerName, String.valueOf(trackedObjectId));
    }

    public void delete(String trackerName, String trackedObjectId) {
        String key = getKey(trackerName);
        Statement query = QueryBuilder.delete()
                .from(getMainColumnFamilyName())
                .where(eq(getKeyColumnName(), key))
                .and(eq(OBJECT_ID_FIELD, trackedObjectId));
        execute(query, " deleteColumn");
    }

    public List<TrackingInfo> getList(String trackerName) {
        String key = getKey(trackerName);
        List<TrackingInfo> result = new LinkedList();
        ResultSet resultSet = execute(
                QueryBuilder.select().
                        column(OBJECT_ID_FIELD).writeTime(SERIALIZED_COLUMN_NAME).writeTime(JSON_COLUMN_NAME).
                        from(getMainColumnFamilyName()).
                        where(eq(getKeyColumnName(), key)
                        ), "getList");
        for (Row row : resultSet) {
            String trackedObjectId = row.getString(OBJECT_ID_FIELD);
            Long writeTimes = row.getLong("writetime(" + SERIALIZED_COLUMN_NAME + ")");
            Long writeTimej = row.getLong("writetime(" + JSON_COLUMN_NAME + ")");

            Long writeTime = Long.max(writeTimes == null ? -1 : writeTimes,
                    writeTimej == null ? -1 : writeTimej);

            if (writeTime <= 0) {
                ColumnDefinitions definitions = resultSet.getColumnDefinitions();
                for (ColumnDefinitions.Definition definition : definitions) {
                    LOG.info("getList: writetime(OBJECT_ID) not found: " + definition.getName() + ": " +
                            definition.getType());
                }
            } else if (!StringUtils.isTrimmedEmpty(trackedObjectId)) {
                try {
                    result.add(new TrackingInfo(Long.valueOf(trackedObjectId), writeTime));
                } catch (NumberFormatException e) {
                    LOG.error("For trackerName+GsId=" + key + ". Incorrect column name=" + trackedObjectId, e);
                }
            }
        }
        return result;
    }

    /**
     * Load all items by provided trackerName and current game server. Addition values will be deserialized with class,
     * i.e. must be used if addition was saved with "persistWithAdditionClassInfo"=true
     *
     * @param trackerName
     * @param <T>         type of saved addition
     * @return
     */
    public <T> Map<String, T> getTrackingInfo(String trackerName) {
        return getTrackingInfos(trackerName, true, null);
    }

    /**
     * Load all items by provided trackerName and current game server. Addition values will be deserialized based on
     * provided aClass, i.e. must be used if addition was saved just as byte[] (with "persistWithAdditionClassInfo"=false).
     *
     * @param trackerName
     * @param aClass<T>   class of saved addition items
     * @return
     */
    public <T> Map<String, T> getTrackingInfo(String trackerName, Class<T> aClass) {
        return getTrackingInfos(trackerName, false, aClass);
    }

    protected <T> Map<String, T> getTrackingInfos(String trackerName,
                                                  boolean isPersistedWithAdditionClassInfo,
                                                  Class<T> aClass) {
        String key = getKey(trackerName);
        Map<String, T> result = new HashMap<>();
        Statement query = QueryBuilder.select()
                .column(OBJECT_ID_FIELD)
                .column(SERIALIZED_COLUMN_NAME).writeTime(SERIALIZED_COLUMN_NAME)
                .column(JSON_COLUMN_NAME).writeTime(JSON_COLUMN_NAME)
                .from(getMainColumnFamilyName())
                .where(eq(getKeyColumnName(), key));
        ResultSet resultSet = execute(query, "getList");
        for (Row row : resultSet) {
            String trackedObjectId = row.getString(OBJECT_ID_FIELD);
            Long writeTimes = null;
            Long writeTimej = null;
            try {
                writeTimes = row.getLong("writetime(" + SERIALIZED_COLUMN_NAME + ")");
                writeTimej = row.getLong("writetime(" + JSON_COLUMN_NAME + ")");
            } catch (Exception e) {
                getLog().error("getTrackingInfos: load writetime error,", e);
            }
            Long writeTime = Long.max(writeTimes == null ? -1 : writeTimes,
                    writeTimej == null ? -1 : writeTimej);
            if (writeTime <= 0) {
                ColumnDefinitions definitions = resultSet.getColumnDefinitions();
                for (ColumnDefinitions.Definition definition : definitions) {
                    LOG.info("getList: writetime(OBJECT_ID) not found: " + definition.getName() + ": " +
                            definition.getType());
                }
            }
            T addition = null;
            String json = row.getString(JSON_COLUMN_NAME);
            addition = isPersistedWithAdditionClassInfo 
                    ? TABLE.deserializeWithClassFromJson(json) 
                    : TABLE.deserializeFromJson(json, aClass);

            if (addition == null) {
                ByteBuffer bytes = row.getBytes(SERIALIZED_COLUMN_NAME);
                if (bytes != null && !StringUtils.isTrimmedEmpty(trackedObjectId)) {
                    addition = isPersistedWithAdditionClassInfo ? TABLE.<T>deserializeWithClassFrom(bytes) :
                            TABLE.deserializeFrom(bytes, aClass);
                }
            }
            if (addition != null) {
                result.put(trackedObjectId, addition);
            }
        }
        return result;
    }

    public <T> T getTrackingInfo(String trackerName, String trackedObjectId, boolean isPersistedWithAdditionClassInfo, Class<T> aClass) {
        String key = getKey(trackerName);
        Statement query = QueryBuilder.select()
                .column(SERIALIZED_COLUMN_NAME).writeTime(SERIALIZED_COLUMN_NAME)
                .column(JSON_COLUMN_NAME).writeTime(JSON_COLUMN_NAME)
                .from(getMainColumnFamilyName())
                .where(eq(getKeyColumnName(), key))
                .and(eq(OBJECT_ID_FIELD, trackedObjectId));
        ResultSet resultSet = execute(query, "getTrackingInfo");
        T addition = null;
        if (!resultSet.isExhausted()) {
            Row row = resultSet.one();
            Long writeTimes = null;
            Long writeTimej = null;
            try {
                writeTimes = row.getLong("writetime(" + SERIALIZED_COLUMN_NAME + ")");
                writeTimej = row.getLong("writetime(" + JSON_COLUMN_NAME + ")");
            } catch (Exception e) {
                getLog().error("getTrackingInfos: load writetime error,", e);
            }
            Long writeTime = Long.max(writeTimes == null ? -1 : writeTimes,
                    writeTimej == null ? -1 : writeTimej);
            if (writeTime <= 0) {
                ColumnDefinitions definitions = resultSet.getColumnDefinitions();
                for (ColumnDefinitions.Definition definition : definitions) {
                    LOG.info("getList: writetime(OBJECT_ID) not found: " + definition.getName() + ": " +
                            definition.getType());
                }
            }
            String json = row.getString(JSON_COLUMN_NAME);
            addition = isPersistedWithAdditionClassInfo 
                    ? TABLE.deserializeWithClassFromJson(json)
                    : TABLE.deserializeFromJson(json, aClass);

            if (addition == null) {
                ByteBuffer bytes = row.getBytes(SERIALIZED_COLUMN_NAME);
                if (bytes != null && !StringUtils.isTrimmedEmpty(trackedObjectId)) {
                    addition = isPersistedWithAdditionClassInfo ? TABLE.<T>deserializeWithClassFrom(bytes) :
                            TABLE.deserializeFrom(bytes, aClass);
                }
            }
        }
        return addition;
    }

    @Override
    public TableDefinition getMainTableDefinition() {
        return TABLE;
    }

    @Override
    public Logger getLog() {
        return LOG;
    }
}
