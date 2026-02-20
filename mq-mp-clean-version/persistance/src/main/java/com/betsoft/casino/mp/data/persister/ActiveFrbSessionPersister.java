package com.betsoft.casino.mp.data.persister;

import com.betsoft.casino.mp.model.IActiveFrbSession;
import com.betsoft.casino.mp.service.IActiveFrbSessionService;
import com.datastax.driver.core.DataType;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.querybuilder.Insert;
import com.datastax.driver.core.querybuilder.Select;
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
public class ActiveFrbSessionPersister extends AbstractCassandraPersister<Long, String>
        implements IActiveFrbSessionService {
    private static final Logger LOG = LogManager.getLogger(ActiveFrbSessionPersister.class);

    private static final String CF_NAME = "ActiveFrbSession";
    private static final String KEY_COLUMN = "frbId";
    private static final String ACCOUNT_ID_COLUMN = "accountId";

    private static final TableDefinition TABLE = new TableDefinition(CF_NAME,
            Arrays.asList(
                    new ColumnDefinition(KEY_COLUMN, DataType.bigint(), false, false, true),
                    new ColumnDefinition(ACCOUNT_ID_COLUMN, DataType.bigint(), false, true, false),
                    new ColumnDefinition(SERIALIZED_COLUMN_NAME, DataType.blob()),
                    new ColumnDefinition(JSON_COLUMN_NAME, DataType.text())
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
    public List<IActiveFrbSession> getByAccountId(long accountId) {
        Select query = getSelectColumnsQuery(SERIALIZED_COLUMN_NAME, JSON_COLUMN_NAME);
        query.where(eq(ACCOUNT_ID_COLUMN, accountId));
        ResultSet rows = execute(query, "getByAccountId");
        List<IActiveFrbSession> result = new ArrayList<>();
        for (Row row : rows) {
            String json = row.getString(JSON_COLUMN_NAME);
            IActiveFrbSession session = TABLE.deserializeWithClassFromJson(json);

            if (session == null) {
                ByteBuffer bytes = row.getBytes(SERIALIZED_COLUMN_NAME);
                if (bytes != null) {
                    session = TABLE.deserializeWithClassFrom(bytes);
                }
            }
            if (session != null) {
                result.add(session);
            }
        }
        return result;
    }

    @Override
    public IActiveFrbSession get(Long id) {
        String json = getJson(id);
        IActiveFrbSession session = TABLE.deserializeWithClassFromJson(json);

        if (session == null) {
            ByteBuffer bytes = get(id, SERIALIZED_COLUMN_NAME);
            session = TABLE.deserializeWithClassFrom(bytes);
        }
        return session;
    }

    @Override
    public void persist(IActiveFrbSession activeFrbSession) {
        Insert query = getInsertQuery();
        ByteBuffer byteBuffer = TABLE.serializeWithClassToBytes(activeFrbSession);
        String json = TABLE.serializeWithClassToJson(activeFrbSession);
        try {
            query.value(KEY_COLUMN, activeFrbSession.getBonusId()).
                    value(ACCOUNT_ID_COLUMN, activeFrbSession.getAccountId()).
                    value(SERIALIZED_COLUMN_NAME, byteBuffer).
                    value(JSON_COLUMN_NAME, json);
            execute(query, "persist");
        } finally {
            releaseBuffer(byteBuffer);
        }
    }

    @Override
    public void remove(Long frbId) {
        deleteItem(frbId);
    }
}
