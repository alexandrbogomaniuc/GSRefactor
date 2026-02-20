package com.dgphoenix.casino.promo.persisters;

import com.datastax.driver.core.*;
import com.datastax.driver.core.querybuilder.Insert;
import com.datastax.driver.core.querybuilder.Select;
import com.dgphoenix.casino.cassandra.persist.engine.AbstractCassandraPersister;
import com.dgphoenix.casino.cassandra.persist.engine.ColumnDefinition;
import com.dgphoenix.casino.cassandra.persist.engine.TableDefinition;
import com.dgphoenix.casino.cassandra.persist.engine.configuration.CompactionStrategy;
import com.dgphoenix.casino.common.promo.TournamentMemberRank;
import com.dgphoenix.casino.common.promo.TournamentMemberRanks;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.ByteBuffer;
import java.util.*;

/**
 * User: flsh
 * Date: 12.01.17.
 */
public class CassandraTournamentRankPersister extends AbstractCassandraPersister<Long, String> {
    private static final Logger LOG = LogManager.getLogger(CassandraTournamentRankPersister.class);

    private static final String TOURNAMENT_RANK_CF = "PromoTRankCF";
    private static final String CAMPAIGN_ID_FIELD = "GmpId";
    private static final String ACCOUNT_ID_FIELD = "AccountId";

    private static final TableDefinition TOURNAMENT_RANK_TABLE = new TableDefinition(TOURNAMENT_RANK_CF,
            Arrays.asList(
                    new ColumnDefinition(CAMPAIGN_ID_FIELD, DataType.bigint(), false, false, true),
                    new ColumnDefinition(ACCOUNT_ID_FIELD, DataType.bigint(), false, false, true),
                    new ColumnDefinition(SERIALIZED_COLUMN_NAME, DataType.blob()),
                    new ColumnDefinition(JSON_COLUMN_NAME, DataType.text())
            ), CAMPAIGN_ID_FIELD)
            .compaction(CompactionStrategy.LEVELED);

    @Override
    public TableDefinition getMainTableDefinition() {
        return TOURNAMENT_RANK_TABLE;
    }

    public void prepareToPersist(Map<Session, List<Statement>> statementsMap, TournamentMemberRanks ranks,
                                 List<ByteBuffer> byteBuffersCollector) {
        List<Statement> statements = getOrCreateStatements(statementsMap);
        for (TournamentMemberRank rank : ranks.getRanks()) {
            String json = getMainTableDefinition().serializeToJson(rank);
            ByteBuffer byteBuffer = getMainTableDefinition().serializeToBytes(rank);
            byteBuffersCollector.add(byteBuffer);
            Insert query = getInsertQuery(getTtl());
            query.value(CAMPAIGN_ID_FIELD, rank.getCampaignId());
            query.value(ACCOUNT_ID_FIELD, rank.getAccountId());
            query.value(SERIALIZED_COLUMN_NAME, byteBuffer);
            query.value(JSON_COLUMN_NAME, json);
            statements.add(query);
        }
    }

    public void persist(TournamentMemberRank rank) {
        ByteBuffer byteBuffer = getMainTableDefinition().serializeToBytes(rank);
        String json = getMainTableDefinition().serializeToJson(rank);
        try {
            Insert query = getInsertQuery(getTtl());
            query.value(CAMPAIGN_ID_FIELD, rank.getCampaignId());
            query.value(ACCOUNT_ID_FIELD, rank.getAccountId());
            query.value(SERIALIZED_COLUMN_NAME, byteBuffer);
            query.value(JSON_COLUMN_NAME, json);
            execute(query, "persist");
        } finally {
            releaseBuffer(byteBuffer);
        }
    }

    public Multimap<String, TournamentMemberRank> getByCampaign(long campaignId,
                                                                Comparator<TournamentMemberRank> rankQualifier) {
        List<TournamentMemberRank> result = getByCampaign(campaignId);
        if (rankQualifier == null) {
            Collections.sort(result);
        } else {
            Collections.sort(result, rankQualifier);
        }

        Multimap<String, TournamentMemberRank> ranks = LinkedListMultimap.create(result.size());
        int currentPlace = 1;
        List<TournamentMemberRank> samePlaceMembersRanks = new LinkedList<>();
        TournamentMemberRank lastProcessedRank = null;
        for (TournamentMemberRank rank : result) {
            if (lastProcessedRank != null) {
                boolean hasHigherRank = rankQualifier != null
                        ? rankQualifier.compare(rank, lastProcessedRank) > 0
                        : rank.compareTo(lastProcessedRank) > 0;
                if (hasHigherRank) {
                    putMembersRanks(ranks, currentPlace, samePlaceMembersRanks);
                    currentPlace += samePlaceMembersRanks.size();
                    samePlaceMembersRanks.clear();
                }
            }
            samePlaceMembersRanks.add(rank);
            lastProcessedRank = rank;
        }

        putMembersRanks(ranks, currentPlace, samePlaceMembersRanks);

        return ranks;
    }

    public TournamentMemberRank getForAccount(long campaignId, long accountId) {
        Select query = getSelectColumnsQuery(SERIALIZED_COLUMN_NAME, JSON_COLUMN_NAME);
        query.where().and(eq(CAMPAIGN_ID_FIELD, campaignId)).and(eq(ACCOUNT_ID_FIELD, accountId));
        ResultSet resultSet = execute(query, "getForAccount");
        Row row = resultSet.one();
        TournamentMemberRank result = null;
        if (row != null) {
            result = getMainTableDefinition()
                    .deserializeFromJson(row.getString(JSON_COLUMN_NAME), TournamentMemberRank.class);

            if (result == null) {
                ByteBuffer buffer = row.getBytes(SERIALIZED_COLUMN_NAME);
                result = getMainTableDefinition().deserializeFrom(buffer, TournamentMemberRank.class);
            }
        }
        return result;
    }

    public List<TournamentMemberRank> getByCampaign(long campaignId) {
        Select query = getSelectColumnsQuery(SERIALIZED_COLUMN_NAME, JSON_COLUMN_NAME);
        query.where().and(eq(CAMPAIGN_ID_FIELD, campaignId));
        ResultSet resultSet = execute(query, "getByCampaign");
        List<TournamentMemberRank> result = new ArrayList<>();
        for (Row row : resultSet) {
            TournamentMemberRank rank = 
                    getMainTableDefinition().deserializeFromJson(row.getString(JSON_COLUMN_NAME),
                            TournamentMemberRank.class);
            if (rank == null) {
                rank = getMainTableDefinition().deserializeFrom(row.getBytes(SERIALIZED_COLUMN_NAME),
                                TournamentMemberRank.class);
            }
            if (rank != null) {
                result.add(rank);
            }
        }
        return result;
    }

    private void putMembersRanks(Multimap<String, TournamentMemberRank> ranks, int place,
                                 List<TournamentMemberRank> samePlaceMembersRanks) {
        String finalPlace;
        int samePlaceMembersCount = samePlaceMembersRanks.size();
        if (samePlaceMembersCount == 1) {
            finalPlace = Integer.toString(place);
        } else {
            finalPlace = place + "-" + (place + samePlaceMembersCount - 1);
        }
        ranks.putAll(finalPlace, samePlaceMembersRanks);
    }

    @Override
    public Logger getLog() {
        return LOG;
    }
}
