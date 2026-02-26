package com.betsoft.casino.mp.data.persister;

import com.betsoft.casino.mp.model.ActiveCashBonusSession;
import com.betsoft.casino.mp.service.IActiveCashBonusSessionService;
import com.dgphoenix.casino.cassandra.persist.engine.AbstractCassandraPersister;
import com.dgphoenix.casino.cassandra.persist.engine.ColumnDefinition;
import com.dgphoenix.casino.cassandra.persist.engine.TableDefinition;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * User: flsh
 * Date: 22.08.18.
 */
public class ActiveCashBonusSessionPersister extends AbstractCassandraPersister<Long, String>
        implements IActiveCashBonusSessionService<ActiveCashBonusSession> {
    private static final Logger LOG = LogManager.getLogger(ActiveCashBonusSessionPersister.class);

    private static final String CF_NAME = "ActiveCashBonusSession";
    private static final String KEY_COLUMN = "id";
    private static final String ACCOUNT_ID_COLUMN = "accountId";

    private static final TableDefinition TABLE = new TableDefinition(CF_NAME,
            Arrays.asList(
                    new ColumnDefinition(KEY_COLUMN, com.datastax.driver.core.DataType.bigint(), false, false, true),
                    new ColumnDefinition(ACCOUNT_ID_COLUMN, com.datastax.driver.core.DataType.bigint(), false, true, false),
                    new ColumnDefinition(SERIALIZED_COLUMN_NAME, com.datastax.driver.core.DataType.blob()),
                    new ColumnDefinition(JSON_COLUMN_NAME, com.datastax.driver.core.DataType.text())
            ), KEY_COLUMN);

    @Override
    public TableDefinition getMainTableDefinition() {
        return TABLE;
    }

    @Override
    public String getMainColumnFamilyName() {
        return CF_NAME;
    }

    @Override
    protected String getKeyColumnName() {
        return KEY_COLUMN;
    }

    @Override
    public Logger getLog() {
        return LOG;
    }

    @Override
    public List<ActiveCashBonusSession> getByAccountId(long accountId) {
        com.datastax.driver.core.ResultSet rows = execute(getSelectColumnsQuery(SERIALIZED_COLUMN_NAME, JSON_COLUMN_NAME)
                        .where(eq(ACCOUNT_ID_COLUMN, accountId)),
                "getByAccountId");
        List<ActiveCashBonusSession> result = new ArrayList<>();
        for (com.datastax.driver.core.Row row : rows) {
            ActiveCashBonusSession session = 
                    TABLE.deserializeFromJson(row.getString(JSON_COLUMN_NAME), 
                            ActiveCashBonusSession.class);
            if (session == null) {
                ByteBuffer bytes = row.getBytes(SERIALIZED_COLUMN_NAME);
                if (bytes != null) {
                    session = TABLE.deserializeFrom(bytes, ActiveCashBonusSession.class);
                }
            }
            if (session != null) {
                result.add(session);
            }
        }
        return result;
    }

    @Override
    public ActiveCashBonusSession get(Long id) {
        String json = getJson(id);
        ActiveCashBonusSession session = TABLE.deserializeFromJson(json, ActiveCashBonusSession.class);

        if (session == null) {
            ByteBuffer bytes = get(id, SERIALIZED_COLUMN_NAME);
            session = TABLE.deserializeFrom(bytes, ActiveCashBonusSession.class);
        }
        return session;
    }

    @Override
    public void persist(ActiveCashBonusSession activeSession) {
        ByteBuffer byteBuffer = TABLE.serializeToBytes(activeSession);
        String json = TABLE.serializeToJson(activeSession);
        try {
            execute(getInsertQuery()
                            .value(KEY_COLUMN, activeSession.getId())
                            .value(ACCOUNT_ID_COLUMN, activeSession.getAccountId())
                            .value(SERIALIZED_COLUMN_NAME, byteBuffer)
                            .value(JSON_COLUMN_NAME, json),
                    "persist");
        } finally {
            releaseBuffer(byteBuffer);
        }
    }

    @Override
    public void remove(Long id) {
        deleteItem(id);
    }
}
