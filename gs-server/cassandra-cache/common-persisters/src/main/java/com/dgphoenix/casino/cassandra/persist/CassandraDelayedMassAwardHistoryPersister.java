package com.abs.casino.cassandra.persist;

import com.abs.casino.cassandra.persist.engine.AbstractCassandraPersister;
import com.abs.casino.cassandra.persist.engine.ColumnDefinition;
import com.abs.casino.cassandra.persist.engine.TableDefinition;
import com.abs.casino.common.cache.data.bonus.DelayedMassAward;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * User: Grien
 * Date: 28.01.2015 13:56
 */
public class CassandraDelayedMassAwardHistoryPersister extends AbstractCassandraPersister<Long, String> {
    private static final Logger LOG = LogManager.getLogger(CassandraDelayedMassAwardHistoryPersister.class);
    private static final String DELAYED_MASS_AWARD_CF = "DMassAwardHistoryCF";
    private static final String GS_ID_FIELD = "GsId";
    private static final TableDefinition TABLE = new TableDefinition(DELAYED_MASS_AWARD_CF,
            Arrays.asList(
                    new ColumnDefinition(KEY, com.datastax.driver.core.DataType.bigint(), false, false, true),
                    new ColumnDefinition(GS_ID_FIELD, com.datastax.driver.core.DataType.cint()),
                    new ColumnDefinition(SERIALIZED_COLUMN_NAME, com.datastax.driver.core.DataType.blob()),
                    new ColumnDefinition(JSON_COLUMN_NAME, com.datastax.driver.core.DataType.text())
            ),
            KEY);

    private CassandraDelayedMassAwardHistoryPersister() {
    }

    @Override
    public TableDefinition getMainTableDefinition() {
        return TABLE;
    }

    @Override
    public Logger getLog() {
        return LOG;
    }

    public void create(DelayedMassAward award, int gameServerId) {
        ByteBuffer byteBuffer = TABLE.serializeToBytes(award);
        String json = TABLE.serializeToJson(award);
        try {
            com.datastax.driver.core.Statement query = getInsertQuery()
                    .value(KEY, award.getId())
                    .value(GS_ID_FIELD, gameServerId)
                    .value(SERIALIZED_COLUMN_NAME, byteBuffer)
                    .value(JSON_COLUMN_NAME, json);
            execute(query, "create");
        } finally {
            releaseBuffer(byteBuffer);
        }
    }

    public DelayedMassAward get(long id) {
        return get(id, DelayedMassAward.class);
    }
}
