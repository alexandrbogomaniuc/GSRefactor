package com.abs.casino.promo.persisters;

import com.dgphoenix.casino.cassandra.persist.engine.AbstractCassandraPersister;
import com.dgphoenix.casino.cassandra.persist.engine.ColumnDefinition;
import com.dgphoenix.casino.cassandra.persist.engine.TableDefinition;
import com.dgphoenix.casino.cassandra.persist.engine.configuration.CompactionStrategy;
import com.dgphoenix.casino.common.promo.TournamentObjective;
import com.abs.casino.common.promo.feed.tournament.*;
import com.dgphoenix.casino.common.util.string.StringUtils;
import com.abs.casino.promo.masker.SummaryFeedNameMasker;
import com.abs.casino.promo.masker.TournamentSummaryFeedNameMasker;
import com.thoughtworks.xstream.XStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

/**
 * User: flsh
 * Date: 31.03.17.
 */
public class CassandraSummaryTournamentPromoFeedPersister extends AbstractCassandraPersister<Long, String> {
    private static final Logger LOG = LogManager.getLogger(CassandraSummaryTournamentPromoFeedPersister.class);

    private static final String SUMMARY_PROMO_FEED_CF = "TournamentPromoSumFeedCF";
    private static final String ID = "ID";
    private static final String TOURNAMENT_ID = "TOURNAMENT_ID";
    private static final String FEED_URL = "FURL";
    private static final String BANK_NAME = "BNAME";
    private static final String START_DATE = "START_DATE";
    private static final String END_DATE = "END_DATE";
    private static final String CHECKSUM = "PCHECKSUM";
    private static final String TOURNAMENT_TYPE = "TTYPE";
    private static final String UPDATE_TIME = "UPDATE_TIME";
    private static final String FEED_BODY = "FEED_BODY";
    private static final String MASK_NAME = "MASK_NAME";
    private static final EnumMap<TournamentObjective, XStream> xStreams = new EnumMap<>(TournamentObjective.class);
    private static final EnumMap<TournamentObjective, Class> recordsByObjectives = new EnumMap<>(TournamentObjective.class);

    static {
        recordsByObjectives.put(TournamentObjective.HIGHEST_WIN, DecimalScoreRecord.class);
        recordsByObjectives.put(TournamentObjective.MAX_PERFORMANCE, ScoreRecord.class);
        recordsByObjectives.put(TournamentObjective.CURRENT_TOURNAMENT_BALANCE, MaxBalanceRecord.class);
        recordsByObjectives.put(TournamentObjective.TOURNAMENT_MAX_BET_SUM, MaxBalanceRecord.class);

        for (Map.Entry<TournamentObjective, Class> entry : recordsByObjectives.entrySet()) {
            XStream xStream = new XStream();
            XStream.setupDefaultSecurity(xStream);
            xStream.allowTypesByWildcard(new String[]{"com.dgphoenix.casino.**"});
            xStream.processAnnotations(TournamentFeed.class);
            xStream.processAnnotations(entry.getValue());

            xStreams.put(entry.getKey(), xStream);
        }
    }

    private static final TableDefinition SUMMARY_PROMO_FEED_TABLE = new TableDefinition(
            SUMMARY_PROMO_FEED_CF,
            Arrays.asList(
                    new ColumnDefinition(ID, com.datastax.driver.core.DataType.bigint(), false, false, true),
                    new ColumnDefinition(FEED_URL, com.datastax.driver.core.DataType.text(), false, false, true),
                    new ColumnDefinition(START_DATE, com.datastax.driver.core.DataType.bigint(), true, false, false),
                    new ColumnDefinition(END_DATE, com.datastax.driver.core.DataType.bigint(), true, false, false),
                    new ColumnDefinition(BANK_NAME, com.datastax.driver.core.DataType.text(), false, false, false),
                    new ColumnDefinition(CHECKSUM, com.datastax.driver.core.DataType.text(), false, false, false),
                    new ColumnDefinition(TOURNAMENT_TYPE, com.datastax.driver.core.DataType.text(), false, false, false),
                    new ColumnDefinition(UPDATE_TIME, com.datastax.driver.core.DataType.bigint(), false, false, false),
                    new ColumnDefinition(FEED_BODY, com.datastax.driver.core.DataType.text()),
                    new ColumnDefinition(MASK_NAME, com.datastax.driver.core.DataType.cboolean(), false, false, false),
                    new ColumnDefinition(TOURNAMENT_ID, com.datastax.driver.core.DataType.bigint(), false, true, false)
            ), ID)
            .compaction(CompactionStrategy.LEVELED);

    @Override
    public TableDefinition getMainTableDefinition() {
        return SUMMARY_PROMO_FEED_TABLE;
    }

    @Override
    public Logger getLog() {
        return LOG;
    }

    public void create(long id, String feedUrl, String bankName, long startDate, long endDate,
                       TournamentObjective type, long tournamentId) {
        if (recordsByObjectives.get(type) == null) {
            throwUnsupportedTournamentObjective(type);
        }
        execute(getInsertQuery()
                .value(ID, id)
                .value(FEED_URL, feedUrl)
                .value(BANK_NAME, bankName)
                .value(START_DATE, startDate)
                .value(TOURNAMENT_TYPE, type.name())
                .value(END_DATE, endDate)
                .value(TOURNAMENT_ID, tournamentId), "persist");
    }

    public void create(long id, String feedUrl, String bankName, long startDate, long endDate,
                       TournamentObjective type, boolean maskName, long tournamentId) {
        if (recordsByObjectives.get(type) == null) {
            throwUnsupportedTournamentObjective(type);
        }
        execute(getInsertQuery()
                .value(ID, id)
                .value(FEED_URL, feedUrl)
                .value(BANK_NAME, bankName)
                .value(START_DATE, startDate)
                .value(TOURNAMENT_TYPE, type.name())
                .value(END_DATE, endDate)
                .value(MASK_NAME, maskName), "persist");
    }

    private static void throwUnsupportedTournamentObjective(TournamentObjective type) {
        throw new RuntimeException("Unsupported tournament objective: " + type);
    }

    public void update(long id, String feedUrl, String checkSum, String feedBody) {
        execute(getInsertQuery()
                .value(ID, id)
                .value(FEED_URL, feedUrl)
                .value(CHECKSUM, checkSum)
                .value(UPDATE_TIME, System.currentTimeMillis())
                .value(FEED_BODY, feedBody), "persist");
    }

    public List<SummaryTournamentFeed> getAllFeeds() {
        List<SummaryTournamentFeed> feeds = new ArrayList<>();
        com.datastax.driver.core.ResultSet result = execute(getSelectColumnsQuery(ID, FEED_URL, BANK_NAME, START_DATE, END_DATE, CHECKSUM, TOURNAMENT_TYPE,
                MASK_NAME), "getAllFeeds");
        for (com.datastax.driver.core.Row row : result) {
            feeds.add(convert(row));
        }
        return feeds;
    }

    public List<SummaryTournamentFeed> getFeeds(long id) {
        List<SummaryTournamentFeed> feeds = new ArrayList<>();
        com.datastax.driver.core.ResultSet result = execute(getSelectColumnsQuery(ID, FEED_URL, BANK_NAME, START_DATE, END_DATE, CHECKSUM, TOURNAMENT_TYPE,
                MASK_NAME)
                .where(eq(ID, id)), "getFeeds");
        for (com.datastax.driver.core.Row row : result) {
            feeds.add(convert(row));
        }
        return feeds;
    }

    private static XStream getXstream(TournamentObjective objective) {
        XStream xStream = xStreams.get(objective);
        if (xStream == null) {
            throwUnsupportedTournamentObjective(objective);
        }
        return xStream;
    }

    //key: url
    public Map<String, List<SummaryTournamentFeedEntry>> getAllFeedEntriesForTournament(long tournamentId) {
        return getEntriesForStatement(getSelectColumnsQuery(FEED_URL, FEED_BODY, BANK_NAME, TOURNAMENT_TYPE, MASK_NAME)
                .where(eq(TOURNAMENT_ID, tournamentId)), tournamentId);
    }

    //key: url
    public Map<String, List<SummaryTournamentFeedEntry>> getAllFeedEntries(long id) {
        return getEntriesForStatement(getSelectColumnsQuery(FEED_URL, FEED_BODY, BANK_NAME, TOURNAMENT_TYPE, MASK_NAME)
                .where(eq(ID, id)), id);
    }

    //key: url
    public Map<String, List<SummaryTournamentFeedEntry>> getEntriesForStatement(com.datastax.driver.core.Statement select, long id) {
        com.datastax.driver.core.ResultSet resultSet = execute(select, "getAllFeedEntries");
        Map<String, List<SummaryTournamentFeedEntry>> result = new HashMap<>(resultSet.getAvailableWithoutFetching());
        for (com.datastax.driver.core.Row row : resultSet) {
            String feedBody = row.getString(FEED_BODY);
            String url = row.getString(FEED_URL);
            String bankName = row.getString(BANK_NAME);
            String tournamentType = row.getString(TOURNAMENT_TYPE);
            boolean maskName = row.getBool(MASK_NAME);
            if (!StringUtils.isTrimmedEmpty(feedBody)) {
                TournamentObjective objective = TournamentObjective.valueOf(tournamentType);
                XStream xStream = getXstream(objective);
                TournamentFeed feed = (TournamentFeed) xStream.fromXML(feedBody);
                if (feed.getRecords() == null || feed.getRecords().isEmpty()) {
                    LOG.warn("Empty feed: id={}, url={}, bankName={}", id, url, bankName);
                    continue;
                }
                List<ITournamentFeedRecord> feedEntries = feed.getRecords();
                List<SummaryTournamentFeedEntry> stfEntries = new ArrayList<>(feedEntries.size());
                for (ITournamentFeedRecord feedEntry : feedEntries) {
                    stfEntries.add(new SummaryTournamentFeedEntry(bankName, feedEntry));
                }
                if (maskName) {
                    SummaryFeedNameMasker<SummaryTournamentFeedEntry> masker =
                            new TournamentSummaryFeedNameMasker(stfEntries);
                    stfEntries = masker.getFeedEntriesWithMaskedNames();
                }
                result.put(url, stfEntries);
            }
        }
        return result;
    }

    public List<SummaryTournamentFeedEntry> getFeedEntries(long id, String feedUrl) {
        com.datastax.driver.core.Row row = execute(getSelectColumnsQuery(FEED_BODY, BANK_NAME, TOURNAMENT_TYPE)
                .where(eq(ID, id))
                .and(eq(FEED_URL, feedUrl)), "getFeedEntries").one();
        List<SummaryTournamentFeedEntry> feedEntries = new ArrayList<>();
        if (row != null) {
            String feedBody = row.getString(FEED_BODY);
            String bankName = row.getString(BANK_NAME);
            String tournamentType = row.getString(TOURNAMENT_TYPE);
            if (!StringUtils.isTrimmedEmpty(feedBody)) {
                TournamentObjective objective = TournamentObjective.valueOf(tournamentType);
                XStream xStream = getXstream(objective);
                TournamentFeed feed = (TournamentFeed) xStream.fromXML(feedBody);
                for (ITournamentFeedRecord feedEntry : feed.getRecords()) {
                    feedEntries.add(new SummaryTournamentFeedEntry(bankName, feedEntry));
                }
            }
        }
        return feedEntries;
    }

    private SummaryTournamentFeed convert(com.datastax.driver.core.Row row) {
        String tournamentType = row.getString(TOURNAMENT_TYPE);
        TournamentObjective type = TournamentObjective.valueOf(tournamentType);
        return new SummaryTournamentFeed(row.getLong(ID), row.getString(FEED_URL), row.getString(BANK_NAME),
                row.getLong(START_DATE), row.getLong(END_DATE), row.getString(CHECKSUM), type, row.getBool(MASK_NAME));
    }
}
