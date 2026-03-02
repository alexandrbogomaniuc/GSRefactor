package com.abs.casino.promo.persisters;

import com.abs.casino.cassandra.persist.engine.AbstractCassandraPersister;
import com.abs.casino.cassandra.persist.engine.ColumnDefinition;
import com.abs.casino.cassandra.persist.engine.TableDefinition;
import com.abs.casino.common.promo.ai.IPromoFeedPersister;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;

public class CassandraPromoFeedPersister extends AbstractCassandraPersister<String, String> implements IPromoFeedPersister {
    private static final Logger LOG = LogManager.getLogger(CassandraPromoFeedPersister.class);

    private static final String CF_NAME = "PromoFeedCF";
    private static final String TOURNAMENT_ID_COLUMN = "id";
    private static final String TIME_COLUMN = "t";
    private static final String FEED_COLUMN = "f";

    private static final TableDefinition FEED_TABLE = new TableDefinition(CF_NAME,
            Arrays.asList(
                    new ColumnDefinition(TOURNAMENT_ID_COLUMN, com.datastax.driver.core.DataType.bigint(), false, false, true),
                    new ColumnDefinition(TIME_COLUMN, com.datastax.driver.core.DataType.cint(), false, false, true),
                    new ColumnDefinition(FEED_COLUMN, com.datastax.driver.core.DataType.varchar())
            ), TOURNAMENT_ID_COLUMN);

    @Override
    public void persist(long tournamentId, long time, String feed) {
        com.datastax.driver.core.Statement insert = getInsertQuery()
                .value(TOURNAMENT_ID_COLUMN, tournamentId)
                .value(TIME_COLUMN, time)
                .value(FEED_COLUMN, feed);

        execute(insert, "saveFeed");
    }

    @Override
    public String get(long tournamentId, long time) {
        com.datastax.driver.core.Statement query = getSelectColumnsQuery(FEED_TABLE, FEED_COLUMN)
                .where(eq(TOURNAMENT_ID_COLUMN, tournamentId))
                .and(eq(TIME_COLUMN, time))
                .limit(1);

        com.datastax.driver.core.Row result = execute(query, "getFeed").one();
        if (result != null) {
            return result.getString(FEED_COLUMN);
        }
        return null;
    }

    @Override
    public TableDefinition getMainTableDefinition() {
        return FEED_TABLE;
    }

    @Override
    public Logger getLog() {
        return LOG;
    }
}
