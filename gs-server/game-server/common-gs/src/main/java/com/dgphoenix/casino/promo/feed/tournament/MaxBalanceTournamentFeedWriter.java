package com.dgphoenix.casino.promo.feed.tournament;

import com.dgphoenix.casino.cassandra.CassandraPersistenceManager;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.promo.IPromoCampaign;
import com.dgphoenix.casino.common.promo.MaxBalanceTournamentPromoTemplate;
import com.dgphoenix.casino.common.promo.feed.tournament.ITournamentFeedRecord;
import com.dgphoenix.casino.common.promo.feed.tournament.MaxBalanceRecord;
import com.dgphoenix.casino.common.promo.feed.tournament.TournamentFeed;
import com.dgphoenix.casino.common.upload.JSchUploadClient;
import com.dgphoenix.casino.common.util.ApplicationContextHelper;
import com.dgphoenix.casino.promo.feed.AbstractFeedWriter;
import com.dgphoenix.casino.promo.tournaments.TournamentManager;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class MaxBalanceTournamentFeedWriter<T extends MaxBalanceTournamentPromoTemplate> extends AbstractFeedWriter<T, TournamentFeed> {
    private static final Logger LOG = LogManager.getLogger(MaxBalanceTournamentFeedWriter.class);
    private static final String FEED_WRITE_TASK_NAME = "maxBalanceTournamentWriteTask";
    private static final CSVFormat tournamentCSVFormat = CSVFormat.newFormat(';')
            .withHeader("bankId", "playerId", "nickName", "rank", "score", "prize")
            .withRecordSeparator("\n");

    public MaxBalanceTournamentFeedWriter(String rootDirectory, CassandraPersistenceManager persistenceManager,
                                          JSchUploadClient jschClient) {
        super(rootDirectory, persistenceManager, jschClient);
        xStream.processAnnotations(TournamentFeed.class);
        xStream.processAnnotations(MaxBalanceRecord.class);
    }

    protected TournamentFeed collectFeed(IPromoCampaign tournament) throws CommonException {
        TournamentManager tournamentManager = ApplicationContextHelper.getBean(TournamentManager.class);
        LOG.debug("Start creating feed for campaign with id = {}", tournament.getId());
        TournamentFeed tournamentFeed = tournamentManager.getLeaderboard(tournament);
        LOG.debug("End creating feed. Players count = {}", tournamentFeed.getSize());
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

    private void appendCSVData(CSVPrinter printer, ITournamentFeedRecord feedRecord) throws IOException, CommonException {
        MaxBalanceRecord maxBalanceRecord;
        if (feedRecord instanceof MaxBalanceRecord) {
            maxBalanceRecord = (MaxBalanceRecord) feedRecord;
        } else {
            LOG.error("Unsupported feed record type");
            throw new CommonException("Unsupported feed record type");
        }
        printer.print(maxBalanceRecord.getBankId());
        printer.print(maxBalanceRecord.getPlayerId());
        printer.print(maxBalanceRecord.getNickname());
        printer.print(maxBalanceRecord.getRank());
        printer.print(maxBalanceRecord.getScoreAsString());
        printer.print(maxBalanceRecord.getPrize());
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
