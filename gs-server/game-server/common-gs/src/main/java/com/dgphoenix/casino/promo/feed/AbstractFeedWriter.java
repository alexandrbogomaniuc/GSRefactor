package com.dgphoenix.casino.promo.feed;

import com.dgphoenix.casino.cassandra.CassandraPersistenceManager;
import com.dgphoenix.casino.cassandra.persist.CassandraPeriodicTasksPersister;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.promo.IPromoCampaign;
import com.dgphoenix.casino.common.promo.IPromoTemplate;
import com.dgphoenix.casino.common.promo.Status;
import com.dgphoenix.casino.common.promo.feed.IPromoFeedWriter;
import com.dgphoenix.casino.common.upload.JSchUploadClient;
import com.dgphoenix.casino.common.web.statistics.StatisticsManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import com.thoughtworks.xstream.XStream;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

/**
 * User: flsh
 * Date: 11.04.17.
 */
public abstract class AbstractFeedWriter<T extends IPromoTemplate<?,?>, FEED> implements IPromoFeedWriter {
    protected static final long UPDATE_INTERVAL = TimeUnit.MINUTES.toMillis(10);
    public static final String XML_SCHEMA = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n";
    public static final String TOURNAMENT_FEED_FILE_NAME = "leaderboard";
    public static final String XML_EXTENSION = ".xml";
    public static final String CSV_EXTENSION = ".csv";
    public static final String JSON_EXTENSION = ".json";

    protected final XStream xStream = new XStream();
    protected final String rootDirectory;
    protected final ConcurrentMap<Long, Long> lastWriteTimeByCampaign = new ConcurrentHashMap<>();
    protected final CassandraPeriodicTasksPersister periodicTasksPersister;
    protected final JSchUploadClient jschClient;
    protected Gson jsonHandler = new GsonBuilder().setPrettyPrinting().create();

    protected AbstractFeedWriter(String rootDirectory, CassandraPersistenceManager persistenceManager, JSchUploadClient jschClient) {
        this.rootDirectory = rootDirectory;
        this.periodicTasksPersister = persistenceManager.getPersister(CassandraPeriodicTasksPersister.class);
        this.jschClient = jschClient;
    }

    @Override
    public void write(IPromoCampaign promoCampaign) throws CommonException {
        try {
            writeFeed(promoCampaign);
        } catch (Exception e) {
            throw new CommonException("Error during writing feed", e);
        }
    }

    protected void writeFeed(IPromoCampaign promoCampaign) throws CommonException, IOException, SftpException, JSchException {
        long startWriteTime = System.currentTimeMillis();
        getLogger().debug("Feed upload started, campaignId={}", promoCampaign.getId());
        uploadPromoFeed(promoCampaign.getId(), collectFeed(promoCampaign));
        getLogger().debug("Feed upload finished, campaignId={}", promoCampaign.getId());

        String taskKey = getTaskKey(promoCampaign.getId());
        periodicTasksPersister.saveLastExecutionTime(taskKey, startWriteTime);
        lastWriteTimeByCampaign.put(promoCampaign.getId(), startWriteTime);
        StatisticsManager.getInstance().updateRequestStatistics(getClass().getSimpleName() + " writeFeed",
                System.currentTimeMillis() - startWriteTime);
    }

    protected abstract FEED collectFeed(IPromoCampaign tournament) throws CommonException;

    @Override
    public boolean isReadyToWrite(IPromoCampaign promoCampaign) {
        getLogger().debug("Check if it is needed to write feed for campaign with id = {}", promoCampaign.getId());
        if (promoCampaign.getStatus() == Status.QUALIFICATION) {
            getLogger().debug("QUALIFICATION state, needed");
            return true;
        }
        Date endDate = promoCampaign.getActionPeriod().getEndDate();
        if (promoCampaign.getStatus() == Status.FINISHED && endDate != null) {
            long now = System.currentTimeMillis();
            if (endDate.getTime() < now && endDate.getTime() + TimeUnit.HOURS.toMillis(1) > now) {
                getLogger().debug("FINISHED state and 1 hour period, needed, endDate={}", endDate);
                return true;
            }
        }

        if (promoCampaign.getStatus() == Status.STARTED) {
            Long lastWriteTime = lastWriteTimeByCampaign.get(promoCampaign.getId());
            if (isTimeToWrite(lastWriteTime)) {
                String taskKey = getTaskKey(promoCampaign.getId());
                lastWriteTime = periodicTasksPersister.getLastExecutionTime(taskKey);
                if (lastWriteTime != null) {
                    lastWriteTimeByCampaign.put(promoCampaign.getId(), lastWriteTime);
                }
                boolean timeToWrite = isTimeToWrite(lastWriteTime);
                if (!timeToWrite) {
                    getLogger().debug("Too early to write, lastWriteTime: {}", new Date(lastWriteTime));
                }
                return timeToWrite;
            } else {
                getLogger().debug("Too early to write, lastWriteTime at least: {}", new Date(lastWriteTime));
            }
        } else {
            getLogger().debug("Not STARTED state, no need");
        }

        return false;
    }

    protected abstract ByteArrayOutputStream prepareCsvFeed(FEED feed) throws CommonException, IOException;

    protected ByteArrayOutputStream prepareJsonFeed(FEED feed) throws IOException {
        String jsonString = jsonHandler.toJson(feed);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        outputStream.write(jsonString.getBytes(StandardCharsets.UTF_8));
        return outputStream;
    }

    protected ByteArrayOutputStream prepareXmlFeed(FEED feed) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        outputStream.write(XML_SCHEMA.getBytes(StandardCharsets.UTF_8));
        xStream.toXML(feed, outputStream);
        return outputStream;
    }

    protected void uploadPromoFeed(long campaignId, FEED feed) throws CommonException, IOException, JSchException, SftpException {
        try (ByteArrayOutputStream xmlFeedOutput = prepareXmlFeed(feed)) {
            uploadFeed(xmlFeedOutput, getRemoteXmlFeedsPath(campaignId));
        }
        try (ByteArrayOutputStream csvFeedOutput = prepareCsvFeed(feed)) {
            uploadFeed(csvFeedOutput, getRemoteCsvFeedsPath(campaignId));
        }
        try (ByteArrayOutputStream jsonFeedOutput = prepareJsonFeed(feed)) {
            uploadFeed(jsonFeedOutput, getRemoteJsonFeedsPath(campaignId));
        }
    }

    protected void uploadFeed(ByteArrayOutputStream feedOutputStream, String feedPath)
            throws SftpException, JSchException, IOException {
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(feedOutputStream.toByteArray())) {
            jschClient.sendFile(byteArrayInputStream, feedPath);
        }
    }

    private String getBaseRemoteFeedPath(long campaignId) {
        return rootDirectory + "/" + campaignId + "/" + getFeedName();
    }

    private String getRemoteXmlFeedsPath(long campaignId) {
        return getBaseRemoteFeedPath(campaignId) + XML_EXTENSION;
    }

    private String getRemoteCsvFeedsPath(long campaignId) {
        return getBaseRemoteFeedPath(campaignId) + CSV_EXTENSION;
    }

    private String getRemoteJsonFeedsPath(long campaignId) {
        return getBaseRemoteFeedPath(campaignId) + JSON_EXTENSION;
    }

    protected abstract String getFeedName();

    protected boolean isTimeToWrite(Long lastWriteTime) {
        return lastWriteTime == null || System.currentTimeMillis() - lastWriteTime >= UPDATE_INTERVAL;
    }

    protected abstract Logger getLogger();

    protected abstract String getTaskKey(long campaignId);
}
