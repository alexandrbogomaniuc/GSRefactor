package com.abs.casino.cassandra.persist;

import com.abs.casino.cassandra.persist.engine.AbstractCassandraPersister;
import com.abs.casino.cassandra.persist.engine.ColumnDefinition;
import com.abs.casino.cassandra.persist.engine.TableDefinition;
import com.abs.casino.common.cache.data.bonus.DelayedMassAward;
import com.abs.casino.common.web.statistics.StatisticsManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static com.abs.casino.cassandra.persist.engine.CassandraDataTypes.bigint;
import static com.abs.casino.cassandra.persist.engine.CassandraDataTypes.blob;
import static com.abs.casino.cassandra.persist.engine.CassandraDataTypes.cint;
import static com.abs.casino.cassandra.persist.engine.CassandraDataTypes.text;

/**
 * User: flsh
 * Date: 20.12.13
 */
public class CassandraDelayedMassAwardPersister extends AbstractCassandraPersister<Long, String> {
    private static final Logger LOG = LogManager.getLogger(CassandraDelayedMassAwardPersister.class);
    private static final String DELAYED_MASS_AWARD_CF = "DMassAwardCF";
    private static final String GS_ID_FIELD = "GsId";
    private static final TableDefinition TABLE = new TableDefinition(DELAYED_MASS_AWARD_CF,
            Arrays.asList(
                    new ColumnDefinition(GS_ID_FIELD, cint(), false, false, true),
                    new ColumnDefinition(KEY, bigint(), false, true, true),
                    new ColumnDefinition(SERIALIZED_COLUMN_NAME, blob()),
                    new ColumnDefinition(JSON_COLUMN_NAME, text())
            ),
            GS_ID_FIELD);

    private CassandraDelayedMassAwardPersister() {
    }

    @Override
    public TableDefinition getMainTableDefinition() {
        return TABLE;
    }

    @Override
    public Logger getLog() {
        return LOG;
    }

    public DelayedMassAward get(long id) {
        return get(id, DelayedMassAward.class);
    }

    public Collection<DelayedMassAward> getUncompleted(int gameServerId) {
        long now = System.currentTimeMillis();
        List<DelayedMassAward> result = new ArrayList<>();
        com.datastax.driver.core.Statement query = getSelectColumnsQuery(SERIALIZED_COLUMN_NAME, JSON_COLUMN_NAME)
                .where(eq(GS_ID_FIELD, gameServerId));
        com.datastax.driver.core.ResultSet resultSet = execute(query, "getUncompleted");
        for (com.datastax.driver.core.Row row : resultSet) {
            String json = row.getString(JSON_COLUMN_NAME);
            DelayedMassAward awardFrb = TABLE.deserializeFromJson(json, DelayedMassAward.class);
            if (awardFrb == null) {
                ByteBuffer bytes = row.getBytes(SERIALIZED_COLUMN_NAME);
                awardFrb = TABLE.deserializeFrom(bytes, DelayedMassAward.class);
            }
            if (awardFrb != null) {
                result.add(awardFrb);
            }
        }
        StatisticsManager.getInstance().updateRequestStatistics(getClass().getSimpleName() + " getUncompleted",
                System.currentTimeMillis() - now);
        return result;
    }

    public void create(DelayedMassAward award, int gameServerId) {
        String json = TABLE.serializeToJson(award);
        ByteBuffer byteBuffer = TABLE.serializeToBytes(award);
        try {
            com.datastax.driver.core.Statement query = getInsertQuery()
                    .value(KEY, award.getId())
                    .value(GS_ID_FIELD, gameServerId)
                    .value(SERIALIZED_COLUMN_NAME, byteBuffer)
                    .value(JSON_COLUMN_NAME, json);
            com.datastax.driver.core.ResultSet resultSet = execute(query, "create");
            if (!resultSet.wasApplied()) {
                getLog().error("DelayedMassAward not created: {}", award);
                throw new RuntimeException("DelayedMassAward not created");
            }
        } finally {
            releaseBuffer(byteBuffer);
        }
    }

    public void delete(long id, int serverId) {
        execute(addItemDeletion(eq(GS_ID_FIELD, serverId), eq(KEY, id)), "delete by serverId&awardId");
    }

    public void delete(long id) {
        super.deleteWithCheck(id);
    }
}
