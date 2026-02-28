package com.abs.casino.cassandra.persist;

import com.dgphoenix.casino.cassandra.persist.engine.AbstractCassandraPersister;
import com.dgphoenix.casino.cassandra.persist.engine.ColumnDefinition;
import com.dgphoenix.casino.cassandra.persist.engine.TableDefinition;
import com.abs.casino.cassandra.persist.engine.configuration.CompactionStrategy;
import com.abs.casino.common.remotecall.PersistableCall;
import com.dgphoenix.casino.common.web.statistics.StatisticsManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * User: flsh
 * Date: 02.04.13
 */
public class CassandraRemoteCallPersister extends AbstractCassandraPersister<Integer, String> {
    private static final Logger LOG = LogManager.getLogger(CassandraRemoteCallPersister.class);
    public static final String REMOTE_CALL_CF = "RcCF";
    public static final String GS_ID_FIELD = "G";
    public static final String SERIALIZED_COLUMN_NAME = "SCN";
    private static final TableDefinition TABLE = new TableDefinition(REMOTE_CALL_CF,
            Arrays.asList(
                    new ColumnDefinition(GS_ID_FIELD, com.datastax.driver.core.DataType.cint(), false, false, true),
                    new ColumnDefinition(KEY, com.datastax.driver.core.DataType.bigint(), false, false, true),
                    new ColumnDefinition(SERIALIZED_COLUMN_NAME, com.datastax.driver.core.DataType.blob()),
                    new ColumnDefinition(JSON_COLUMN_NAME, com.datastax.driver.core.DataType.text())
            ), GS_ID_FIELD)
            .compaction(CompactionStrategy.getLeveled(true, TimeUnit.HOURS.toSeconds(1)))
            .gcGraceSeconds(TimeUnit.HOURS.toSeconds(4));

    private CassandraRemoteCallPersister() {
        super();
    }

    @Override
    public TableDefinition getMainTableDefinition() {
        return TABLE;
    }

    @Override
    public Logger getLog() {
        return LOG;
    }

    @Override
    protected String getKeyColumnName() {
        return GS_ID_FIELD;
    }

    public List<PersistableCall> getRemoteCalls(int serverId) {
        long now = System.currentTimeMillis();
        List<PersistableCall> result = new ArrayList<>();
        com.datastax.driver.core.Statement query = getSelectColumnsQuery(SERIALIZED_COLUMN_NAME, JSON_COLUMN_NAME)
                .where(eq(GS_ID_FIELD, serverId));
        com.datastax.driver.core.ResultSet resultSet = execute(query, "getRemoteCalls");
        for (com.datastax.driver.core.Row row : resultSet) {
            PersistableCall call = TABLE.deserializeFromJson(row.getString(JSON_COLUMN_NAME),
                    PersistableCall.class);
            if (call == null) {
                call = TABLE.deserializeFrom(row.getBytes(SERIALIZED_COLUMN_NAME),
                        PersistableCall.class);
            }
            result.add(call);
        }
        Collections.sort(result);
        StatisticsManager.getInstance().updateRequestStatistics(getClass().getSimpleName() + " getRemoteCalls",
                System.currentTimeMillis() - now);

        return result;
    }

    public void persist(PersistableCall entry) {
        LOG.debug("entry " + entry);
        ByteBuffer byteBuffer = TABLE.serializeToBytes(entry);
        String json = TABLE.serializeToJson(entry);
        try {
            com.datastax.driver.core.Statement query = getInsertQuery().
                    value(GS_ID_FIELD, entry.getServerId()).
                    value(KEY, entry.getId()).
                    value(SERIALIZED_COLUMN_NAME, byteBuffer).
                    value(JSON_COLUMN_NAME, json);
            execute(query, "persist");
        } finally {
            releaseBuffer(byteBuffer);
        }
    }

    public void delete(int serverId, long id) {
        execute(addItemDeletion(eq(GS_ID_FIELD, serverId), eq(KEY, id)), "delete");
    }
}
