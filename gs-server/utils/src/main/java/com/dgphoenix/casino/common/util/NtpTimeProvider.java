package com.dgphoenix.casino.common.util;

import com.dgphoenix.casino.common.config.UtilsApplicationContextHelper;
import com.dgphoenix.casino.common.web.statistics.StatisticsManager;
import org.apache.commons.net.ntp.NtpV3Packet;
import org.apache.commons.net.ntp.TimeInfo;
import org.apache.log4j.Logger;

import javax.annotation.PreDestroy;
import javax.xml.bind.DatatypeConverter;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static com.dgphoenix.casino.common.util.string.StringUtils.isTrimmedEmpty;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

/**
 * The same as SynchroTimeProvider, but with whole-system linear timestamps and microseconds
 * User: van0ss
 * Date: 21.02.2017
 */
public class NtpTimeProvider implements ITimeProvider {
    private static final Logger LOG = Logger.getLogger(NtpTimeProvider.class);
    private static final Logger CRITICAL_LOG = Logger.getLogger("critical");

    private final NtpWrapper ntpClient;
    private InetAddress serverHost;
    private static final int DEFAULT_TIMEOUT = 3000;
    private static final long CRITICAL_OFFSET_CHANGE = TimeUnit.DAYS.toMillis(1);
    private static final long SOCKET_TIMEOUT_WARN_INTERVAL_MS = TimeUnit.MINUTES.toMillis(1);
    private final long UPDATE_TIME;

    private volatile long offset = 0;
    private long minOffset = Long.MAX_VALUE;
    private long maxOffset = Long.MIN_VALUE;
    private long minDiff = Long.MAX_VALUE;
    private long maxDiff = Long.MIN_VALUE;
    private TimeInfo timeInfo;
    private long lastUpdateTime;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    private final AtomicLong lastTimeMilli = new AtomicLong(0);
    private final AtomicLong lastTimeMicro = new AtomicLong(0);
    private String ntpServerHost;
    private volatile boolean started;
    private volatile long lastSocketTimeoutWarnMs;

    @Deprecated
    public static NtpTimeProvider getInstance() {
        return UtilsApplicationContextHelper.getApplicationContext()
                .getBean("timeProvider", NtpTimeProvider.class);
    }

    public NtpTimeProvider() {
        UPDATE_TIME = 200;
        ntpClient = new NtpWrapper();
        ntpClient.setDefaultTimeout(DEFAULT_TIMEOUT);
    }

    /**
     * Test constructor
     */
    NtpTimeProvider(NtpWrapper ntpClient, long updateTime) {
        this.ntpClient = ntpClient;
        UPDATE_TIME = updateTime;
    }

    public void start(String ntpServerHost) {
        checkArgument(!isTrimmedEmpty(ntpServerHost), "ntpServerHost must be defined");
        checkState(!started, "Can not be started twice");

        this.ntpServerHost = ntpServerHost;
        try {
            serverHost = InetAddress.getByName(getNtpServerHost());
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
        started = true;
        scheduler.scheduleAtFixedRate(new TimeUpdater(), UPDATE_TIME, UPDATE_TIME, TimeUnit.MILLISECONDS);

        StatisticsManager.getInstance()
                .registerStatisticsGetter("NtpTimeProvider", () -> {
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
                });
    }

    @PreDestroy
    public void shutdown() {
        started = false;
        ExecutorUtils.shutdownService(this.getClass().getSimpleName(), scheduler, 1000);
        LOG.info("shutdown completed");
    }

    private String getNtpServerHost() {
        return ntpServerHost;
    }

    /**
     * @return time in milliseconds
     */
    @Override
    public long getTime() {
        while (true) {
            long last = lastTimeMilli.get();
            long currentTick = System.currentTimeMillis() + offset;
            long next = (last > currentTick) ? last : currentTick;
            if (lastTimeMilli.compareAndSet(last, next)) {
                return next;
            }
        }
    }

    public long getTimeMicroseconds() {
        while (true) {
            long last = lastTimeMicro.get();
            long currentTick = (System.currentTimeMillis() + offset) * 1000;
            long next = (last >= currentTick) ? last + 1 : currentTick;
            if (lastTimeMicro.compareAndSet(last, next)) {
                return next;
            }
        }
    }

    class TimeUpdater implements Runnable {
        @Override
        public void run() {
            if (!started) {
                return;
            }
            try {
                long now = System.currentTimeMillis();
                ntpClient.open();
                timeInfo = ntpClient.getTime(serverHost);
                timeInfo.computeDetails();
                long oldOffset = offset;
                long newOffset = timeInfo.getOffset();
                long diff = newOffset - oldOffset;
                if (Math.abs(diff) >= CRITICAL_OFFSET_CHANGE) {
                    NtpV3Packet message = timeInfo.getMessage();
                    List<String> computationComments = timeInfo.getComments();
                    String errorMessage = "Critical offset change (more than on a day), offset won't be applied" +
                            ", diff = " + diff +
                            ", oldOffset = " + oldOffset + ", newOffset = " + newOffset +
                            ", message: " + DatatypeConverter.printHexBinary(message.getDatagramPacket().getData()) +
                            ", comments: " + computationComments;
                    LOG.error(errorMessage);
                    CRITICAL_LOG.error(errorMessage);
                    return;
                }
                offset = newOffset;
                // Not count start case and system ntp case
                if (oldOffset != 0 && Math.abs(offset) > 1) {
                    if (minDiff > diff) {
                        minDiff = diff;
                        LOG.warn("New minDiff offset=" + minDiff + ", old offset=" + oldOffset + ", new offset=" + offset);
                    }
                    if (diff < -1000) {
                        LOG.warn("Offset jumped more than 1sec backward! old offset=" + oldOffset + ", new offset=" + offset);
                    }
                    if (maxDiff < diff) {
                        maxDiff = diff;
                        LOG.warn("New maxDiff offset=" + maxDiff + ", old offset=" + oldOffset + ", new offset=" + offset);
                    }
                    if (diff > 1000) {
                        LOG.warn("Offset jumped more than 1sec forward! old offset=" + oldOffset + ", new offset=" + offset);
                    }
                }
                lastUpdateTime = System.currentTimeMillis();
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
                if (started) {
                    if (e instanceof SocketTimeoutException) {
                        long now = System.currentTimeMillis();
                        if (now - lastSocketTimeoutWarnMs >= SOCKET_TIMEOUT_WARN_INTERVAL_MS) {
                            lastSocketTimeoutWarnMs = now;
                            LOG.warn("SocketTimeoutException with host: " + serverHost + ", reason=" + e
                                    + " (further timeout warnings are rate-limited)");
                        } else {
                            LOG.debug("SocketTimeoutException with host: " + serverHost + ", reason=" + e);
                        }
                    } else {
                        LOG.error("Cannot sync time with host: " + serverHost, e);
                    }
                }
            } finally {
                ntpClient.close();
            }
        }
    }

}
