package com.dgphoenix.casino.promo.feed.tournament;

import com.dgphoenix.casino.cassandra.CassandraPersistenceManager;
import com.dgphoenix.casino.common.promo.feed.tournament.*;
import com.dgphoenix.casino.common.util.CollectionUtils;
import com.dgphoenix.casino.common.util.string.StringUtils;
import com.dgphoenix.casino.gs.status.ServersStatusWatcher;
import com.dgphoenix.casino.promo.feed.AbstractSummaryFeedWriter;
import com.dgphoenix.casino.promo.persisters.CassandraSummaryFeedTransformerPersister;
import com.dgphoenix.casino.promo.persisters.CassandraSummaryTournamentPromoFeedPersister;
import com.dgphoenix.casino.promo.persisters.ISummaryFeedTransformer;
import com.dgphoenix.casino.system.configuration.GameServerConfiguration;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * User: flsh
 * Date: 20.11.2019.
 */
public class SummaryTournamentFeedWriter extends AbstractSummaryFeedWriter {
    private static final Logger LOG = LogManager.getLogger(SummaryTournamentFeedWriter.class);

    private final CassandraSummaryTournamentPromoFeedPersister summaryFeedPersister;
    private final CassandraSummaryFeedTransformerPersister transformerPersister;

    static {
        xStream.processAnnotations(SummaryTournamentFeed.class);
        xStream.processAnnotations(SummaryTournamentFeedEntry.class);
        xStream.processAnnotations(DecimalScoreRecord.class);
        xStream.processAnnotations(RoundCountRecord.class);
        xStream.processAnnotations(ScoreRecord.class);
        xStream.processAnnotations(MaxBalanceRecord.class);
        xStream.alias("players", List.class);
        xStream.alias("players", ArrayList.class);
        xStream.registerConverter(new SummaryTournamentFeedEntryConverter());
    }

    public SummaryTournamentFeedWriter(GameServerConfiguration configuration,
                                       CassandraPersistenceManager persistenceManager) {
        super(configuration);
        this.summaryFeedPersister = persistenceManager.getPersister(CassandraSummaryTournamentPromoFeedPersister.class);
        this.transformerPersister = persistenceManager.getPersister(CassandraSummaryFeedTransformerPersister.class);
        mainService.scheduleWithFixedDelay(new PeriodicUpdater(), DEFAULT_UPDATE_PERIOD, DEFAULT_UPDATE_PERIOD,
                TimeUnit.MINUTES);
        LOG.debug("Created");
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }

    @Override
    protected void updateFeedPersister(long promoId, String url, String newMd5, String feed) {
        summaryFeedPersister.update(promoId, url, newMd5, feed);
    }

    private class PeriodicUpdater implements Runnable {

        @Override
        public void run() {
            boolean master = ServersStatusWatcher.getInstance().isMaster();
            if (!master) {
                LOG.debug("PeriodicUpdater: run, not master, exit");
                return;
            }
            LOG.debug("PeriodicUpdater: run, voted as master");
            int downloadTaskCount = 0;
            Set<Long> promoIds = new HashSet<>();
            List<SummaryTournamentFeed> allFeeds = summaryFeedPersister.getAllFeeds();
            for (SummaryTournamentFeed feed : allFeeds) {
                if (feed.getStartDate() > System.currentTimeMillis() ||
                        (feed.getEndDate() + END_PROMO_PERIOD) < System.currentTimeMillis()) {
                    LOG.debug("PeriodicUpdater: skip process, promo out of date: {}, please remove", feed);
                    continue;
                }
                executor.submit(new DownloadFeedTask(feed.getId(), feed.getFeedURL(), feed.getChecksum()));
                downloadTaskCount++;
                promoIds.add(feed.getId());
            }
            if (downloadTaskCount > 0) {
                int perThreadTasks = downloadTaskCount / DOWNLOAD_POOL_SIZE;
                if (perThreadTasks <= 0) {
                    perThreadTasks = 1;
                }
                mainService.schedule(new CollectFeedsAndUploadTask(promoIds), perThreadTasks * MAX_DOWNLOAD_TASK_TIME,
                        TimeUnit.MILLISECONDS);
            }
        }
    }

    private class CollectFeedsAndUploadTask implements Runnable {
        private final Set<Long> promoIds;

        private CollectFeedsAndUploadTask(Set<Long> promoIds) {
            this.promoIds = promoIds;
        }


        @Override
        public void run() {
            LOG.debug("CollectFeedsAndUploadTask run: promoIds={}", promoIds);
            for (Long id : promoIds) {
                FileWriter tempXMLWriter = null;
                ObjectOutputStream oos = null;
                try {
                    if (!ServersStatusWatcher.getInstance().isMaster()) {
                        LOG.debug("CollectFeedsAndUploadTask: not master, exit");
                        return;
                    }
                    List<ISummaryTournamentFeedEntry> summaryList = new ArrayList<>();
                    Map<String, List<SummaryTournamentFeedEntry>> entries = summaryFeedPersister.getAllFeedEntries(id);
                    for (Map.Entry<String, List<SummaryTournamentFeedEntry>> entry : entries.entrySet()) {
                        String url = entry.getKey();
                        LOG.debug("CollectFeedsAndUploadTask: process url={}", url);
                        List<SummaryTournamentFeedEntry> onePromoList = entry.getValue();
                        if (onePromoList.size() > 5000) {
                            LOG.debug("CollectFeedsAndUploadTask: list too long= {}, truncate", onePromoList.size());
                            onePromoList = onePromoList.subList(0, 5000);
                        }
                        summaryList.addAll(onePromoList);
                    }
                    sortEntries(summaryList);

                    ISummaryFeedTransformer transformer = transformerPersister.get(id);
                    if (transformer != null) {
                        summaryList = transformer.transform(id, summaryList);
                    }

                    if (summaryList != null) {
                        String promoFileName = "summaryPromo_" + id;
                        String remoteFeedPath = promoRootPath + "/" + promoFileName + ".json";
                        File tempFile = null;
                        boolean deleted = false;
                        try {
                            tempFile = File.createTempFile(promoFileName, ".json");
                            tempXMLWriter = new FileWriter(tempFile);
                            oos = xStream.createObjectOutputStream(tempXMLWriter, TournamentFeed.class.
                                    getAnnotation(XStreamAlias.class).value());
                            oos.writeObject(summaryList);
                            tempXMLWriter.flush();
                            oos.close();
                            oos = null;

                            uploadTempFile(tempFile.getPath(), remoteFeedPath);
                        } finally {
                            if (tempFile != null) {
                                deleted = tempFile.delete();
                            }
                        }
                        LOG.debug("Upload file success, tempFile={}, remoteFeedPath={}, deleted={}",
                                tempFile.getPath(), remoteFeedPath, deleted);
                    }
                } catch (Exception e) {
                    LOG.error("Cannot process feed, id=" + id, e);
                } finally {
                    if (oos != null) {
                        try {
                            oos.close();
                        } catch (IOException e) {
                            //nop
                        }
                    }
                    if (tempXMLWriter != null) {
                        try {
                            tempXMLWriter.close();
                        } catch (IOException e) {
                            //nop
                        }
                    }
                }
            }
        }

        private void sortEntries(List<ISummaryTournamentFeedEntry> summaryList) {
            if (!CollectionUtils.isEmpty(summaryList) && summaryList.get(0).getRecord() instanceof MaxBalanceRecord) {
                summaryList.sort((o1, o2) -> {
                    double score1 = Double.parseDouble(o1.getRecord().getScoreAsString());
                    double score2 = Double.parseDouble(o2.getRecord().getScoreAsString());
                    return Double.compare(score2, score1);
                });
            } else {
                summaryList.sort(Collections.reverseOrder());
            }
        }
    }

    private static class SummaryTournamentFeedEntryConverter implements Converter {
        @Override
        public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
            SummaryTournamentFeedEntry entry = (SummaryTournamentFeedEntry) source;
            if (!StringUtils.isTrimmedEmpty(entry.getBankName())) {
                writer.startNode("bankName");
                context.convertAnother(entry.getBankName());
                writer.endNode();
            }

            writer.startNode("nickName");
            context.convertAnother(entry.getNickName());
            writer.endNode();

            writer.startNode("score");
            context.convertAnother(entry.getScore());
            writer.endNode();
        }

        @Override
        public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
            String bankName = "";
            String nickName = "";
            String score = "";
            while (reader.hasMoreChildren()) {
                reader.moveDown();
                switch (reader.getNodeName()) {
                    case "bankName":
                        bankName = (String) context.convertAnother(null, String.class);
                        break;
                    case "nickName":
                        nickName = (String) context.convertAnother(null, String.class);
                        break;
                    case "score":
                        score = (String) context.convertAnother(null, String.class);
                        break;
                    default:
                        throw new ConversionException("Field is incorrect");
                }
                reader.moveUp();
            }
            MaxBalanceRecord record = new MaxBalanceRecord(0, 0, nickName, nickName, score, "0");
            return new SummaryTournamentFeedEntry(bankName, record);
        }

        @Override
        public boolean canConvert(Class type) {
            return SummaryTournamentFeedEntry.class == type;
        }
    }
}
