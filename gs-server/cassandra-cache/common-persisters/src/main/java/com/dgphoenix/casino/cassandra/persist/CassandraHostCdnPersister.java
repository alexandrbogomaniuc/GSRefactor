package com.abs.casino.cassandra.persist;

import com.abs.casino.cassandra.persist.engine.AbstractCassandraPersister;
import com.abs.casino.cassandra.persist.engine.ColumnDefinition;
import com.abs.casino.cassandra.persist.engine.Cql;
import com.abs.casino.cassandra.persist.engine.TableDefinition;
import com.abs.casino.common.games.CdnCheckResult;
import com.abs.casino.common.games.ICassandraHostCdnPersister;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import static com.abs.casino.cassandra.persist.engine.CassandraDataTypes.bigint;
import static com.abs.casino.cassandra.persist.engine.CassandraDataTypes.cint;
import static com.abs.casino.cassandra.persist.engine.CassandraDataTypes.text;





/**
 * Created by inter on 07.09.15.
 */
public class CassandraHostCdnPersister extends AbstractCassandraPersister<String, String> implements ICassandraHostCdnPersister {

    private static final Logger LOG = LogManager.getLogger(CassandraHostCdnPersister.class);

    public static final String COLUMN_FAMILY_NAME = "HostCdnCF";
    public static final String IP_FIELD = "IP";
    public static final String CDN_FIELD = "CDN";
    public static final String TIME_FIELD = "TIME";
    public static final String LAST_UPDATE_FIELD = "LAST_UPDATE"; // ALTER TABLE hostcdncf ADD LAST_UPDATE bigint;

    private static final TableDefinition TABLE = new TableDefinition(COLUMN_FAMILY_NAME,
            Arrays.asList(
                    new ColumnDefinition(IP_FIELD, text(), false, false, true),
                    new ColumnDefinition(CDN_FIELD, text(), false, false, true),
                    new ColumnDefinition(TIME_FIELD, cint(), false, false, false),
                    new ColumnDefinition(LAST_UPDATE_FIELD, bigint(), false, false, false)
            ), IP_FIELD);

    private CassandraHostCdnPersister() {

    }

    @Override
    public TableDefinition getMainTableDefinition() {
        return TABLE;
    }

    @Override
    public Logger getLog() {
        return LOG;
    }


    public void persist(String ip, String cdn, int time) {
        com.datastax.driver.core.Statement query = getInsertQuery()
                .value(IP_FIELD, ip)
                .value(CDN_FIELD, cdn)
                .value(TIME_FIELD, time)
                .value(LAST_UPDATE_FIELD, System.currentTimeMillis());
        execute(query, "create");
    }

    public List<CdnCheckResult> getCdnByIp(String ip) {
        com.datastax.driver.core.Statement query = Cql.select()
                .column(CDN_FIELD)
                .column(TIME_FIELD)
                .column(LAST_UPDATE_FIELD)
                .from(COLUMN_FAMILY_NAME)
                .where(Cql.eq(IP_FIELD, ip)).limit(1000);
        com.datastax.driver.core.ResultSet rows = execute(query, "getCdnByIp");

        List<CdnCheckResult> result = new ArrayList<>();
        for (com.datastax.driver.core.Row row : rows) {
            result.add(new CdnCheckResult(row.getString(CDN_FIELD), row.getInt(TIME_FIELD), row.getLong(LAST_UPDATE_FIELD)));
        }

        return result;
    }

    public void remove(String ip, String cdn) {
        com.datastax.driver.core.Statement query = Cql.delete()
                .all()
                .from(COLUMN_FAMILY_NAME)
                .where(Cql.eq(IP_FIELD, ip))
                .and(Cql.eq(CDN_FIELD, cdn));
        execute(query, "deleteItem");
    }
}
