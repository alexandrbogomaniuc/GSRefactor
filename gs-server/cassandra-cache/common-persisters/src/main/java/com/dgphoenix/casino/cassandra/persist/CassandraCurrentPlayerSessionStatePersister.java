package com.abs.casino.cassandra.persist;

import com.abs.casino.cassandra.persist.engine.Cql;

import com.abs.casino.cassandra.persist.engine.AbstractCassandraPersister;
import com.abs.casino.cassandra.persist.engine.ColumnDefinition;
import com.abs.casino.cassandra.persist.engine.TableDefinition;
import com.abs.casino.cassandra.persist.CassandraPlayerSessionState;
import com.abs.casino.cassandra.persist.engine.configuration.Caching;
import com.abs.casino.cassandra.persist.engine.configuration.CompactionStrategy;
import com.abs.casino.cassandra.persist.engine.configuration.Compression;
import com.abs.casino.common.util.string.StringUtils;
import com.abs.casino.common.web.statistics.StatisticsManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import static com.abs.casino.cassandra.persist.engine.CassandraDataTypes.bigint;
import static com.abs.casino.cassandra.persist.engine.CassandraDataTypes.cboolean;
import static com.abs.casino.cassandra.persist.engine.CassandraDataTypes.text;

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
                    new ColumnDefinition(KEY, text(), false, false, false),
                    new ColumnDefinition(SID_FIELD, text(), false, true, false),
                    new ColumnDefinition(DAY_TIME_FIELD, bigint(), false, false, false),
                    new ColumnDefinition(IS_FINISH_GAME_SESSION_FIELD, cboolean(), false, false, false),
                    new ColumnDefinition(PRIVATE_ROOM_ID_FIELD, text(), false, true, false)

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

    private CassandraPlayerSessionState extractFromRow(com.abs.casino.cassandra.persist.engine.Row row) {

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
        com.datastax.driver.core.Statement query = getSelectAllColumnsQuery(getMainTableDefinition())
                .where(eq(SID_FIELD, sid))
                .limit(1)
                .allowFiltering();

        getLog().debug("getBySid: sid={}, query={}", sid, query);

        com.abs.casino.cassandra.persist.engine.ResultSet resultSet = executeWrapped(query, "getBySid");
        com.abs.casino.cassandra.persist.engine.Row row = resultSet.one();
        return extractFromRow(row);
    }

    public CassandraPlayerSessionState getByExtId(String extId) {

        com.datastax.driver.core.Statement query = getSelectAllColumnsQuery(getMainTableDefinition())
                .where(eq(KEY, extId))
                .limit(1);

        getLog().debug("getByExtId: extId={}, query={}", extId, query);

        com.abs.casino.cassandra.persist.engine.ResultSet resultSet = executeWrapped(query, "getByExtId");
        com.abs.casino.cassandra.persist.engine.Row row = resultSet.one();

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

            com.datastax.driver.core.Statement insertQuery = getInsertQuery()
                    .value(KEY, cassandraPlayerSessionState.getExtId())
                    .value(SID_FIELD, sid)
                    .value(PRIVATE_ROOM_ID_FIELD, cassandraPlayerSessionState.getPrivateRoomId())
                    .value(IS_FINISH_GAME_SESSION_FIELD, cassandraPlayerSessionState.isFinishGameSession())
                    .value(DAY_TIME_FIELD, cassandraPlayerSessionState.getDayTime());

            execute(insertQuery, "insert");
            getLog().info("insert: cassandraPlayerSessionState: {}", cassandraPlayerSessionState);

        } else {

            getLog().debug("persist: currentCassandraPlayerSessionState is not null, update existing record to:{}",
                    cassandraPlayerSessionState);

            com.datastax.driver.core.Statement updateQuery = getUpdateQuery()
                    .with(set(SID_FIELD, cassandraPlayerSessionState.getSid()))
                    .and(set(PRIVATE_ROOM_ID_FIELD, cassandraPlayerSessionState.getPrivateRoomId()))
                    .and(set(IS_FINISH_GAME_SESSION_FIELD, cassandraPlayerSessionState.isFinishGameSession()))
                    .and(set(DAY_TIME_FIELD, cassandraPlayerSessionState.getDayTime()))
                    .where(eq(KEY, currentCassandraPlayerSessionState.getExtId()));

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
        com.datastax.driver.core.Statement query =
                Cql.delete().
                        from(getMainColumnFamilyName()).
                        where(Cql.in(KEY, sids));
        execute(query, "delete player Session states");
    }
}
