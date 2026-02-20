package com.dgphoenix.casino.statistics.http;

import com.dgphoenix.casino.cassandra.persist.IHttpClientStatisticsPersister;
import com.dgphoenix.casino.common.util.ApplicationContextHelper;
import com.dgphoenix.casino.common.util.CommonExecutorService;
import com.dgphoenix.casino.common.util.web.HttpClientConnectionStatistics;
import com.dgphoenix.casino.common.util.web.IHttpClientConnectionCallbackHandler;
import org.apache.commons.lang.time.FastDateFormat;
import org.apache.http.client.utils.URIBuilder;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkNotNull;

public class HttpClientCallbackHandler implements IHttpClientConnectionCallbackHandler {
    private static final Logger LOG = Logger.getLogger(HttpClientCallbackHandler.class);
    private static final HttpClientCallbackHandler INSTANCE = new HttpClientCallbackHandler();
    //hack for gamesession limit statistic storing
    private static final String GAME_SESSION_LIMIT = "GameSessionLimit";

    private final FastDateFormat dateFormat = FastDateFormat.getInstance("dd.MM.yy");
    private final LocalAccumulatedStatistics localStatistics = new LocalAccumulatedStatistics();
    private ScheduledExecutorService executor;

    private IHttpClientStatisticsPersister persister;
    private ScheduledFuture<?> flushTask;
    private boolean isEnabled;


    private HttpClientCallbackHandler() {
    }

    public static HttpClientCallbackHandler getInstance() {
        return INSTANCE;
    }

    public void init(IHttpClientStatisticsPersister persister) {
        if (!isEnabled) {
            executor = ApplicationContextHelper.getBean(CommonExecutorService.class);
            this.persister = persister;
            flushTask = executor.scheduleAtFixedRate(new FlushTask(), 30, 30, TimeUnit.MINUTES);
            isEnabled = true;
        }
    }

    public void shutdown() {
        if (isEnabled) {
            if (flushTask != null && flushTask.getDelay(TimeUnit.SECONDS) > 0) {
                new FlushTask().run();
                flushTask.cancel(true);
            }
            isEnabled = false;
        }
    }

    public void stopFlushing() {
        if (isEnabled && flushTask != null) {
            flushTask.cancel(true);
            flushTask = null;
        }
    }

    public void resumeFlushing() {
        if (isEnabled && flushTask == null) {
            flushTask = executor.scheduleAtFixedRate(new FlushTask(), 30, 30, TimeUnit.MINUTES);
        }
    }

    @Override
    public void timeout(String url) {
        if (isEnabled && url != null) {
            getStatistics(url).getTimeouts().incrementAndGet();
        }
    }

    @Override
    public void emptyResponse(String url) {
        if (isEnabled && url != null) {
            getStatistics(url).getEmptyResponces().incrementAndGet();
        }
    }

    @Override
    public void unclassifiedError(String url) {
        if (isEnabled && url != null) {
            getStatistics(url).getUnclassifiedErrors().incrementAndGet();
        }
    }

    @Override
    public void httpError503(String url, IOException e) {
        if (isEnabled && url != null) {
            getStatistics(url).getError503().incrementAndGet();
        }
    }

    @Override
    public void httpError500(String url, IOException e) {
        if (isEnabled && url != null) {
            getStatistics(url).getError500().incrementAndGet();
        }
    }

    @Override
    public void httpErrorUnclassified(String url, IOException e) {
        if (isEnabled && url != null) {
            getStatistics(url).getErrorUnclassified().incrementAndGet();
        }
    }

    @Override
    public void success(String url) {
        if (isEnabled && url != null) {
            getStatistics(url).getSuccess().incrementAndGet();
        }
    }

    @Override
    public void loginErrorByGameSessionsLimit() {
        if (isEnabled) {
            getStatistics(GAME_SESSION_LIMIT).getLoginErrorByGameSessionsLimit().incrementAndGet();
        }
    }

    @Override
    public void longRequest(String url) {
        if (isEnabled && url != null) {
            getStatistics(url).getLongRequests().incrementAndGet();
        }
    }

    public Map<StatisticsKey, HttpClientConnectionStatistics> getLocalAccumulatedStatistics() {
        return localStatistics.getAsMap();
    }

    private String getDate() {
        return getDate(System.currentTimeMillis());
    }

    public String getDate(long time) {
        return dateFormat.format(time);
    }

    private HttpClientConnectionStatistics getStatistics(String url) {
        String date = getDate();

        checkNotNull(date, "::getStatistics Arguments must be not null. Date: %s, URL: %s", date, url);
        checkNotNull(url, "::getStatistics Arguments must be not null. Date: %s, URL: %s", date, url);

        if (GAME_SESSION_LIMIT.equals(url)) {
            return localStatistics.getForKey(date, url);
        } else {
            try {
                String uriWithoutParameters = new URIBuilder(url).clearParameters().toString();
                return localStatistics.getForKey(date, uriWithoutParameters);
            } catch (URISyntaxException e) {
                throw new IllegalArgumentException("Invalid URL: " + url, e);
            }
        }
    }

    private class FlushTask implements Runnable {
        @Override
        public void run() {
            ConcurrentMap<StatisticsKey, HttpClientConnectionStatistics> oldStats = localStatistics.flushStats();
            for (Map.Entry<StatisticsKey, HttpClientConnectionStatistics> entry : oldStats.entrySet()) {

                if (Thread.currentThread().isInterrupted()) {
                    LOG.error("persist interrupted");
                    return;
                }

                StatisticsKey key = entry.getKey();
                HttpClientConnectionStatistics statistics = entry.getValue();

                persister.persist(key.date, key.url, true, statistics.getSuccess().get());

                long failCount = statistics.getError500().get() +
                        statistics.getError503().get() +
                        statistics.getTimeouts().get() +
                        statistics.getEmptyResponces().get() +
                        statistics.getErrorUnclassified().get() +
                        statistics.getUnclassifiedErrors().get();

                persister.persist(key.date, key.url, false, failCount);
            }
        }
    }
}
