package com.abs.casino.gs;

import com.dgphoenix.casino.common.SessionHelper;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.transactiondata.ITransactionData;
import com.dgphoenix.casino.common.util.ExecutorUtils;
import com.dgphoenix.casino.common.web.statistics.StatisticsManager;
import com.abs.casino.sm.tracker.logout.LogoutTracker;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Map;
import java.util.concurrent.*;

/**
 * @author <a href="mailto:zomac@dgphoenix.com">Roman Sorokin</a>
 * @since 6/16/22
 */
public class LocalSessionTracker {

    private static final Logger LOG = LogManager.getLogger(LocalSessionTracker.class);

    private final ConcurrentMap<Long,SessionActivityInfo> accountSessions;
    private final ScheduledExecutorService executorService;

    public LocalSessionTracker() {
        accountSessions = new ConcurrentHashMap<>();
        executorService = Executors.newSingleThreadScheduledExecutor();
    }

    public void addSession(long accountId, long timeout) {
        SessionActivityInfo sessionActivityInfo = new SessionActivityInfo(timeout, System.currentTimeMillis());
        accountSessions.put(accountId, sessionActivityInfo);
    }

    @PostConstruct
    private void init() {
        executorService.scheduleAtFixedRate(new TrackingTask(), 10, 10, TimeUnit.SECONDS);
        StatisticsManager.getInstance().registerStatisticsGetter(getClass().getSimpleName(), () -> "size=" + accountSessions.size());
    }

    @PreDestroy
    private void shutdown() {
        ExecutorUtils.shutdownService(getClass().getSimpleName(), executorService, 5000);
    }

    private class TrackingTask implements Runnable {
        @Override
        public void run() {
            long now = System.currentTimeMillis();
            for (Map.Entry<Long, SessionActivityInfo> accountSession : accountSessions.entrySet()) {
                long accountId = accountSession.getKey();
                SessionActivityInfo sessionActivity = accountSession.getValue();
                if (now > sessionActivity.getLastTouchDate() + sessionActivity.getTimeout()) {
                    try {
                        SessionHelper.getInstance().lock(accountId);
                        try {
                            SessionHelper.getInstance().openSession();
                            ITransactionData transactionData = SessionHelper.getInstance().getTransactionData();
                            if (transactionData == null || transactionData.getPlayerSession() == null) {
                                accountSessions.remove(accountId);
                                continue;
                            }
                            long lastActivityTime = transactionData.getPlayerSession().getLastActivityTime();
                            if (lastActivityTime > sessionActivity.getLastTouchDate()) {
                                sessionActivity.updateLastTouchDate(lastActivityTime);
                            } else {
                                LogoutTracker.getInstance().addTask(accountId);
                                accountSessions.remove(accountId);
                            }
                            SessionHelper.getInstance().markTransactionCompleted();
                        } finally {
                            SessionHelper.getInstance().clearWithUnlock();
                        }
                    } catch (CommonException e) {
                        LOG.error("Error occurred while locking account={}", accountId, e);
                    }
                }
            }
            StatisticsManager.getInstance().updateRequestStatistics("LocalSessionTracker execution time", System.currentTimeMillis() - now);
        }
    }

    private static class SessionActivityInfo {

        private final long timeout;
        private long lastTouchDate;

        public SessionActivityInfo(long timeout, long lastTouchDate) {
            this.timeout = timeout;
            this.lastTouchDate = lastTouchDate;
        }

        public long getTimeout() {
            return timeout;
        }

        public long getLastTouchDate() {
            return lastTouchDate;
        }

        public void updateLastTouchDate(long lastTouchDate) {
            this.lastTouchDate = lastTouchDate;
        }
    }
}
