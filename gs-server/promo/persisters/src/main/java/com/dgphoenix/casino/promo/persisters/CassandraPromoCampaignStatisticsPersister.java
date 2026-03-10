package com.abs.casino.promo.persisters;

import com.abs.casino.cassandra.persist.engine.AbstractCassandraPersister;
import com.abs.casino.cassandra.persist.engine.ColumnDefinition;
import com.abs.casino.cassandra.persist.engine.TableDefinition;
import com.abs.casino.common.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;

import static com.abs.casino.cassandra.persist.engine.CassandraDataTypes.bigint;
import static com.abs.casino.cassandra.persist.engine.CassandraDataTypes.cdouble;
import static com.abs.casino.cassandra.persist.engine.CassandraDataTypes.cint;

/**
 * User: flsh
 * Date: 21.09.2019.
 */
public class CassandraPromoCampaignStatisticsPersister extends AbstractCassandraPersister<Long, String> {
    private static final Logger LOG = LogManager.getLogger(CassandraPromoCampaignStatisticsPersister.class);
    private static final String COLUMN_FAMILY_NAME = "PromoCampaignStat";
    private static final String CAMPAIGN_ID = "CampaignId";
    private static final String GS_ID = "GsId";
    private static final String ROUNDS_COUNT = "RoundsCount";
    private static final String BET_SUM = "BetSum";

    private static final TableDefinition TABLE = new TableDefinition(COLUMN_FAMILY_NAME,
            Arrays.asList(
                    new ColumnDefinition(CAMPAIGN_ID, bigint(), false, false, true),
                    new ColumnDefinition(GS_ID, cint(), false, false, true),
                    new ColumnDefinition(ROUNDS_COUNT, cint(), false, false, false),
                    new ColumnDefinition(BET_SUM, cdouble(), false, false, false)
            ), CAMPAIGN_ID);

    public synchronized void increment(long campaignId, int gsId, int roundsCountDelta, double betSumDelta) {
        LOG.debug("increment: campaignId={}, gsId={}, roundsCount={}, betSum={}", campaignId, gsId,
                roundsCountDelta, betSumDelta);
        Pair<Integer, Double> current = getAverageBetPairForGs(campaignId, gsId);
        if (current == null) {
            execute(getInsertQuery()
                    .value(CAMPAIGN_ID, campaignId)
                    .value(GS_ID, gsId)
                    .value(ROUNDS_COUNT, roundsCountDelta)
                    .value(BET_SUM, betSumDelta), "increment: insert");
        } else {
            execute(getUpdateQuery()
                    .where(eq(CAMPAIGN_ID, campaignId))
                    .and(eq(GS_ID, gsId))
                    .with(com.datastax.driver.core.querybuilder.QueryBuilder.set(ROUNDS_COUNT, current.getKey() + roundsCountDelta))
                    .and(com.datastax.driver.core.querybuilder.QueryBuilder.set(BET_SUM, current.getValue() + betSumDelta)), "increment:update");
        }
    }

    public Pair<Integer, Double> getAverageBetPairForGs(long campaignId, int gsId) {
        com.abs.casino.cassandra.persist.engine.ResultSet resultSet = execute(getSelectColumnsQuery(ROUNDS_COUNT, BET_SUM)
                .where(eq(CAMPAIGN_ID, campaignId))
                .and(eq(GS_ID, gsId)), "getAverageBetPairForGs");
        com.abs.casino.cassandra.persist.engine.Row row = resultSet.one();
        return row == null ? null : new Pair<>(row.getInt(ROUNDS_COUNT), row.getDouble(BET_SUM));
    }

    public Pair<Integer, Double> getAverageBetPair(long campaignId) {
        com.abs.casino.cassandra.persist.engine.ResultSet resultSet = execute(getSelectColumnsQuery(ROUNDS_COUNT, BET_SUM)
                .where(eq(CAMPAIGN_ID, campaignId)), "getAverageBetPair");
        int roundsCount = 0;
        double betSum = 0;
        for (com.abs.casino.cassandra.persist.engine.Row row : resultSet) {
            roundsCount += row.getInt(ROUNDS_COUNT);
            betSum += row.getDouble(BET_SUM);
        }

        return roundsCount == 0 ? null : new Pair<>(roundsCount, betSum);
    }

    @Override
    public TableDefinition getMainTableDefinition() {
        return TABLE;
    }

    @Override
    public Logger getLog() {
        return LOG;
    }
}
