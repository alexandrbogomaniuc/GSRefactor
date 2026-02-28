package com.abs.casino.common.util;

import com.dgphoenix.casino.common.util.ExecutorUtils;
import com.dgphoenix.casino.common.util.logkit.ThreadLog;
import com.dgphoenix.casino.common.util.string.StringUtils;
import com.dgphoenix.casino.common.web.statistics.StatisticsManager;
import org.apache.log4j.Logger;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * User: flsh
 * Date: 01.06.2012
 */
public class IntegerSequencer implements IIntegerSequencer {
    private static final Logger LOG = Logger.getLogger(IntegerSequencer.class);
    public static final int BLOCK = 256;
    public static final int HALF_BLOCK = BLOCK / 2;
    private AtomicInteger value;
    private transient volatile int base;
    private transient AtomicInteger offset;
    private transient ReentrantLock lock;
    private String name = "";
    private static int startValue;
    private transient ScheduledExecutorService scheduler;
    private transient CachedBlockAllocator blockAllocator = new CachedBlockAllocator();

    static {
        startValue = 1;
        try {
            String property = System.getProperty("INT_ID_GENERATOR_START_VALUE");
            if (!StringUtils.isTrimmedEmpty(property)) {
                startValue = Integer.parseInt(property);
            }
        } catch (Throwable e) {
            LOG.debug("IntegerSequencer::init error:", e);
        }
        LOG.info("IntegerSequencer::init index start value:" + startValue);
    }

    public IntegerSequencer(String name) {
        this(name, startValue);
    }

    public IntegerSequencer(String name, int value) {
        this.value = new AtomicInteger(value);
        init();
    }

    public void init() {  //Sequencer object maybe unloaded from memory
        LOG.info("Init sequencer: " + name);
        //offset = BLOCK;
        if (offset == null) {
            offset = new AtomicInteger(BLOCK);
        }
        if (lock == null) {
            lock = new ReentrantLock();
        }
        if (scheduler == null) {
            scheduler = Executors.newSingleThreadScheduledExecutor();
            blockAllocator = new CachedBlockAllocator();
            scheduler.scheduleAtFixedRate(blockAllocator, 1, 5, TimeUnit.SECONDS);
        }
    }

    public int getAndIncrement() {
        int offsetCandidate;
        int startBase;
        while (true) {
            startBase = base;
            long now = System.currentTimeMillis();
            offsetCandidate = offset.incrementAndGet();
            StatisticsManager.getInstance().updateRequestStatistics("IntegerSequencer [incrementAndGet]: " + name,
                    System.currentTimeMillis() - now);
            if (offsetCandidate < BLOCK && startBase == base) {
                break;
            } else {
                now = System.currentTimeMillis();
                boolean changed = false;
                try {
                    //new block maybe already reserved by other thread
                    if (startBase == base && lock.tryLock(500, TimeUnit.MILLISECONDS)) {
                        if (startBase == base && offset.get() >= BLOCK) {
                            base = allocateNextBlock();
                            offset.set(0);
                            blockAllocator.resetBlock();
                            changed = true;
                        }
                    }
                } catch (InterruptedException e) {
                    ThreadLog.error("Cannot reserve next block", e);
                    throw new RuntimeException("Cannot reserve next block", e);
                } finally {
                    if (lock.isHeldByCurrentThread()) {
                        lock.unlock();
                    }
                }
                StatisticsManager.getInstance().updateRequestStatistics("IntegerSequencer [new block]: " + name +
                        (changed ? " success" : " miss"), System.currentTimeMillis() - now);
            }
        }
        return startBase + offsetCandidate;
    }

    private int allocateNextBlock() {
        int cachedBlock = blockAllocator.getCachedBlock();
        if (cachedBlock < 0) {
            return value.getAndAdd(BLOCK);
        }
        return cachedBlock;
    }

    public int getOffset() {
        return offset.get();
    }

    public void setOffset(int offset) {
        this.offset.set(offset);
    }

    public int getBase() {
        return base;
    }

    public void setBase(int base) {
        this.base = base;
    }

    @Override
    public void setValue(int value) {
        this.value.set(value);
    }

    @Override
    public int getValue() {
        return value.get();
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void shutdownAllocator() {
        if (scheduler != null) {
            ExecutorUtils.shutdownService(this.getClass().getSimpleName(), scheduler, 2000);
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("IntegerSequencer");
        sb.append("[name=").append(name);
        sb.append(", value=").append(value);
        sb.append(", base=").append(base);
        sb.append(", cachedBlock=").append(blockAllocator == null ? "null" : blockAllocator.getCachedBlock());
        sb.append(", offset=").append(offset);
        sb.append(", lock=").append(lock);
        sb.append(']');
        return sb.toString();
    }

    class CachedBlockAllocator extends Thread {
        private volatile int cachedBlock = -1;

        @Override
        public void run() {
            if (offset.get() >= HALF_BLOCK && cachedBlock < 0) {
                cachedBlock = value.getAndAdd(BLOCK);
                LOG.info("CachedBlockAllocator: " + name + ", allocated block=" + cachedBlock);
            }
        }

        public int getCachedBlock() {
            return cachedBlock;
        }

        public void resetBlock() {
            cachedBlock = -1;
        }
    }
}
