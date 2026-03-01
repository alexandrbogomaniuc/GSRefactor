package com.abs.casino.common.util;

import com.abs.casino.common.util.ExecutorUtils;
import com.abs.casino.common.util.ITimeProvider;
import com.abs.casino.common.util.string.StringUtils;
import com.abs.casino.common.web.statistics.IStatisticsGetter;
import com.abs.casino.common.web.statistics.StatisticsManager;
import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;
import org.apache.log4j.Logger;

import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * User: flsh
 * Date: 19.03.13
 */
public class SynchroTimeProvider implements ITimeProvider {
    private static SynchroTimeProvider instance = new SynchroTimeProvider();
    private static final Logger LOG = Logger.getLogger(SynchroTimeProvider.class);
    private final NTPUDPClient ntpClient = new NTPUDPClient();
    private InetAddress serverHost;
    private static final int DEFAULT_TIMEOUT = 3000;
    private static final long UPDATE_TIME = 200;
    private static volatile long offset = 0;
    private static long minOffset = Long.MAX_VALUE;
    private static long maxOffset = Long.MIN_VALUE;
    private static TimeInfo timeInfo;
    private static long lastUpdateTime;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    private final static ThreadLocal<Long> threadOffset = new ThreadLocal<Long>();
    private final static ThreadLocal<Long> lastThreadTime = new ThreadLocal<Long>();
    private String ntpServerHost;
    private boolean started;

    private ITimeProvider wrapped;

    public static SynchroTimeProvider getInstance() {
        return instance;
    }

    static {
        StatisticsManager.getInstance()
                .registerStatisticsGetter("SynchroTimeProvider", new IStatisticsGetter() {
                    @Override
                    public String getStatistics() {
                        StringBuilder sb = new StringBuilder();
                        if (lastUpdateTime > 0) {
                            sb.append("Last updated: ").append(new Date(lastUpdateTime));
                        }
                        sb.append(" Offset: ").append(offset);
                        sb.append(" minOffset: ").append(minOffset);
                        sb.append(" maxOffset: ").append(maxOffset);
                        if (timeInfo != null) {
                            sb.append(" timeInfo.returnTime=").append(new Date(timeInfo.getReturnTime()));
                        }
                        return sb.toString();
                    }
                });

    }

    private SynchroTimeProvider() {
        ntpClient.setDefaultTimeout(DEFAULT_TIMEOUT);
    }

    public void start(String ntpServerHost) {
        if (StringUtils.isTrimmedEmpty(ntpServerHost)) {
            LOG.warn("Ntp update disabled, ntpServerHost is not defined");
            return;
        }
        if (this.ntpServerHost != null) {
            throw new RuntimeException("Already started");
        }
        this.ntpServerHost = ntpServerHost;
        try {
            serverHost = InetAddress.getByName(getNtpServerHost());
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
        started = true;
        scheduler.scheduleAtFixedRate(new TimeUpdater(), UPDATE_TIME, UPDATE_TIME, TimeUnit.MILLISECONDS);
    }

    public void setWrappedProvider(ITimeProvider provider) {
        wrapped = provider;
    }

    public void shutdown() {
        started = false;
        ExecutorUtils.shutdownService(this.getClass().getSimpleName(), scheduler, 1000);
        LOG.info("shutdown completed");
    }

    private String getNtpServerHost() {
        return ntpServerHost;
    }

    @Override
    public long getTime() {
        if (wrapped != null) {
            return wrapped.getTime();
        }
        if (threadOffset.get() == null) {
            threadOffset.set(offset);
        }
        Long time = System.currentTimeMillis() + threadOffset.get();
        if (lastThreadTime.get() == null) {
            lastThreadTime.set(time);
        } else {
            if (lastThreadTime.get() >= time) {
                time = lastThreadTime.get() + 1;
            }
            lastThreadTime.set(time);
        }
        return time;
    }

    public void resetThreadLocalOffset() {
        threadOffset.set(null);
        lastThreadTime.set(null);
    }

    class TimeUpdater extends Thread {
        public void run() {
            try {
                if (!started) {
                    return;
                }
                long now = System.currentTimeMillis();
                ntpClient.open();
                timeInfo = ntpClient.getTime(serverHost);
                timeInfo.computeDetails();
                long oldOffset = offset;
                offset = timeInfo.getOffset();
                if (offset < 0 || (oldOffset < 0 && oldOffset < offset)) {
                    //LOG.warn("Found negative offset change: old=" + oldOffset + ", new=" + offset);
                }
                lastUpdateTime = System.currentTimeMillis();
                ntpClient.close();
                if (minOffset > offset) {
                    LOG.warn("Found minOffset change, old minOffset=" + minOffset + ", new=" + offset);
                    minOffset = offset;
                }
                if (maxOffset < offset) {
                    LOG.warn("Found maxOffset change, old maxOffset=" + maxOffset + ", new=" + offset);
                    maxOffset = offset;
                }
                if (started) {
                    StatisticsManager.getInstance().updateRequestStatistics("TimeUpdater",
                            System.currentTimeMillis() - now);
                }
            } catch (Exception e) {
                if (ntpClient.isOpen()) {
                    ntpClient.close();
                }
                if (started) {
                    if (e instanceof SocketTimeoutException) {
                        LOG.warn("SocketTimeoutException with host: " + serverHost + ", reason=" + e);
                    } else {
                        LOG.error("Cannot sync time with host: " + serverHost, e);
                    }
                }
            }
        }
    }

}
