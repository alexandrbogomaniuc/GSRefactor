package com.abs.casino.promo.feed;

import com.dgphoenix.casino.common.util.ApplicationContextHelper;
import com.dgphoenix.casino.common.util.CommonExecutorService;
import com.dgphoenix.casino.common.util.ExecutorUtils;
import com.dgphoenix.casino.common.util.string.StringUtils;
import com.abs.casino.common.util.web.HttpClientConnection;
import com.dgphoenix.casino.gs.status.ServersStatusWatcher;
import com.dgphoenix.casino.system.configuration.GameServerConfiguration;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.json.JsonHierarchicalStreamDriver;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import org.apache.logging.log4j.Logger;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * User: flsh
 * Date: 20.11.2019.
 */
public abstract class AbstractSummaryFeedWriter {
    protected static final int DOWNLOAD_POOL_SIZE = 5;
    protected static final long MAX_DOWNLOAD_TASK_TIME = TimeUnit.SECONDS.toMillis(90);
    protected static final ScheduledExecutorService mainService = ApplicationContextHelper.getBean(CommonExecutorService.class);
    protected static final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(DOWNLOAD_POOL_SIZE);
    protected static final long DEFAULT_UPDATE_PERIOD = 2;
    protected static final long END_PROMO_PERIOD = TimeUnit.HOURS.toMillis(2);
    protected static final long TIMEOUT_MILLIS = TimeUnit.MINUTES.toMillis(1);
    protected static final int MAX_RESPONSE_SIZE_BYTES = 20 * 1024 * 1024;
    protected static final XStream xStream = new XStream(new JsonHierarchicalStreamDriver());

    protected final GameServerConfiguration configuration;
    protected final String promoRootPath;

    public AbstractSummaryFeedWriter(GameServerConfiguration configuration) {
        this.configuration = configuration;
        promoRootPath = configuration.getPromoFeedsRootPath();
    }

    protected abstract Logger getLogger();

    @PreDestroy
    protected void shutdown() {
        getLogger().debug("shutdown started");
        ExecutorUtils.shutdownService("SummaryRaffleFeedWriter:downloadFeeds", executor, 5000);
        getLogger().debug("shutdown completed");
    }

    protected void uploadTempFile(String srcPath, String destPath) throws IOException {
        for (String host : configuration.getSshJackpotsUploadHosts()) {
            try (SSHClient sshClient = new SSHClient()) {
                sshClient.addHostKeyVerifier(new PromiscuousVerifier());
                sshClient.connect(host, configuration.getSshJackpotsUploadPort());
                sshClient.authPassword(configuration.getSshStatisticUpdaterUser(), configuration.getSshStatisticUpdaterPass());
                sshClient.newSCPFileTransfer().upload(srcPath, destPath);
            }
        }
    }

    protected abstract void updateFeedPersister(long promoId, String url, String newMd5, String feed);

    public class DownloadFeedTask implements Runnable {
        private final long promoId;
        private final String url;
        private final String checksum;

        public DownloadFeedTask(long promoId, String url, String checksum) {
            this.promoId = promoId;
            this.url = url;
            this.checksum = checksum;
        }

        @Override
        public void run() {
            getLogger().debug("DownloadFeedTask run: id={}, url={}, checksum={}", promoId, url, checksum);
            try {
                String feed = HttpClientConnection.newInstance(TIMEOUT_MILLIS, MAX_RESPONSE_SIZE_BYTES)
                        .doRequest(url, null);
                getLogger().debug("DownloadFeedTask feed: id={}, url={}, feed={}", promoId, url, feed);
                if (!StringUtils.isTrimmedEmpty(feed)) {
                    String newMd5 = StringUtils.getMD5(feed);
                    if (StringUtils.isTrimmedEmpty(checksum) || !newMd5.equals(checksum)) {
                        getLogger().debug("DownloadFeedTask changed feed found: id={}, url={}, oldMd5={}, newMd5={}",
                                promoId, url, checksum, newMd5);
                        if (!ServersStatusWatcher.getInstance().isMaster()) {
                            getLogger().debug("DownloadFeedTask: not master, exit");
                            return;
                        }
                        updateFeedPersister(promoId, url, newMd5, feed);
                    }
                }
            } catch (Exception e) {
                getLogger().error("Cannot fetch url: " + url, e);
            }
        }
    }
}
