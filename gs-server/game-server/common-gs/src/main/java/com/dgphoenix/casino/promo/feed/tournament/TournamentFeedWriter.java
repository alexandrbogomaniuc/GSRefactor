package com.dgphoenix.casino.promo.feed.tournament;

import com.dgphoenix.casino.cassandra.CassandraPersistenceManager;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.promo.*;
import com.abs.casino.common.promo.feed.tournament.*;
import com.dgphoenix.casino.common.upload.JSchUploadClient;
import com.dgphoenix.casino.promo.feed.AbstractFeedWriter;
import com.abs.casino.promo.persisters.CassandraTournamentRankPersister;
import com.google.common.collect.Multimap;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by vladislav on 12/27/16.
 */
public class TournamentFeedWriter<T extends TournamentPromoTemplate<?>> extends AbstractFeedWriter<T, TournamentFeed> {
    private static final Logger LOG = LogManager.getLogger(TournamentFeedWriter.class);
    private static final String FEED_WRITE_TASK_NAME = "tournamentWriteTask";
    private static final CSVFormat tournamentCSVFormat = CSVFormat.newFormat(';')
            .withHeader("playerId", "nickName", "rank", "score")
            .withRecordSeparator("\n");

    private final CassandraTournamentRankPersister tournamentRankPersister;
    private final Map<TournamentObjective, IRecordProducer> feedRecordProducersByObjective =
            new EnumMap<>(TournamentObjective.class);

    public TournamentFeedWriter(String rootDirectory, CassandraPersistenceManager persistenceManager,
                                JSchUploadClient jschClient) {
        super(rootDirectory, persistenceManager, jschClient);
        this.tournamentRankPersister = persistenceManager.getPersister(CassandraTournamentRankPersister.class);
        xStream.processAnnotations(TournamentFeed.class);
        xStream.processAnnotations(RoundCountRecord.class);
        xStream.processAnnotations(ScoreRecord.class);
        xStream.processAnnotations(DecimalScoreRecord.class);

        feedRecordProducersByObjective.put(TournamentObjective.MAX_PERFORMANCE,
                (place, rank) -> new ScoreRecord(place, rank.getExtAccountId(), rank.getNickName(), rank.getScore()));
        feedRecordProducersByObjective.put(TournamentObjective.HIGHEST_WIN, (place, rank) -> {
            BigDecimal longScore = BigDecimal.valueOf(rank.getScore());
            BigDecimal score = longScore.divide(TournamentObjective.BD_HIGHEST_WIN_MULTIPLIER, 2,
                    RoundingMode.DOWN);
            return new DecimalScoreRecord(place, rank.getExtAccountId(), rank.getNickName(),
                    String.format("%.02f", score.doubleValue()));
        });
        feedRecordProducersByObjective.put(TournamentObjective.CURRENT_TOURNAMENT_BALANCE, (place, rank) ->
                new ScoreRecord(place, rank.getExtAccountId(), rank.getNickName(), rank.getScore()));
        feedRecordProducersByObjective.put(TournamentObjective.TOURNAMENT_MAX_BET_SUM, (place, rank) ->
                new ScoreRecord(place, rank.getExtAccountId(), rank.getNickName(), rank.getBetSum()));
    }

    protected TournamentFeed collectFeed(IPromoCampaign promoCampaign) {
        LOG.debug("Start creating feed for campaign with id = {}", promoCampaign.getId());
        TournamentPromoTemplate<?> template = (TournamentPromoTemplate<?>) promoCampaign.getTemplate();

        TournamentObjective objective = template.getObjective();
        IRecordProducer recordProducer = feedRecordProducersByObjective.get(objective);
        checkNotNull(recordProducer, "Objective %s is not supported by feed writer", objective);

        TournamentRankQualifier rankQualifier = template.getRankQualifier();
        Multimap<String, TournamentMemberRank> membersRanks = tournamentRankPersister
                .getByCampaign(promoCampaign.getId(), rankQualifier);
        TournamentFeed tournamentFeed = getDefaultFeed(membersRanks, recordProducer);
        LOG.debug("End creating feed. Players count = {}", tournamentFeed.getSize());
        return tournamentFeed;
    }

    private TournamentFeed getDefaultFeed(Multimap<String, TournamentMemberRank> membersRanks, IRecordProducer recordProducer) {
        TournamentFeed tournamentFeed = new TournamentFeed();
        for (Map.Entry<String, Collection<TournamentMemberRank>> rankEntry : membersRanks.asMap().entrySet()) {
            String place = rankEntry.getKey();
            for (TournamentMemberRank memberRank : rankEntry.getValue()) {
                tournamentFeed.addRecord(recordProducer.produce(place, memberRank));
            }
        }
        return tournamentFeed;
    }

    @Override
    public ByteArrayOutputStream prepareCsvFeed(TournamentFeed tournamentFeed) throws CommonException, IOException {
        StringBuilder out = new StringBuilder();
        try (CSVPrinter csvPrinter = new CSVPrinter(out, tournamentCSVFormat)) {
            for (ITournamentFeedRecord feedRecord : tournamentFeed.getRecords()) {
                appendCSVData(csvPrinter, feedRecord);
            }
        }
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byteArrayOutputStream.write(out.toString().getBytes(StandardCharsets.UTF_8));
        return byteArrayOutputStream;
    }

    private void appendCSVData(CSVPrinter printer, ITournamentFeedRecord feedRecord) throws IOException {
        printer.print(feedRecord.getPlayerId());
        printer.print(feedRecord.getNickName());
        printer.print(feedRecord.getRank());
        printer.print(feedRecord.getScoreAsString());
        printer.println();
    }

    @Override
    protected String getFeedName() {
        return TOURNAMENT_FEED_FILE_NAME;
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }

    @Override
    protected String getTaskKey(long campaignId) {
        return FEED_WRITE_TASK_NAME + campaignId;
    }
}
