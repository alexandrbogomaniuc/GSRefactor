package com.abs.casino.common.util;

import com.dgphoenix.casino.common.util.ExecutorUtils;
import com.dgphoenix.casino.common.util.logkit.ThreadLog;
import com.dgphoenix.casino.common.util.string.StringUtils;
import com.dgphoenix.casino.common.web.statistics.StatisticsManager;
import org.apache.log4j.Logger;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

/**
 * User: flsh
 * Date: 07.07.2009
 */
public class Sequencer implements ISequencer {
    private static final Logger LOG = Logger.getLogger(Sequencer.class);

    public static final long BLOCK = 20480;
    public static final long HALF_BLOCK = BLOCK/2;

    private AtomicLong value;

    private transient volatile long base;
    //private transient long offset;
    private transient AtomicLong offset;
    private transient ReentrantLock lock;
    private String name = "";
    private static transient long startValue;
    private transient ScheduledExecutorService scheduler;
    private transient CachedBlockAllocator blockAllocator = new CachedBlockAllocator();

    static {
        startValue = 1;
        try {
            String property = System.getProperty("ID_GENERATOR_START_VALUE");
            if (!StringUtils.isTrimmedEmpty(property)) {
                startValue = Long.valueOf(property);
            }
        } catch (Throwable e) {
            LOG.debug("Sequencer::init error:", e);
        }

        LOG.info("Sequencer::init index start value:" + startValue);
    }

    public Sequencer(String name) {
        this(name, startValue);
    }

    public Sequencer(String name, long value) {
        this.value = new AtomicLong(value);
        init();
    }

    public void init() {  //Sequencer object maybe unloaded from memory
        LOG.info("Init sequencer: " + name);
        //offset = BLOCK;
        if(offset == null) {
            offset = new AtomicLong(BLOCK);
        }
        if(lock == null) {
            lock = new ReentrantLock();
        }
        if(scheduler == null) {
            scheduler = Executors.newSingleThreadScheduledExecutor();
            blockAllocator = new CachedBlockAllocator();
            scheduler.scheduleAtFixedRate(blockAllocator, 1, 5, TimeUnit.SECONDS);
        }
    }

    public long getAndIncrement() {
        long offsetCandidate;
        long startBase;
        while(true) {
            startBase = base;
            long now = System.currentTimeMillis();
            offsetCandidate = offset.incrementAndGet();
            StatisticsManager.getInstance().updateRequestStatistics("Sequencer [incrementAndGet]: " + name,
                    System.currentTimeMillis() - now);
            if(offsetCandidate < BLOCK && startBase == base) {
                break;
            } else {
                now = System.currentTimeMillis();
                boolean changed = false;
                try {
                    //new block maybe already reserved by other thread
                    if(startBase == base && lock.tryLock(500, TimeUnit.MILLISECONDS)) {
                        if(startBase == base && offset.get() >= BLOCK) {
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
                    if(lock.isHeldByCurrentThread()) {
                        lock.unlock();
                    }
                }
                StatisticsManager.getInstance().updateRequestStatistics("Sequencer [new block]: " + name +
                        (changed ? " success" : " miss"), System.currentTimeMillis() - now);
            }
        }
        return startBase + offsetCandidate;
    }

    private long allocateNextBlock() {
        long cachedBlock = blockAllocator.getCachedBlock();
        if(cachedBlock < 0) {
            return value.getAndAdd(BLOCK);
        }
        return cachedBlock;
    }

/*
    public long getAndIncrement() {
        synchronized (lock) {
            if (offset >= BLOCK) {
                // possibly long operation with TC
                base = value.getAndAdd(BLOCK);
                offset = 0;
            }
            return base + offset++;
        }
    }
*/

    public long getOffset() {
        return offset.get();
    }

    public void setOffset(long offset) {
        this.offset.set(offset);
    }

    public long getBase() {
        return base;
    }

    public void setBase(long base) {
        this.base = base;
    }

    @Override
    public void setValue(long value) {
        this.value.set(value);
    }

    @Override
    public long getValue() {
        return value.get();
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void shutdownAllocator() {
        if(scheduler != null) {
            ExecutorUtils.shutdownService(this.getClass().getSimpleName(), scheduler, 2000);
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("Sequencer");
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
        private transient volatile long cachedBlock = -1;

        @Override
        public void run() {
            if(offset.get() >= HALF_BLOCK && cachedBlock < 0) {
                cachedBlock = value.getAndAdd(BLOCK);
                LOG.info("CachedBlockAllocator: " + name + ", allocated block=" + cachedBlock);
            }
        }

        public long getCachedBlock() {
            return cachedBlock;
        }

        public void resetBlock() {
            cachedBlock = -1;
        }
    }
}
