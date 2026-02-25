package com.dgphoenix.casino.promo.persisters;

import com.datastax.driver.core.DataType;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.dgphoenix.casino.cassandra.persist.engine.AbstractCassandraPersister;
import com.dgphoenix.casino.cassandra.persist.engine.ColumnDefinition;
import com.dgphoenix.casino.cassandra.persist.engine.TableDefinition;
import com.dgphoenix.casino.promo.icon.TournamentIcon;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class CassandraTournamentIconPersister extends AbstractCassandraPersister<Long, String> {
    private static final Logger LOG = LogManager.getLogger(CassandraTournamentIconPersister.class);

    private static final String TOURNAMENT_ICON_CF = "TournamentIconCF";
    private static final String ICON_ID_FIELD = "id";
    private static final String ICON_NAME_FIELD = "n";
    private static final String ICON_HTTP_ADDRESS_FIELD = "ha";

    private static final TableDefinition TOURNAMENT_ICON_TABLE = new TableDefinition(TOURNAMENT_ICON_CF,
            Arrays.asList(
                    new ColumnDefinition(ICON_ID_FIELD, DataType.bigint(), false, false, true),
                    new ColumnDefinition(ICON_NAME_FIELD, DataType.text()),
                    new ColumnDefinition(ICON_HTTP_ADDRESS_FIELD, DataType.text())
            ), ICON_ID_FIELD);

    public void persist(TournamentIcon icon) {
        Statement query = getInsertQuery()
                .value(ICON_ID_FIELD, icon.getId())
                .value(ICON_NAME_FIELD, icon.getName())
                .value(ICON_HTTP_ADDRESS_FIELD, icon.getHttpAddress());
        execute(query, "persist");
    }

    public List<TournamentIcon> getAllIcons() {
        Statement query = getSelectAllColumnsQuery();
        ResultSet resultSet = execute(query, "getAllIcons");
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
        Statement query = getSelectColumnsQuery(ICON_NAME_FIELD, ICON_HTTP_ADDRESS_FIELD)
                .where(eq(ICON_ID_FIELD, id)).limit(1);
        Row row = execute(query, "getById").one();
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
