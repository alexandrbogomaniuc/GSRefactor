package com.abs.casino.cassandra.persist;

import com.abs.casino.cassandra.persist.engine.AbstractCassandraPersister;
import com.abs.casino.cassandra.persist.engine.ColumnDefinition;
import com.abs.casino.cassandra.persist.engine.Row;
import com.abs.casino.cassandra.persist.engine.TableDefinition;
import com.abs.casino.common.cache.CacheKeyInfo;
import com.abs.casino.common.cache.IDistributedCache;
import com.abs.casino.common.cache.data.payment.bonus.FRBWinOperation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.ByteBuffer;
import java.util.*;

import static com.abs.casino.cassandra.persist.engine.CassandraDataTypes.bigint;
import static com.abs.casino.cassandra.persist.engine.CassandraDataTypes.blob;
import static com.abs.casino.cassandra.persist.engine.CassandraDataTypes.text;

/**
 * User: flsh
 * Date: 07.06.13
 */
@CacheKeyInfo(description = "FRBWinOperation.id")
public class CassandraFrbWinOperationPersister extends AbstractCassandraPersister<Long, String>
        implements IDistributedCache<Long, FRBWinOperation> {
    private static final Logger LOG = LogManager.getLogger(CassandraFrbWinOperationPersister.class);
    public static final String COLUMN_FAMILY_NAME = "FrbWinCF";
    public static final String ACCOUNT_ID_FIELD = "AccId";
    public static final String GAME_SESSION_ID_FIELD = "GameSessId";
    public static final String SERIALIZED_COLUMN_NAME = "SCN";
    private static final TableDefinition TABLE = new TableDefinition(COLUMN_FAMILY_NAME,
            Arrays.asList(
                    new ColumnDefinition(KEY, bigint(), false, false, true),
                    new ColumnDefinition(ACCOUNT_ID_FIELD, bigint(), false, true, false),
                    new ColumnDefinition(GAME_SESSION_ID_FIELD, bigint(), false, true, false),
                    new ColumnDefinition(SERIALIZED_COLUMN_NAME, blob()),
                    new ColumnDefinition(JSON_COLUMN_NAME, text())
            ),
            Collections.singletonList(KEY));

    private CassandraFrbWinOperationPersister() {
    }

    @Override
    public TableDefinition getMainTableDefinition() {
        return TABLE;
    }

    @Override
    public Logger getLog() {
        return LOG;
    }

    public FRBWinOperation getById(long id) {
        ByteBuffer buffer = get(id, SERIALIZED_COLUMN_NAME);
        String json = getJson(id);
        FRBWinOperation o = TABLE.deserializeFromJson(json, FRBWinOperation.class);
        if (o == null) {
            o = TABLE.deserializeFrom(buffer, FRBWinOperation.class);
        }
        return o;
    }

    public void persist(FRBWinOperation operation) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("persist: " + operation);
        }
        ByteBuffer byteBuffer = TABLE.serializeToBytes(operation);
        String json = TABLE.serializeToJson(operation);
        try {
            com.abs.casino.cassandra.persist.engine.Statement query = com.abs.casino.cassandra.persist.engine.Statement.of(getInsertQuery().value(KEY, operation.getId()).
                    value(ACCOUNT_ID_FIELD, operation.getAccountId()).
                    value(GAME_SESSION_ID_FIELD, operation.getGameSessionId()).
                    value(SERIALIZED_COLUMN_NAME, byteBuffer).
                    value(JSON_COLUMN_NAME, json));
            execute(query, "persist");
        } finally {
            releaseBuffer(byteBuffer);
        }
    }

    public List<FRBWinOperation> getByAccountId(long accountId) {
        List<FRBWinOperation> result = new LinkedList<>();
        Iterator<Row> it = getAllWrapped(eq(ACCOUNT_ID_FIELD, accountId));
        while (it.hasNext()) {
            Row row = it.next();
            String json = row.getString(JSON_COLUMN_NAME);
            ByteBuffer bytes = row.getBytes(SERIALIZED_COLUMN_NAME);
            FRBWinOperation operation = TABLE.deserializeFromJson(json, FRBWinOperation.class);
            if (operation == null) {
                operation = TABLE.deserializeFrom(bytes, FRBWinOperation.class);
            }
            if (operation != null) {
                result.add(operation);
            }
        }
        return result;
    }

    @Override
    public FRBWinOperation getObject(String id) {
        return getById(Long.valueOf(id));
    }

    @Override
    public Map<Long, FRBWinOperation> getAllObjects() {
        //too large, may be implement later
        return Collections.emptyMap();
    }

    @Override
    public String getAdditionalInfo() {
        return null;
    }

    @Override
    public String printDebug() {
        return null;
    }
}
