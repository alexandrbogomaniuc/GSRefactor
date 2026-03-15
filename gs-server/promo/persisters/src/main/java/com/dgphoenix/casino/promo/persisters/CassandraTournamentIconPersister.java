package com.abs.casino.promo.persisters;

import com.abs.casino.cassandra.persist.engine.AbstractCassandraPersister;
import com.abs.casino.cassandra.persist.engine.ColumnDefinition;
import com.abs.casino.cassandra.persist.engine.TableDefinition;
import com.abs.casino.promo.icon.TournamentIcon;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.abs.casino.cassandra.persist.engine.CassandraDataTypes.bigint;
import static com.abs.casino.cassandra.persist.engine.CassandraDataTypes.text;

public class CassandraTournamentIconPersister extends AbstractCassandraPersister<Long, String> {
    private static final Logger LOG = LogManager.getLogger(CassandraTournamentIconPersister.class);

    private static final String TOURNAMENT_ICON_CF = "TournamentIconCF";
    private static final String ICON_ID_FIELD = "id";
    private static final String ICON_NAME_FIELD = "n";
    private static final String ICON_HTTP_ADDRESS_FIELD = "ha";

    private static final TableDefinition TOURNAMENT_ICON_TABLE = new TableDefinition(TOURNAMENT_ICON_CF,
            Arrays.asList(
                    new ColumnDefinition(ICON_ID_FIELD, bigint(), false, false, true),
                    new ColumnDefinition(ICON_NAME_FIELD, text()),
                    new ColumnDefinition(ICON_HTTP_ADDRESS_FIELD, text())
            ), ICON_ID_FIELD);

    public void persist(TournamentIcon icon) {
        com.datastax.driver.core.Statement query = getInsertQuery()
                .value(ICON_ID_FIELD, icon.getId())
                .value(ICON_NAME_FIELD, icon.getName())
                .value(ICON_HTTP_ADDRESS_FIELD, icon.getHttpAddress());
        execute(query, "persist");
    }

    public List<TournamentIcon> getAllIcons() {
        com.datastax.driver.core.Statement query = getSelectAllColumnsQuery();
        com.abs.casino.cassandra.persist.engine.ResultSet resultSet = execute(query, "getAllIcons");
        return resultSet.all().stream()
                .filter(Objects::nonNull)
                .map(row -> {
                    long id = row.getLong(ICON_ID_FIELD);
                    String name = row.getString(ICON_NAME_FIELD);
                    String httpAddress = row.getString(ICON_HTTP_ADDRESS_FIELD);
                    return new TournamentIcon(id, name, httpAddress);
                })
                .collect(Collectors.toList());
    }

    public TournamentIcon getById(long id) {
        com.datastax.driver.core.Statement query = getSelectColumnsQuery(ICON_NAME_FIELD, ICON_HTTP_ADDRESS_FIELD)
                .where(eq(ICON_ID_FIELD, id)).limit(1);
        com.abs.casino.cassandra.persist.engine.Row row = execute(query, "getById").one();
        if (row == null) {
            return null;
        }
        String name = row.getString(ICON_NAME_FIELD);
        String httpAddress = row.getString(ICON_HTTP_ADDRESS_FIELD);
        return new TournamentIcon(id, name, httpAddress);
    }

    @Override
    public TableDefinition getMainTableDefinition() {
        return TOURNAMENT_ICON_TABLE;
    }

    @Override
    public Logger getLog() {
        return LOG;
    }
}
