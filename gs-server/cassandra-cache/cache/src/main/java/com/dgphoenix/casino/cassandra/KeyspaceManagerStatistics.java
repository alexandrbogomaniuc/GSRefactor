package com.abs.casino.cassandra;

import com.abs.casino.common.web.statistics.IStatisticsGetter;

import java.util.function.Supplier;

/**
 * Created by quant on 22.02.17.
 */
public class KeyspaceManagerStatistics implements IStatisticsGetter {
    private final Supplier<KeyspaceMetricsSnapshot> metricsSnapshotSupplier;

    public KeyspaceManagerStatistics(Supplier<KeyspaceMetricsSnapshot> metricsSnapshotSupplier) {
        this.metricsSnapshotSupplier = metricsSnapshotSupplier;
    }

    @Override
    public String getStatistics() {
        if (metricsSnapshotSupplier == null) {
            return null;
        }
        KeyspaceMetricsSnapshot snapshot = metricsSnapshotSupplier.get();
        if (snapshot == null || !snapshot.isAvailable()) {
            return null;
        }
        return "knownHosts=" + snapshot.getKnownHosts()
                + ", connectedToHosts=" + snapshot.getConnectedToHosts()
                + ", openedConnections=" + snapshot.getOpenedConnections()
                + ", trashedConnections=" + snapshot.getTrashedConnections()
                + ", blockingTasks=" + snapshot.getBlockingTasks()
                + ", nonBlockingTasks=" + snapshot.getNonBlockingTasks()
                + ", requestsTimer[requestsCount=" + snapshot.getRequestsCount()
                + ", meanRate=" + snapshot.getMeanRate()
                + ", 1minRate=" + snapshot.getOneMinuteRate()
                + ", 5minRate=" + snapshot.getFiveMinuteRate()
                + ", 15minRate=" + snapshot.getFifteenMinuteRate()
                + "]"
                + ", latency[min=" + snapshot.getLatencyMin()
                + ", max=" + snapshot.getLatencyMax()
                + ", mean=" + snapshot.getLatencyMean()
                + ", median=" + snapshot.getLatencyMedian()
                + ", stdDev=" + snapshot.getLatencyStdDev()
                + "]"
                + ", errors[connectionErrors=" + snapshot.getConnectionErrors()
                + ", ignores=" + snapshot.getIgnores()
                + ", ignoresOnReadTimeout=" + snapshot.getIgnoresOnReadTimeout()
                + ", ignoresOnWriteTimeout=" + snapshot.getIgnoresOnWriteTimeout()
                + ", ignoresOnUnavailable=" + snapshot.getIgnoresOnUnavailable()
                + ", retries=" + snapshot.getRetries()
                + ", retriesOnReadTimeout=" + snapshot.getRetriesOnReadTimeout()
                + ", retriesOnWriteTimeout=" + snapshot.getRetriesOnWriteTimeout()
                + ", retriesOnUnavailable=" + snapshot.getRetriesOnUnavailable()
                + ", readTimeout=" + snapshot.getReadTimeout()
                + ", writeTimeout=" + snapshot.getWriteTimeout()
                + ", unavailable=" + snapshot.getUnavailable()
                + ", others=" + snapshot.getOthers()
                + ", speculativeExecutions=" + snapshot.getSpeculativeExecutions()
                + "]";
    }
}
