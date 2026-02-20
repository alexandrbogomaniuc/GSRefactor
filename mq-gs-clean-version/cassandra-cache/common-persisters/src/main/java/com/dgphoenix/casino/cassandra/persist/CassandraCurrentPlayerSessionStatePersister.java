package com.dgphoenix.casino.cassandra.persist;

import com.datastax.driver.core.DataType;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.querybuilder.Insert;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.datastax.driver.core.querybuilder.Update;
import com.dgphoenix.casino.cassandra.persist.engine.AbstractCassandraPersister;
import com.dgphoenix.casino.cassandra.persist.engine.ColumnDefinition;
import com.dgphoenix.casino.cassandra.persist.engine.TableDefinition;
import com.dgphoenix.casino.cassandra.persist.engine.configuration.Caching;
import com.dgphoenix.casino.cassandra.persist.engine.configuration.CompactionStrategy;
import com.dgphoenix.casino.cassandra.persist.engine.configuration.Compression;
import com.dgphoenix.casino.common.util.string.StringUtils;
import com.dgphoenix.casino.common.web.statistics.StatisticsManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * User: flsh
 * Date: 27.03.12
 */
public class CassandraCurrentPlayerSessionStatePersister extends AbstractCassandraPersister<String, String> {
    private static final Logger LOG = LogManager.getLogger(CassandraCurrentPlayerSessionStatePersister.class);
    public static final String CURRENT_PLAYER_SESSION_STATE = "CurrentPlayerSessionState";

    private static final String SID_FIELD = "sid";
    private static final String DAY_TIME_FIELD = "dayTime";
    private static final String PRIVATE_ROOM_ID_FIELD = "privateRoomId";
    private static final String IS_FINISH_GAME_SESSION_FIELD = "isFinishGameSession";

    private static final TableDefinition CURRENT_PLAYER_SESSION_STATE_TABLE
            = new TableDefinition(CURRENT_PLAYER_SESSION_STATE, Arrays.asList(
                    new ColumnDefinition(KEY, DataType.text(), false, false, false),
                    new ColumnDefinition(SID_FIELD, DataType.text(), false, true, false),
                    new ColumnDefinition(DAY_TIME_FIELD, DataType.bigint(), false, false, false),
                    new ColumnDefinition(IS_FINISH_GAME_SESSION_FIELD, DataType.cboolean(), false, false, false),
                    new ColumnDefinition(PRIVATE_ROOM_ID_FIELD, DataType.text(), false, true, false)

            ), KEY)
            .caching(Caching.NONE)
            .compaction(CompactionStrategy.LEVELED)
            .gcGraceSeconds(TimeUnit.DAYS.toSeconds(1))
            .compression(Compression.NONE);

    protected CassandraCurrentPlayerSessionStatePersister() {
    }

    @Override
    public TableDefinition getMainTableDefinition() {
        return CURRENT_PLAYER_SESSION_STATE_TABLE;
    }

    public Logger getLog() {
        return LOG;
    }

    private CassandraPlayerSessionState extractFromRow(Row row) {

        if(row == null) {
            return null;
        }

        return new CassandraPlayerSessionState(
                row.getString(SID_FIELD),
                row.getString(KEY),
                row.getString(PRIVATE_ROOM_ID_FIELD),
                row.getBool(IS_FINISH_GAME_SESSION_FIELD),
                row.getLong(DAY_TIME_FIELD)
        );
    }

    public CassandraPlayerSessionState getBySid(String sid) {
        Select query = getSelectAllColumnsQuery(getMainTableDefinition());
        query.where()
                .and(eq(SID_FIELD, sid))
                .limit(1);
        query.allowFiltering();

        getLog().debug("getBySid: sid={}, query={}", sid, query);

        ResultSet resultSet = execute(query, "getBySid");
        Row row = resultSet.one();
        return extractFromRow(row);
    }

    public CassandraPlayerSessionState getByExtId(String extId) {

        Select query = getSelectAllColumnsQuery(getMainTableDefinition());
        query.where()
                .and(eq(KEY, extId))
                .limit(1);

        getLog().debug("getByExtId: extId={}, query={}", extId, query);

        ResultSet resultSet = execute(query, "getByExtId");
        Row row = resultSet.one();

        return extractFromRow(row);
    }

    public CassandraPlayerSessionState getPlayerSessionWithUnfinishedSid(String extId) {

        CassandraPlayerSessionState cassandraPlayerSessionState = getByExtId(extId);

        getLog().debug("getPlayerSessionWithUnfinishedSid: extId={}, cassandraPlayerSessionState={}",
                extId, cassandraPlayerSessionState);

        if(cassandraPlayerSessionState == null) {
            return null;
        }

        if(cassandraPlayerSessionState.isFinishGameSession()) {
            return null;
        } else {
            return cassandraPlayerSessionState;
        }
    }

    public void persist(CassandraPlayerSessionState cassandraPlayerSessionState) {

        getLog().debug("persist: cassandraPlayerSessionState={}", cassandraPlayerSessionState);
        long now = System.currentTimeMillis();

        if(cassandraPlayerSessionState == null) {
            getLog().error("persist: cassandraPlayerSessionState is null");
            return;
        }

        String sid = cassandraPlayerSessionState.getSid();

        if(StringUtils.isTrimmedEmpty(sid)) {
            getLog().error("persist: cassandraPlayerSessionState.getSid() is null or empty: {}", cassandraPlayerSessionState);
            return;
        }

        if(cassandraPlayerSessionState.getDayTime() == 0) {
            cassandraPlayerSessionState.setDayTime(now);
            return;
        }

        getLog().debug("persist: call getBySid({})", sid);
        CassandraPlayerSessionState currentCassandraPlayerSessionState = getBySid(sid);
        getLog().debug("persist: 1 currentCassandraPlayerSessionState is {}", currentCassandraPlayerSessionState);

        if(currentCassandraPlayerSessionState == null) {

            String extId = cassandraPlayerSessionState.getExtId();

            if(StringUtils.isTrimmedEmpty(extId)) {
                getLog().error("persist: cassandraPlayerSessionState.getExtId() is null or empty: {}", cassandraPlayerSessionState);
                return;
            }

            getLog().debug("persist: call getByExtId({})", extId);
            currentCassandraPlayerSessionState = getByExtId(extId);
            getLog().debug("persist: 2 currentCassandraPlayerSessionState is {}", currentCassandraPlayerSessionState);
        }

        if(currentCassandraPlayerSessionState == null) {

            getLog().debug("persist: currentCassandraPlayerSessionState is null, insert new record:{}",
                    cassandraPlayerSessionState);

            Insert insertQuery = getInsertQuery();
            insertQuery.value(KEY, cassandraPlayerSessionState.getExtId());
            insertQuery.value(SID_FIELD, sid);
            insertQuery.value(PRIVATE_ROOM_ID_FIELD, cassandraPlayerSessionState.getPrivateRoomId());
            insertQuery.value(IS_FINISH_GAME_SESSION_FIELD, cassandraPlayerSessionState.isFinishGameSession());
            insertQuery.value(DAY_TIME_FIELD, cassandraPlayerSessionState.getDayTime());

            execute(insertQuery, "insert");
            getLog().info("insert: cassandraPlayerSessionState: {}", cassandraPlayerSessionState);

        } else {

            getLog().debug("persist: currentCassandraPlayerSessionState is not null, update existing record to:{}",
                    cassandraPlayerSessionState);

            Update updateQuery = getUpdateQuery();

            updateQuery.with()
                    .and(QueryBuilder.set(SID_FIELD, cassandraPlayerSessionState.getSid()))
                    .and(QueryBuilder.set(PRIVATE_ROOM_ID_FIELD, cassandraPlayerSessionState.getPrivateRoomId()))
                    .and(QueryBuilder.set(IS_FINISH_GAME_SESSION_FIELD, cassandraPlayerSessionState.isFinishGameSession()))
                    .and(QueryBuilder.set(DAY_TIME_FIELD, cassandraPlayerSessionState.getDayTime()));
            updateQuery.where()
                    .and(eq(KEY, currentCassandraPlayerSessionState.getExtId()));

            execute(updateQuery, "update");
            getLog().info("update: cassandraPlayerSessionState from: {} to: {}",
                    currentCassandraPlayerSessionState, cassandraPlayerSessionState);
        }

        StatisticsManager.getInstance().updateRequestStatistics(getClass().getSimpleName() + " persist",
                System.currentTimeMillis() - now);
    }

    public void persist(String sid, String extId, String privateRoomId, boolean isFinishGameSession, long dateTime) {
        persist(new CassandraPlayerSessionState(sid, extId, privateRoomId, isFinishGameSession, dateTime));
    }

    public boolean delete(String sid) {
        return super.deleteWithCheck(sid);
    }

    public void delete(String... sids) {
        if (sids.length == 0) {
            return;
        }
        Statement query =
                QueryBuilder.delete().
                        from(getMainColumnFamilyName()).
                        where(QueryBuilder.in(KEY, sids));
        execute(query, "delete player Session States");
    }
}
