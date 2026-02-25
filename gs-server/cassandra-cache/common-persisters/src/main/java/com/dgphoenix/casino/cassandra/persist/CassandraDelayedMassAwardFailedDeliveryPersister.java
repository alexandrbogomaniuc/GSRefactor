package com.dgphoenix.casino.cassandra.persist;

import com.datastax.driver.core.DataType;
import com.datastax.driver.core.Statement;
import com.dgphoenix.casino.cassandra.persist.engine.AbstractCassandraPersister;
import com.dgphoenix.casino.cassandra.persist.engine.ColumnDefinition;
import com.dgphoenix.casino.cassandra.persist.engine.TableDefinition;
import com.dgphoenix.casino.common.cache.data.bonus.DelayedMassAwardDelivery;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * @author <a href="mailto:dader@dgphoenix.com">Timur Shaymardanov</a>
 * @since 05.03.2019
 */
public class CassandraDelayedMassAwardFailedDeliveryPersister extends AbstractCassandraPersister<Long, String> {
    private static final Logger LOG = LogManager.getLogger(CassandraDelayedMassAwardFailedDeliveryPersister.class);
    private static final String DELAYED_MASS_AWARD_CF = "DMassAwardFailedSendCF";
    private static final TableDefinition TABLE = new TableDefinition(DELAYED_MASS_AWARD_CF,
            Arrays.asList(
                    new ColumnDefinition(KEY, DataType.bigint(), false, false, true),
                    new ColumnDefinition(SERIALIZED_COLUMN_NAME, DataType.blob()),
                    new ColumnDefinition(JSON_COLUMN_NAME, DataType.text())
            ),
            KEY);

    public CassandraDelayedMassAwardFailedDeliveryPersister() {
    }

    @Override
    public TableDefinition getMainTableDefinition() {
        return TABLE;
    }

    @Override
    public Logger getLog() {
        return LOG;
    }

    public void create(DelayedMassAwardDelivery award) {
        ByteBuffer byteBuffer = TABLE.serializeToBytes(award);
        String json = TABLE.serializeToJson(award);
        try {
            Statement query = getInsertQuery()
                    .value(KEY, award.getId())
                    .value(SERIALIZED_COLUMN_NAME, byteBuffer)
                    .value(JSON_COLUMN_NAME, json);
            execute(query, "create");
        } finally {
            releaseBuffer(byteBuffer);
        }
    }

    public DelayedMassAwardDelivery get(long id) {
        return get(id, DelayedMassAwardDelivery.class);
    }

    public void delete(long id) {
        super.deleteWithCheck(id);
    }
}
