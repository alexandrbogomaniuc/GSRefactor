package com.dgphoenix.casino.cassandra;

import com.codahale.metrics.Snapshot;
import com.datastax.driver.core.Metrics;
import com.dgphoenix.casino.common.web.statistics.IStatisticsGetter;

/**
 * Created by quant on 22.02.17.
 */
public class KeyspaceManagerStatistics implements IStatisticsGetter {
    final private Metrics metrics;

    public KeyspaceManagerStatistics(Metrics metrics) {
        this.metrics = metrics;
    }

    @Override
    public String getStatistics() {
        if (metrics == null) {
            return null;
        }
        Snapshot snapshot = metrics.getRequestsTimer().getSnapshot();
        return "knownHosts=" + metrics.getKnownHosts().getValue()
                + ", connectedToHosts=" + metrics.getConnectedToHosts().getValue()
                + ", openedConnections=" + metrics.getOpenConnections().getValue()
                + ", trashedConnections=" + metrics.getTrashedConnections().getValue()
                + ", blockingTasks=" + metrics.getBlockingExecutorQueueDepth().getValue()
                + ", nonBlockingTasks=" + metrics.getExecutorQueueDepth().getValue()
                + ", requestsTimer[requestsCount=" + metrics.getRequestsTimer().getCount()
                + ", meanRate=" + metrics.getRequestsTimer().getMeanRate()
                + ", 1minRate=" + metrics.getRequestsTimer().getOneMinuteRate()
                + ", 5minRate=" + metrics.getRequestsTimer().getFiveMinuteRate()
                + ", 15minRate=" + metrics.getRequestsTimer().getFifteenMinuteRate()
                + "]"
                + ", latency[min=" + snapshot.getMin()
                + ", max=" + snapshot.getMax()
                + ", mean=" + snapshot.getMean()
                + ", median=" + snapshot.getMedian()
                + ", stdDev=" + snapshot.getStdDev()
                + "]"
                + ", errors[connectionErrors=" + metrics.getErrorMetrics().getConnectionErrors().getCount()
                + ", ignores=" + metrics.getErrorMetrics().getIgnores().getCount()
                + ", ignoresOnReadTimeout=" + metrics.getErrorMetrics().getIgnoresOnReadTimeout().getCount()
                + ", ignoresOnWriteTimeout=" + metrics.getErrorMetrics().getIgnoresOnWriteTimeout().getCount()
                + ", ignoresOnUnavailable=" + metrics.getErrorMetrics().getIgnoresOnUnavailable().getCount()
                + ", retries=" + metrics.getErrorMetrics().getRetries().getCount()
                + ", retriesOnReadTimeout=" + metrics.getErrorMetrics().getRetriesOnReadTimeout().getCount()
                + ", retriesOnWriteTimeout=" + metrics.getErrorMetrics().getRetriesOnWriteTimeout().getCount()
                + ", retriesOnUnavailable=" + metrics.getErrorMetrics().getRetriesOnUnavailable().getCount()
                + ", readTimeout=" + metrics.getErrorMetrics().getReadTimeouts().getCount()
                + ", writeTimeout=" + metrics.getErrorMetrics().getWriteTimeouts().getCount()
                + ", unavailable=" + metrics.getErrorMetrics().getUnavailables().getCount()
                + ", others=" + metrics.getErrorMetrics().getOthers().getCount()
                + ", speculativeExecutions=" + metrics.getErrorMetrics().getSpeculativeExecutions().getCount()
                + "]";
    }
}
