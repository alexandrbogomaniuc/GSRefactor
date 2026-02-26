package com.dgphoenix.casino.cassandra;

import com.codahale.metrics.Snapshot;
import com.codahale.metrics.Timer;

/**
 * Driver-neutral snapshot of Cassandra keyspace driver metrics.
 */
public final class KeyspaceMetricsSnapshot {

    private static final KeyspaceMetricsSnapshot UNAVAILABLE = new KeyspaceMetricsSnapshot(false,
            0L, 0L, 0L, 0L, 0L, 0L,
            0L, 0D, 0D, 0D, 0D,
            0L, 0L, 0D, 0D, 0D,
            0L, 0L, 0L, 0L, 0L,
            0L, 0L, 0L, 0L,
            0L, 0L, 0L, 0L, 0L);

    private final boolean available;

    private final long knownHosts;
    private final long connectedToHosts;
    private final long openedConnections;
    private final long trashedConnections;
    private final long blockingTasks;
    private final long nonBlockingTasks;

    private final long requestsCount;
    private final double meanRate;
    private final double oneMinuteRate;
    private final double fiveMinuteRate;
    private final double fifteenMinuteRate;

    private final long latencyMin;
    private final long latencyMax;
    private final double latencyMean;
    private final double latencyMedian;
    private final double latencyStdDev;

    private final long connectionErrors;
    private final long ignores;
    private final long ignoresOnReadTimeout;
    private final long ignoresOnWriteTimeout;
    private final long ignoresOnUnavailable;
    private final long retries;
    private final long retriesOnReadTimeout;
    private final long retriesOnWriteTimeout;
    private final long retriesOnUnavailable;
    private final long readTimeout;
    private final long writeTimeout;
    private final long unavailable;
    private final long others;
    private final long speculativeExecutions;

    private KeyspaceMetricsSnapshot(boolean available,
                                    long knownHosts,
                                    long connectedToHosts,
                                    long openedConnections,
                                    long trashedConnections,
                                    long blockingTasks,
                                    long nonBlockingTasks,
                                    long requestsCount,
                                    double meanRate,
                                    double oneMinuteRate,
                                    double fiveMinuteRate,
                                    double fifteenMinuteRate,
                                    long latencyMin,
                                    long latencyMax,
                                    double latencyMean,
                                    double latencyMedian,
                                    double latencyStdDev,
                                    long connectionErrors,
                                    long ignores,
                                    long ignoresOnReadTimeout,
                                    long ignoresOnWriteTimeout,
                                    long ignoresOnUnavailable,
                                    long retries,
                                    long retriesOnReadTimeout,
                                    long retriesOnWriteTimeout,
                                    long retriesOnUnavailable,
                                    long readTimeout,
                                    long writeTimeout,
                                    long unavailable,
                                    long others,
                                    long speculativeExecutions) {
        this.available = available;
        this.knownHosts = knownHosts;
        this.connectedToHosts = connectedToHosts;
        this.openedConnections = openedConnections;
        this.trashedConnections = trashedConnections;
        this.blockingTasks = blockingTasks;
        this.nonBlockingTasks = nonBlockingTasks;
        this.requestsCount = requestsCount;
        this.meanRate = meanRate;
        this.oneMinuteRate = oneMinuteRate;
        this.fiveMinuteRate = fiveMinuteRate;
        this.fifteenMinuteRate = fifteenMinuteRate;
        this.latencyMin = latencyMin;
        this.latencyMax = latencyMax;
        this.latencyMean = latencyMean;
        this.latencyMedian = latencyMedian;
        this.latencyStdDev = latencyStdDev;
        this.connectionErrors = connectionErrors;
        this.ignores = ignores;
        this.ignoresOnReadTimeout = ignoresOnReadTimeout;
        this.ignoresOnWriteTimeout = ignoresOnWriteTimeout;
        this.ignoresOnUnavailable = ignoresOnUnavailable;
        this.retries = retries;
        this.retriesOnReadTimeout = retriesOnReadTimeout;
        this.retriesOnWriteTimeout = retriesOnWriteTimeout;
        this.retriesOnUnavailable = retriesOnUnavailable;
        this.readTimeout = readTimeout;
        this.writeTimeout = writeTimeout;
        this.unavailable = unavailable;
        this.others = others;
        this.speculativeExecutions = speculativeExecutions;
    }

    public static KeyspaceMetricsSnapshot unavailable() {
        return UNAVAILABLE;
    }

    public static KeyspaceMetricsSnapshot from(com.datastax.driver.core.Metrics metrics) {
        if (metrics == null) {
            return unavailable();
        }

        try {
            Timer requestsTimer = metrics.getRequestsTimer();
            Snapshot latencySnapshot = requestsTimer != null ? requestsTimer.getSnapshot() : null;
            com.datastax.driver.core.Metrics.Errors errors = metrics.getErrorMetrics();

            return new KeyspaceMetricsSnapshot(true,
                    safeGaugeValue(metrics.getKnownHosts()),
                    safeGaugeValue(metrics.getConnectedToHosts()),
                    safeGaugeValue(metrics.getOpenConnections()),
                    safeGaugeValue(metrics.getTrashedConnections()),
                    safeGaugeValue(metrics.getBlockingExecutorQueueDepth()),
                    safeGaugeValue(metrics.getExecutorQueueDepth()),
                    requestsTimer != null ? requestsTimer.getCount() : 0L,
                    requestsTimer != null ? requestsTimer.getMeanRate() : 0D,
                    requestsTimer != null ? requestsTimer.getOneMinuteRate() : 0D,
                    requestsTimer != null ? requestsTimer.getFiveMinuteRate() : 0D,
                    requestsTimer != null ? requestsTimer.getFifteenMinuteRate() : 0D,
                    latencySnapshot != null ? latencySnapshot.getMin() : 0L,
                    latencySnapshot != null ? latencySnapshot.getMax() : 0L,
                    latencySnapshot != null ? latencySnapshot.getMean() : 0D,
                    latencySnapshot != null ? latencySnapshot.getMedian() : 0D,
                    latencySnapshot != null ? latencySnapshot.getStdDev() : 0D,
                    errors != null ? errors.getConnectionErrors().getCount() : 0L,
                    errors != null ? errors.getIgnores().getCount() : 0L,
                    errors != null ? errors.getIgnoresOnReadTimeout().getCount() : 0L,
                    errors != null ? errors.getIgnoresOnWriteTimeout().getCount() : 0L,
                    errors != null ? errors.getIgnoresOnUnavailable().getCount() : 0L,
                    errors != null ? errors.getRetries().getCount() : 0L,
                    errors != null ? errors.getRetriesOnReadTimeout().getCount() : 0L,
                    errors != null ? errors.getRetriesOnWriteTimeout().getCount() : 0L,
                    errors != null ? errors.getRetriesOnUnavailable().getCount() : 0L,
                    errors != null ? errors.getReadTimeouts().getCount() : 0L,
                    errors != null ? errors.getWriteTimeouts().getCount() : 0L,
                    errors != null ? errors.getUnavailables().getCount() : 0L,
                    errors != null ? errors.getOthers().getCount() : 0L,
                    errors != null ? errors.getSpeculativeExecutions().getCount() : 0L);
        } catch (Throwable ignored) {
            return unavailable();
        }
    }

    private static long safeGaugeValue(com.codahale.metrics.Gauge<?> gauge) {
        if (gauge == null || gauge.getValue() == null) {
            return 0L;
        }
        Object value = gauge.getValue();
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        try {
            return Long.parseLong(value.toString());
        } catch (Exception ignored) {
            return 0L;
        }
    }

    public boolean isAvailable() {
        return available;
    }

    public long getKnownHosts() {
        return knownHosts;
    }

    public long getConnectedToHosts() {
        return connectedToHosts;
    }

    public long getOpenedConnections() {
        return openedConnections;
    }

    public long getTrashedConnections() {
        return trashedConnections;
    }

    public long getBlockingTasks() {
        return blockingTasks;
    }

    public long getNonBlockingTasks() {
        return nonBlockingTasks;
    }

    public long getRequestsCount() {
        return requestsCount;
    }

    public double getMeanRate() {
        return meanRate;
    }

    public double getOneMinuteRate() {
        return oneMinuteRate;
    }

    public double getFiveMinuteRate() {
        return fiveMinuteRate;
    }

    public double getFifteenMinuteRate() {
        return fifteenMinuteRate;
    }

    public long getLatencyMin() {
        return latencyMin;
    }

    public long getLatencyMax() {
        return latencyMax;
    }

    public double getLatencyMean() {
        return latencyMean;
    }

    public double getLatencyMedian() {
        return latencyMedian;
    }

    public double getLatencyStdDev() {
        return latencyStdDev;
    }

    public long getConnectionErrors() {
        return connectionErrors;
    }

    public long getIgnores() {
        return ignores;
    }

    public long getIgnoresOnReadTimeout() {
        return ignoresOnReadTimeout;
    }

    public long getIgnoresOnWriteTimeout() {
        return ignoresOnWriteTimeout;
    }

    public long getIgnoresOnUnavailable() {
        return ignoresOnUnavailable;
    }

    public long getRetries() {
        return retries;
    }

    public long getRetriesOnReadTimeout() {
        return retriesOnReadTimeout;
    }

    public long getRetriesOnWriteTimeout() {
        return retriesOnWriteTimeout;
    }

    public long getRetriesOnUnavailable() {
        return retriesOnUnavailable;
    }

    public long getReadTimeout() {
        return readTimeout;
    }

    public long getWriteTimeout() {
        return writeTimeout;
    }

    public long getUnavailable() {
        return unavailable;
    }

    public long getOthers() {
        return others;
    }

    public long getSpeculativeExecutions() {
        return speculativeExecutions;
    }
}
