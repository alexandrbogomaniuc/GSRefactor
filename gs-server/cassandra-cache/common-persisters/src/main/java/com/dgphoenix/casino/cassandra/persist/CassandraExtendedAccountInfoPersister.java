package com.dgphoenix.casino.cassandra.persist;

import com.datastax.driver.core.DataType;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.dgphoenix.casino.cassandra.persist.engine.AbstractCassandraPersister;
import com.dgphoenix.casino.cassandra.persist.engine.ColumnDefinition;
import com.dgphoenix.casino.cassandra.persist.engine.TableDefinition;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.Map;

/**
 * Created by mic on 12.01.15.
 */
public class CassandraExtendedAccountInfoPersister extends AbstractCassandraPersister<String, String> implements ExtendedAccountInfoPersister {
    public static final String COLUMN_FAMILY_NAME = "ExtAccountCF";

    private static final Logger LOG = LogManager.getLogger(CassandraExtendedAccountInfoPersister.class);

    private static final String BANK_ID = "BankId";
    private static final String EXTERNAL_ID = "ExternalId";
    private static final String PROPERTIES = "Properties";

    private static final TableDefinition TABLE = new TableDefinition(COLUMN_FAMILY_NAME,
            Arrays.asList(
                    new ColumnDefinition(BANK_ID, DataType.bigint(), false, false, true),
                    new ColumnDefinition(EXTERNAL_ID, DataType.text(), false, false, true),
                    new ColumnDefinition(PROPERTIES, DataType.map(DataType.text(), DataType.text()))
            ),
            BANK_ID, EXTERNAL_ID);

    private CassandraExtendedAccountInfoPersister() {
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
    public String get(long bankId, String externalId, String propertyName) {
        Map<String, String> map = get(bankId, externalId);
        if (map != null) {
            return map.get(propertyName);
        }
        return null;
    }

    @Override
    public Map<String, String> get(long bankId, String externalId) {
        Statement select = getSelectColumnsQuery(PROPERTIES)
                .where(eq(BANK_ID, bankId))
                .and(eq(EXTERNAL_ID, externalId));
        Row row = execute(select, "get").one();
        if (row != null) {
            return row.getMap(PROPERTIES, String.class, String.class);
        }
        return null;
    }

    @Override
    public void persist(long bankId, String externalId, Map<String, String> properties) {
        Statement update = getUpdateQuery()
                .where(eq(BANK_ID, bankId))
                .and(eq(EXTERNAL_ID, externalId))
                .with(QueryBuilder.putAll(PROPERTIES, properties));
        execute(update, "persist");
    }

    @Override
    public void persist(long bankId, String externalId, String propertyName, String value) {
        Statement update = getUpdateQuery()
                .where(eq(BANK_ID, bankId))
                .and(eq(EXTERNAL_ID, externalId))
                .with(QueryBuilder.put(PROPERTIES, propertyName, value));
        execute(update, "persist");
    }

    public void delete(long bankId, String externalId, String propertyName) {
        deleteMapItem(PROPERTIES, propertyName, eq(BANK_ID, bankId), eq(EXTERNAL_ID, externalId));
    }
}
