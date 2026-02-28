package com.abs.casino.system;

import com.dgphoenix.casino.cassandra.CassandraPersistenceManager;
import com.abs.casino.cassandra.persist.CassandraMetricsPersister;
import com.dgphoenix.casino.common.util.ApplicationContextHelper;
import com.dgphoenix.casino.common.util.ExecutorUtils;
import com.dgphoenix.casino.common.util.Pair;
import com.abs.casino.common.util.system.IMetricEvaluator;
import com.abs.casino.common.util.system.IMetricsManager;
import com.abs.casino.common.util.system.Metric;
import com.abs.casino.common.util.system.MetricStat;
import com.dgphoenix.casino.gs.GameServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by quant on 19.11.15.
 */
public class MetricsManager implements IMetricsManager {
    private static final Logger LOG = LogManager.getLogger(MetricsManager.class);
    private static final int SCHEDULE_INTERVAL = 1000;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    private final EnumMap<Metric, MetricProcessor> metrics = new EnumMap<>(Metric.class);
    private volatile boolean started = false;
    private static final MetricsManager instance = new MetricsManager();

    private final CassandraMetricsPersister metricsPersister;

    private MetricsManager() {
        CassandraPersistenceManager persistenceManager = ApplicationContextHelper.getApplicationContext()
                .getBean("persistenceManager", CassandraPersistenceManager.class);
        metricsPersister = persistenceManager.getPersister(CassandraMetricsPersister.class);
    }

    public static MetricsManager getInstance() {
        return instance;
    }

    public synchronized void register(Metric metricId, IMetricEvaluator metric) {
        if (!metrics.containsKey(metricId)) {
            metrics.put(metricId, new MetricProcessor(metric));
        } else {
            LOG.warn("Metric " + metricId + " is already registered.");
        }
    }

    public synchronized void startup() {
        if (!started) {
            LOG.info("Startup");
            started = true;
            scheduler.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    try {
                        saveAllMetrics();
                    } catch (Exception e) {
                        LOG.error("Error occurred while saving metrics", e);
                    }
                }
            }, SCHEDULE_INTERVAL, SCHEDULE_INTERVAL, TimeUnit.MILLISECONDS);

            scheduler.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    try {
                        metricsPersister.persistStat(GameServer.getInstance().getServerId());
                    } catch (Exception e) {
                        LOG.error("Error occurred while calculating metrics stat for gameServerId=" +
                                GameServer.getInstance().getServerId());
                    }
                }
            }, TimeUnit.SECONDS.toMillis(2), TimeUnit.HOURS.toMillis(1), TimeUnit.MILLISECONDS);
            LOG.info("Startup completed");
        } else {
            LOG.warn("Manager is already started");
        }
    }

    public void shutdown() {
        if (started) {
            LOG.info("Shutdown started");
            started = false;
            try {
                ExecutorUtils.shutdownService(getClass().getSimpleName(), scheduler, 2000);
            } catch (Exception e) {
                LOG.error("Cannot shutdown", e);
            }
            LOG.info("Shutdown completed");
        }
    }

    private synchronized void saveAllMetrics() {
        Set<Map.Entry<Metric, MetricProcessor>> set = metrics.entrySet();
        Map<Metric, Pair<Long, Long>> metricsForSave = new HashMap<>();
        for (Map.Entry<Metric, MetricProcessor> e : set) {
            if (!started) {
                LOG.warn("Processing was stopped");
                return;
            }
            long curTime = System.currentTimeMillis();
            MetricProcessor metricProcessor = e.getValue();
            Metric metric = e.getKey();
            if (curTime - metricProcessor.getLastLogTime() >= metric.getLogPeriodMillis()) {
                metricProcessor.setLastLogTime(curTime);
                metricsForSave.put(metric, new Pair<>(curTime, metricProcessor.process()));
            }
        }
        metricsPersister.persist(metricsForSave, GameServer.getInstance().getServerId());
    }

    public List<Pair<Long, Long>> getMetricValues(Metric metricId, int gameServerId, long startTime, long endTime) {
        return metricsPersister.getMetricValues(metricId, gameServerId, startTime, endTime);
    }

    public List<MetricStat> getMetricStatValues(Metric metricId, int gameServerId, long startTime, long endTime) {
        return metricsPersister.getMetricStatValues(metricId, gameServerId, startTime, endTime);
    }

    private static class MetricProcessor {
        private final IMetricEvaluator metric;
        private long lastLogTime = 0;

        MetricProcessor(IMetricEvaluator metric) {
            this.metric = metric;
        }

        long getLastLogTime() {
            return lastLogTime;
        }

        void setLastLogTime(long lastLogTime) {
            this.lastLogTime = lastLogTime;
        }

        long process() {
            return metric.getValue();
        }
    }
}