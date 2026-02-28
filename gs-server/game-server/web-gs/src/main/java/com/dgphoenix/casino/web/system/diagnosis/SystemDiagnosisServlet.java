package com.abs.casino.web.system.diagnosis;

import com.dgphoenix.casino.cassandra.CassandraPersistenceManager;
import com.abs.casino.cassandra.IKeyspaceManager;
import com.dgphoenix.casino.cassandra.persist.CassandraCurrencyRatesPersister;
import com.abs.casino.common.config.FreeSpaceThresholdType;
import com.abs.casino.common.config.MountMonitoringEntry;
import com.abs.casino.common.currency.CurrencyRate;
import com.dgphoenix.casino.common.util.ApplicationContextHelper;
import com.dgphoenix.casino.common.util.CommonExecutorService;
import com.abs.casino.common.util.web.HttpClientConnection;
import com.abs.casino.common.web.diagnostic.BaseDiagnosisServlet;
import com.abs.casino.common.web.diagnostic.CheckTask;
import com.dgphoenix.casino.common.web.statistics.StatisticsManager;
import com.dgphoenix.casino.gs.GameServer;
import com.abs.casino.gs.managers.payment.bonus.tracker.FRBonusWinTracker;
import com.dgphoenix.casino.gs.managers.payment.wallet.tracker.WalletTracker;
import com.dgphoenix.casino.system.configuration.GameServerConfiguration;
import com.abs.casino.web.system.diagnosis.tasks.CassandraStateMonitoringTask;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.ServletException;
import java.io.File;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;

public class SystemDiagnosisServlet extends BaseDiagnosisServlet {
    private static final Logger LOG = LogManager.getLogger(SystemDiagnosisServlet.class);
    private static final int STATISTICS_DROP_PERIOD = 1;
    private final GameServerConfiguration configuration = ApplicationContextHelper.getApplicationContext()
            .getBean("gameServerConfiguration", GameServerConfiguration.class);

    @Override
    public boolean discontinuesSameTypeErrorsDiagnosticEnabled() {
        return configuration.discontinuesSameTypeErrorsDiagnosticEnabled();
    }

    @Override
    public void init() throws ServletException {
        super.init();
        LOG.debug("SystemDiagnosisServlet::init");
        RunnableCheckTask tmpRun;
        tmpRun = new TrackerUpdaterCheckTask("WalletTracker is out 10 or more minutes", false,
                WalletTracker.getInstance(), 10);
        checkerPool.scheduleWithFixedDelay(tmpRun, 0, 1, TimeUnit.MINUTES);
        taskList.add(tmpRun);

        tmpRun = new TrackerUpdaterCheckTask("FRBonusWinTracker is out 10 or more minutes", false,
                FRBonusWinTracker.getInstance(), 10);
        checkerPool.scheduleWithFixedDelay(tmpRun, 0, 1, TimeUnit.MINUTES);
        taskList.add(tmpRun);

        taskList.add(new CheckTask("HttpClientConnection too busy", true) {
            private final int max = HttpClientConnection.getMaxTotal();

            public boolean isOut(boolean strongValidation) {
                int current = HttpClientConnection.getConnectionsInPool();
                if (current >= max * 0.9) {
                    warning = current != max;
                    return true;
                }
                return false;
            }
        });

        taskList.add(new CheckTask("Cassandra nodes down:", false) {
            private Set<String> hosts = null;

            @Override
            public boolean isOut(boolean strongValidation) {
                CassandraPersistenceManager persistenceManager = ApplicationContextHelper.getApplicationContext()
                        .getBean("persistenceManager", CassandraPersistenceManager.class);
                Collection<IKeyspaceManager> managers = persistenceManager.getKeyspaceManagers();
                for (IKeyspaceManager keySpaceManager : managers) {
                    hosts = keySpaceManager.getDownHostAddresses();
                    if (!hosts.isEmpty()) {
                        return true;
                    }
                }
                return false;
            }

            @Override
            public String getErrorMessage() {
                StringBuilder builder = new StringBuilder(super.getErrorMessage());
                boolean first = true;
                for (String host : hosts) {
                    if (first) {
                        first = false;
                    } else {
                        builder.append(", ");
                    }
                    builder.append(host);
                }
                return builder.toString();
            }
        });

        taskList.add(new FreeSpaceMonitoringTask());

        final Runnable statisticsLoggingTask = new Runnable() {
            final Logger STATISTICS_LOGGER = LogManager.getLogger("SystemDiagnosisServlet.StatisticsLogger");
            final StringBuilder statistics = new StringBuilder();

            @Override
            public void run() {
                STATISTICS_LOGGER.debug("Start task");
                statistics.setLength(0);
                StatisticsManager.getInstance().printRequestStatistics(statistics, true);
                STATISTICS_LOGGER.info(statistics);
            }
        };
        taskList.add(new CassandraStateMonitoringTask());
        taskList.add(new ExchangeRateMonitoringTask());
        ScheduledExecutorService statisticsLoggerScheduleService = ApplicationContextHelper.getBean(CommonExecutorService.class);
        statisticsLoggerScheduleService.scheduleWithFixedDelay(statisticsLoggingTask, STATISTICS_DROP_PERIOD, STATISTICS_DROP_PERIOD, TimeUnit.MINUTES);
    }

    @Override
    protected boolean isInitializationCompleted() {
        return GameServer.getInstance().isServletContextInitialized();
    }

    private class FreeSpaceMonitoringTask extends CheckTask {
        private static final String ERROR_MESSAGE = "Critical free space amount. Path: {0}, " +
                "total: {1} MB, free: {2} MB ({3}%).\n";

        public FreeSpaceMonitoringTask() {
            super(null, true);
        }

        @Override
        public boolean isOut(boolean strongValidation) {
            warning = true;

            GameServerConfiguration serverConfiguration = GameServerConfiguration.getInstance();
            Set<MountMonitoringEntry> mountMonitoringEntries = serverConfiguration.getDiskFreeSpaceMonitoringSettings();

            StringBuilder errorBuilder = new StringBuilder();
            for (MountMonitoringEntry monitoringEntry : mountMonitoringEntries) {
                try {
                    long totalSpaceMB = getTotalSpaceMB(monitoringEntry.getMountPath());
                    long freeSpaceMB = getFreeSpaceMB(monitoringEntry.getMountPath());

                    FreeSpaceThresholdType thresholdType = monitoringEntry.getThresholdType();
                    long thresholdAmount = monitoringEntry.getThresholdAmount();

                    if (thresholdType.isFreeSpaceMuchLower(thresholdAmount, totalSpaceMB, freeSpaceMB)) {
                        warning = false;
                        errorBuilder.append(buildErrorMessage(monitoringEntry, totalSpaceMB, freeSpaceMB));

                    } else if (thresholdType.isFreeSpaceLower(thresholdAmount, totalSpaceMB, freeSpaceMB)) {
                        errorBuilder.append(buildErrorMessage(monitoringEntry, totalSpaceMB, freeSpaceMB));
                    }
                } catch (Exception e) {
                    LOG.error("FreeSpaceMonitoringTask:: error, mountPath: {}", monitoringEntry.getMountPath(), e);
                }
            }
            errorMessage = errorBuilder.toString();

            return !errorMessage.isEmpty();
        }

        private long getTotalSpaceMB(String path) {
            long totalSpaceInMB = new File(path).getTotalSpace() / (1024 * 1024);
            checkArgument(totalSpaceInMB != 0, "Mount for path: %s not found", path);
            return totalSpaceInMB;
        }

        private long getFreeSpaceMB(String path) {
            long freeSpaceMB = new File(path).getUsableSpace() / (1024 * 1024);
            checkArgument(freeSpaceMB != 0, "Mount for path: %s not found", path);
            return freeSpaceMB;
        }

        private String buildErrorMessage(MountMonitoringEntry monitoringEntry, long totalSpaceMB, long freeSpaceMB) {
            long freeSpacePercentage = FreeSpaceThresholdType.PERCENTAGE.calculateFreeSpace(totalSpaceMB, freeSpaceMB);
            return MessageFormat.format(ERROR_MESSAGE, monitoringEntry.getMountPath(), totalSpaceMB, freeSpaceMB,
                    freeSpacePercentage);
        }
    }

    private class ExchangeRateMonitoringTask extends CheckTask {
        private static final int LIMIT_HOURS = 36;
        private static final String MONITORED_CURRENCY_RATE_FROM = "USD";
        private static final String MONITORED_CURRENCY_RATE_TO = "EUR";
        private static final String ERROR_MSG_TMPL = "Currency exchange rate from %s to %s was not updated for %d hours";
        private final CassandraPersistenceManager persistenceManager;
        private final CassandraCurrencyRatesPersister cassandraCurrencyRatesPersister;
        private final long limitMillis = TimeUnit.HOURS.toMillis(LIMIT_HOURS);

        public ExchangeRateMonitoringTask() {
            super(null, true);
            persistenceManager = ApplicationContextHelper.getApplicationContext().getBean("persistenceManager", CassandraPersistenceManager.class);
            cassandraCurrencyRatesPersister = persistenceManager.getPersister(CassandraCurrencyRatesPersister.class);
        }

        @Override
        public boolean isOut(boolean strongValidation) {
            String source = MONITORED_CURRENCY_RATE_FROM;
            String target = MONITORED_CURRENCY_RATE_TO;

            Collection<CurrencyRate> currencyRates = cassandraCurrencyRatesPersister.getRates();
            if (currencyRates == null || currencyRates.isEmpty()) {
                LOG.debug("Currency rates not found");
                return false;
            }

            List<CurrencyRate> staleRates = currencyRates.stream()
                    .filter(expired())
                    .collect(Collectors.toList());
            if (staleRates.isEmpty()) {
                LOG.trace("There are no stale currencies rates");
            } else {
                LOG.debug("Stale currency rates: " + staleRates.toString());
            }

            Optional<CurrencyRate> monitoredRate = staleRates.stream()
                    .filter(cr -> source.equals(cr.getSourceCurrency()) && target.equals(cr.getDestinationCurrency()))
                    .findFirst();

            if (monitoredRate.isPresent()) {
                long expMillis = System.currentTimeMillis() - monitoredRate.get().getUpdateDate();
                LOG.error("{} to {} was updated {} hours ago", source, target, TimeUnit.MILLISECONDS.toHours(expMillis));
                this.warning = true;
                errorMessage = String.format(ERROR_MSG_TMPL, source, target, LIMIT_HOURS);
                return true;
            }
            return false;
        }

        private Predicate<CurrencyRate> expired() {
            return currencyRate -> System.currentTimeMillis() - currencyRate.getUpdateDate() >= limitMillis;
        }
    }

    @Override
    public void destroy() {
        super.destroy();
    }
}
