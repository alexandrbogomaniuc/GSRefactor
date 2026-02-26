package com.dgphoenix.casino.promo.persisters;

import com.dgphoenix.casino.cassandra.persist.engine.AbstractCassandraPersister;
import com.dgphoenix.casino.cassandra.persist.engine.ColumnDefinition;
import com.dgphoenix.casino.cassandra.persist.engine.TableDefinition;
import com.dgphoenix.casino.cassandra.persist.engine.configuration.CompactionStrategy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class CassandraPlayerAliasPersister extends AbstractCassandraPersister<Long, String> {
    private static final Logger LOG = LogManager.getLogger(CassandraPlayerAliasPersister.class);

    public static final long EMPTY_ALIAS_POSTFIX = -1;
    private static final String PLAYER_ALIAS_CF = "PlayerAliasCF";
    private static final String NETWORK_TOURNAMENT_ID = "ntid";
    private static final String PLAYER_ALIAS = "pa";
    private static final String ALIAS_POSTFIX = "ap";
    private static final TableDefinition PLAYER_ALIAS_TABLE = new TableDefinition(PLAYER_ALIAS_CF,
            Arrays.asList(
                    new ColumnDefinition(NETWORK_TOURNAMENT_ID, com.datastax.driver.core.DataType.bigint(), false, false, true),
                    new ColumnDefinition(PLAYER_ALIAS, com.datastax.driver.core.DataType.varchar(), false, false, true),
                    new ColumnDefinition(ALIAS_POSTFIX, com.datastax.driver.core.DataType.bigint(), false, false, true)
            ), NETWORK_TOURNAMENT_ID)
            .compaction(CompactionStrategy.LEVELED);

    @Override
    public TableDefinition getMainTableDefinition() {
        return PLAYER_ALIAS_TABLE;
    }

    public void persistForMultiCluster(long networkTournamentId, String alias, long postfix) {
        com.datastax.driver.core.Statement query = getInsertQuery(getTtl())
                .value(NETWORK_TOURNAMENT_ID, networkTournamentId)
                .value(PLAYER_ALIAS, alias)
                .value(ALIAS_POSTFIX, postfix);
        execute(query, "persistForMultiCluster");
    }

    public void persistForSingleCluster(long networkTournamentId, String alias) {
        com.datastax.driver.core.Statement query = getInsertQuery(getTtl())
                .value(NETWORK_TOURNAMENT_ID, networkTournamentId)
                .value(PLAYER_ALIAS, alias)
                .value(ALIAS_POSTFIX, EMPTY_ALIAS_POSTFIX);
        execute(query, "persistForSingleCluster");
    }

    public boolean isExistsForSingleCluster(long networkTournamentId, String alias) {
        com.datastax.driver.core.Statement selectQuery = getSelectAllColumnsQuery(PLAYER_ALIAS_TABLE)
                .where(eq(NETWORK_TOURNAMENT_ID, networkTournamentId))
                .and(eq(PLAYER_ALIAS, alias))
                .limit(1);
        com.datastax.driver.core.ResultSet allAliases = execute(selectQuery, "isExists");
        return allAliases.one() != null;
    }

    public Long getAliasPostfix(long networkTournamentId, String alias) {
        com.datastax.driver.core.Statement selectQuery = getSelectAllColumnsQuery(PLAYER_ALIAS_TABLE)
                .where(eq(NETWORK_TOURNAMENT_ID, networkTournamentId))
                .and(eq(PLAYER_ALIAS, alias));
        List<com.datastax.driver.core.Row> aliases = execute(selectQuery, "getAlias").all();
        Optional<com.datastax.driver.core.Row> row = aliases.stream()
                .max(Comparator.comparingLong(r -> r.getLong(ALIAS_POSTFIX)));
        if (row.isPresent()) {
            return row.get().getLong(ALIAS_POSTFIX);
        }
        return null;
    }

    @Override
    public Logger getLog() {
        return LOG;
    }
}
