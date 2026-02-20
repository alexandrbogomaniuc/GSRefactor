package com.dgphoenix.casino.cassandra.persist;

import com.datastax.driver.core.DataType;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Update;
import com.dgphoenix.casino.cassandra.persist.engine.AbstractCassandraPersister;
import com.dgphoenix.casino.cassandra.persist.engine.ColumnDefinition;
import com.dgphoenix.casino.cassandra.persist.engine.TableDefinition;
import com.dgphoenix.casino.cassandra.persist.engine.configuration.Caching;
import com.dgphoenix.casino.cassandra.persist.engine.configuration.CompactionStrategy;
import com.dgphoenix.casino.common.cache.data.payment.bonus.FRBonusWin;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * User: flsh
 * Date: 30.06.15.
 */
public class CassandraFRBonusWinPersister extends AbstractCassandraPersister<Long, String> {
    //main CF, key is accountId
    public static final String FRBONUS_WIN_CF = "FrBonusWinCF";
    private static final Logger LOG = LogManager.getLogger(CassandraFRBonusWinPersister.class);
    private static final TableDefinition TABLE = new TableDefinition(FRBONUS_WIN_CF,
            Arrays.asList(
                    new ColumnDefinition(KEY, DataType.bigint(), false, false, true),
                    new ColumnDefinition(SERIALIZED_COLUMN_NAME, DataType.blob()),
                    new ColumnDefinition(JSON_COLUMN_NAME, DataType.text())
            ), KEY)
            .caching(Caching.NONE)
            .compaction(CompactionStrategy.LEVELED);

    private CassandraFRBonusWinPersister() {
    }

    @Override
    public TableDefinition getMainTableDefinition() {
        return TABLE;
    }

    public void persist(FRBonusWin win) {
        LOG.debug("persist: " + win);
        Update query = getUpdateQuery(win.getAccountId());
        String json = TABLE.serializeToJson(win);
        ByteBuffer byteBuffer = TABLE.serializeToBytes(win);
        try {
            query.with(QueryBuilder.set(SERIALIZED_COLUMN_NAME, byteBuffer));
            query.with(QueryBuilder.set(JSON_COLUMN_NAME, json));
            execute(query, "persist");
        } finally {
            releaseBuffer(byteBuffer);
        }
    }

    public FRBonusWin get(long id) {
        return get(id, FRBonusWin.class);
    }

    public void delete(long id) {
        LOG.debug("delete: " + id);
        deleteItem(id);
    }

    @Override
    public Logger getLog() {
        return LOG;
    }
}
