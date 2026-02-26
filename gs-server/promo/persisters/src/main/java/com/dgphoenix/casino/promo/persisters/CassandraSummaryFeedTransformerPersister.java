package com.dgphoenix.casino.promo.persisters;

import com.dgphoenix.casino.cassandra.persist.engine.AbstractCassandraPersister;
import com.dgphoenix.casino.cassandra.persist.engine.ColumnDefinition;
import com.dgphoenix.casino.cassandra.persist.engine.TableDefinition;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class CassandraSummaryFeedTransformerPersister extends AbstractCassandraPersister<Long, String> {
    private static final Logger LOG = LogManager.getLogger(CassandraSummaryFeedTransformerPersister.class);

    private static final String CF_NAME = "SummaryTransformerCF";
    private static final String TOURNAMENT_ID_COLUMN = "id";

    private static final TableDefinition TABLE = new TableDefinition(CF_NAME,
            Arrays.asList(
                    new ColumnDefinition(TOURNAMENT_ID_COLUMN, com.datastax.driver.core.DataType.bigint(), false, false, true),
                    new ColumnDefinition(SERIALIZED_COLUMN_NAME, com.datastax.driver.core.DataType.blob()),
                    new ColumnDefinition(JSON_COLUMN_NAME, com.datastax.driver.core.DataType.text())
            ), TOURNAMENT_ID_COLUMN);

    public void persist(long tournamentId, ISummaryFeedTransformer transformer) {
        String json = TABLE.serializeToJson(transformer);
        ByteBuffer buffer = TABLE.serializeWithClassToBytes(transformer);
        try {
            com.datastax.driver.core.Statement insert = getInsertQuery()
                    .value(TOURNAMENT_ID_COLUMN, tournamentId)
                    .value(SERIALIZED_COLUMN_NAME, buffer)
                    .value(JSON_COLUMN_NAME, json);

            execute(insert, "persistTransformer");
        } finally {
            releaseBuffer(buffer);
        }
    }

    public ISummaryFeedTransformer get(long tournamentId) {
        com.datastax.driver.core.Statement select = getSelectColumnsQuery(SERIALIZED_COLUMN_NAME, JSON_COLUMN_NAME)
                .where(eq(TOURNAMENT_ID_COLUMN, tournamentId))
                .limit(1);

        com.datastax.driver.core.Row result = execute(select, "getTransformer").one();
        if (result != null) {
            ISummaryFeedTransformer t = TABLE.deserializeWithClassFromJson(result.getString(JSON_COLUMN_NAME));
            if (t == null) {
                t = TABLE.deserializeWithClassFrom(result.getBytes(SERIALIZED_COLUMN_NAME));
            }
            return t;
        }
        return null;
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
