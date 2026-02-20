package com.betsoft.casino.mp.data.persister;

import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.util.ExecutorUtils;
import com.dgphoenix.casino.common.util.ISequencer;
import com.dgphoenix.casino.common.util.logkit.LogUtils;
import com.dgphoenix.casino.common.util.logkit.ThreadLog;
import com.dgphoenix.casino.common.web.statistics.StatisticsManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

/**
 * User: flsh
 * Date: 4/10/12
 */
public class CassandraSequencer implements ISequencer {
    private static final Logger LOG = LogManager.getLogger(CassandraSequencer.class);

    public static final long BLOCK = 10000;
    public static final long HALF_BLOCK = BLOCK / 2;

    //private AtomicLong value;

    private volatile long base;
    private AtomicLong offset;
    private transient ReentrantLock lock;
    private String name = "";
    private transient ScheduledExecutorService scheduler;
    private transient CachedBlockAllocator blockAllocator = new CachedBlockAllocator();
    private transient volatile Thread lockerThread;
    private final SequencerPersister sequencerPersister;

    public CassandraSequencer(String name, long value, SequencerPersister sequencerPersister) {
        this.base = value;
        this.name = name;
        this.sequencerPersister = sequencerPersister;
        init();
    }

    public void init() {  //Sequencer object maybe unloaded from memory
        LOG.info("Init sequencer: " + name);
        offset = new AtomicLong(BLOCK);
        if (lock == null) {
            lock = new ReentrantLock();
        }
        if (scheduler == null) {
            scheduler = Executors.newScheduledThreadPool(1);
            blockAllocator = new CachedBlockAllocator();
            scheduler.scheduleAtFixedRate(blockAllocator, 1, 1, TimeUnit.SECONDS);
        }
    }

    public long getAndIncrement() {
        long offsetCandidate;
        long startBase;
        long startTime = System.currentTimeMillis();
        long timeOut = 60000;
        while (true) {
            if (System.currentTimeMillis() - startTime > timeOut) {
                if (lock.isLocked()) {
                    LOG.error("timeout error, lock=" + lock + ": " + LogUtils.dumpThread(lockerThread));
                }
                throw new RuntimeException("sequencer error: timeout");
            }
            startBase = base;
            long now = System.currentTimeMillis();
            offsetCandidate = offset.incrementAndGet();
            StatisticsManager.getInstance().updateRequestStatistics("Sequencer [incrementAndGet]: " + name,
                    System.currentTimeMillis() - now);
            //noinspection ConstantConditions
            if (offsetCandidate < BLOCK && startBase == base) {
                break;
            } else {
                now = System.currentTimeMillis();
                boolean changed = false;
                try {
                    //new block maybe already reserved by other thread
                    //noinspection ConstantConditions
                    if (startBase == base && lock.tryLock(500, TimeUnit.MILLISECONDS)) {
                        lockerThread = Thread.currentThread();
                        if (startBase == base && offset.get() >= BLOCK) {
                            base = allocateNextBlock();
                            offset.set(0);
                            blockAllocator.resetBlock();
                            changed = true;
                        }
                    }
                } catch (Exception e) {
                    ThreadLog.error("Cannot reserve next block", e);
                    throw new RuntimeException("Cannot reserve next block", e);
                } finally {
                    if (lock.isHeldByCurrentThread()) {
                        lockerThread = null;
                        lock.unlock();
                    }
                }
                StatisticsManager.getInstance().updateRequestStatistics("Sequencer [new block]: " + name +
                        (changed ? " success" : " miss"), System.currentTimeMillis() - now);
            }
        }
        return startBase + offsetCandidate;
    }

    private long allocateNextBlock() throws CommonException {
        long cachedBlock = blockAllocator.getCachedBlock();
        if (cachedBlock < 0) {
            return sequencerPersister.allocateNextBlock(name, BLOCK);
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
    public synchronized void setValue(long value) {
        throw new RuntimeException("Unsupported method");
    }

    @Override
    public long getValue() {
        return base;
    }

    public synchronized void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void shutdownAllocator() {
        if (scheduler != null) {
            ExecutorUtils.shutdownService(this.getClass().getSimpleName(), scheduler, 5000);
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("CassandraSequencer");
        sb.append("[name=").append(name);
        sb.append(", base=").append(base);
        sb.append(", cachedBlock=").append(blockAllocator == null ? "null" : blockAllocator.getCachedBlock());
        sb.append(", offset=").append(offset);
        sb.append(", lock=").append(lock);
        sb.append(']');
        return sb.toString();
    }

    class CachedBlockAllocator extends Thread {
        private volatile long cachedBlock = -1;

        @Override
        public void run() {
            if (offset.get() >= HALF_BLOCK && cachedBlock < 0) {
                cachedBlock = sequencerPersister.allocateNextBlock(name, BLOCK);
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
