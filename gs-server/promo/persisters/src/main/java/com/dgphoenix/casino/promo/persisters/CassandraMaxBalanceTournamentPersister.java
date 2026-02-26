package com.dgphoenix.casino.promo.persisters;

import com.dgphoenix.casino.cassandra.persist.engine.AbstractCassandraPersister;
import com.dgphoenix.casino.cassandra.persist.engine.ColumnDefinition;
import com.dgphoenix.casino.cassandra.persist.engine.TableDefinition;
import com.dgphoenix.casino.cassandra.persist.engine.configuration.CompactionStrategy;
import com.dgphoenix.casino.common.promo.MaxBalanceTournamentPlayerDetails;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CassandraMaxBalanceTournamentPersister extends AbstractCassandraPersister<Long, String> {
    private static final Logger LOG = LogManager.getLogger(CassandraMaxBalanceTournamentPersister.class);

    private static final String MAX_BALANCE_DETAILS_CF = "PromoTMaxBalanceCF";
    private static final String CAMPAIGN_ID_FIELD = "cid";
    private static final String ACCOUNT_ID_FIELD = "aid";
    private static final TableDefinition MAX_BALANCE_DETAILS_TABLE = new TableDefinition(MAX_BALANCE_DETAILS_CF,
            Arrays.asList(
                    new ColumnDefinition(CAMPAIGN_ID_FIELD, com.datastax.driver.core.DataType.bigint(), false, false, true),
                    new ColumnDefinition(ACCOUNT_ID_FIELD, com.datastax.driver.core.DataType.bigint(), false, false, true),
                    new ColumnDefinition(SERIALIZED_COLUMN_NAME, com.datastax.driver.core.DataType.blob()),
                    new ColumnDefinition(JSON_COLUMN_NAME, com.datastax.driver.core.DataType.text())
            ), CAMPAIGN_ID_FIELD)
            .compaction(CompactionStrategy.LEVELED);

    @Override
    public TableDefinition getMainTableDefinition() {
        return MAX_BALANCE_DETAILS_TABLE;
    }

    public void persist(MaxBalanceTournamentPlayerDetails details) {
        ByteBuffer byteBuffer = getMainTableDefinition().serializeToBytes(details);
        String json = getMainTableDefinition().serializeToJson(details);
        try {
            execute(getInsertQuery(getTtl())
                    .value(CAMPAIGN_ID_FIELD, details.getCampaignId())
                    .value(ACCOUNT_ID_FIELD, details.getAccountId())
                    .value(SERIALIZED_COLUMN_NAME, byteBuffer)
                    .value(JSON_COLUMN_NAME, json), "persist");
        } finally {
            releaseBuffer(byteBuffer);
        }
    }

    public List<MaxBalanceTournamentPlayerDetails> getByTournament(long tournamentId) {
        com.datastax.driver.core.ResultSet resultSet = execute(getSelectColumnsQuery(SERIALIZED_COLUMN_NAME, JSON_COLUMN_NAME)
                .where(eq(CAMPAIGN_ID_FIELD, tournamentId)), "getByTournament");
        List<MaxBalanceTournamentPlayerDetails> result = new ArrayList<>();
        for (com.datastax.driver.core.Row row : resultSet) {
            String json = row.getString(JSON_COLUMN_NAME);
            ByteBuffer buffer = row.getBytes(SERIALIZED_COLUMN_NAME);
            MaxBalanceTournamentPlayerDetails rank =
                    getMainTableDefinition().deserializeFromJson(json, MaxBalanceTournamentPlayerDetails.class);

            if (rank == null) {
                rank = getMainTableDefinition().deserializeFrom(buffer, MaxBalanceTournamentPlayerDetails.class);
            }

            result.add(rank);
        }
        LOG.debug("getByTournament: tournamentId={}, size={}", tournamentId, result.size());
        return result;
    }

    public MaxBalanceTournamentPlayerDetails getForAccount(long accountId, long campaignId) {
        com.datastax.driver.core.ResultSet resultSet = execute(getSelectColumnsQuery(SERIALIZED_COLUMN_NAME, JSON_COLUMN_NAME)
                .where(eq(ACCOUNT_ID_FIELD, accountId))
                .and(eq(CAMPAIGN_ID_FIELD, campaignId))
                .limit(1), "getForAccount");
        com.datastax.driver.core.Row row = resultSet.one();
        MaxBalanceTournamentPlayerDetails result = null;
        if (row != null) {
            String json = row.getString(JSON_COLUMN_NAME);
            ByteBuffer buffer = row.getBytes(SERIALIZED_COLUMN_NAME);
            result = getMainTableDefinition().deserializeFromJson(json, MaxBalanceTournamentPlayerDetails.class);
            if (result == null) {
                result = getMainTableDefinition().deserializeFrom(buffer, MaxBalanceTournamentPlayerDetails.class);
            }
        }
        return result;
    }

    @Override
    public Logger getLog() {
        return LOG;
    }
}
