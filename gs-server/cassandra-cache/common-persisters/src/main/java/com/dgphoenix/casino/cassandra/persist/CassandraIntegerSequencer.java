package com.dgphoenix.casino.cassandra.persist;

import com.dgphoenix.casino.cassandra.CassandraPersistenceManager;
import com.dgphoenix.casino.common.util.ApplicationContextHelper;
import com.dgphoenix.casino.common.util.ExecutorUtils;
import com.dgphoenix.casino.common.util.IIntegerSequencer;
import com.abs.casino.common.util.logkit.LogUtils;
import com.abs.casino.common.util.logkit.ThreadLog;
import com.dgphoenix.casino.common.web.statistics.StatisticsManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * User: flsh
 * Date: 4/10/12
 */
public class CassandraIntegerSequencer implements IIntegerSequencer {
    private static final Logger LOG = LogManager.getLogger(CassandraIntegerSequencer.class);
    public static final int BLOCK = 256;
    public static final int HALF_BLOCK = BLOCK / 2;
    private volatile int base;
    private AtomicInteger offset;
    private transient ReentrantLock lock;
    private String name;
    private transient ScheduledExecutorService scheduler;
    private transient CachedBlockAllocator blockAllocator = new CachedBlockAllocator();
    private transient Thread lockerThread;
    private final transient CassandraIntSequencerPersister sequencerPersister;

    public CassandraIntegerSequencer(String name, int value) {
        this.base = value;
        this.name = name;
        init();
        CassandraPersistenceManager persistenceManager = ApplicationContextHelper.getApplicationContext()
                .getBean("persistenceManager", CassandraPersistenceManager.class);
        sequencerPersister = persistenceManager.getPersister(CassandraIntSequencerPersister.class);
    }

    public void init() {  //Sequencer object maybe unloaded from memory
        LOG.info("Init sequencer: {}", name);
        offset = new AtomicInteger(BLOCK);
        if (lock == null) {
            lock = new ReentrantLock();
        }
        if (scheduler == null) {
            scheduler = Executors.newScheduledThreadPool(1);
            blockAllocator = new CachedBlockAllocator();
            scheduler.scheduleAtFixedRate(blockAllocator, 1, 1, TimeUnit.SECONDS);
        }
    }

    public int getAndIncrement() {
        int offsetCandidate;
        int startBase;
        long startTime = System.currentTimeMillis();
        long timeOut = 60000;
        while (true) {
            if (System.currentTimeMillis() - startTime > timeOut) {
                if (lock.isLocked()) {
                    LOG.error("timeout error, lock={}: {}", lock, LogUtils.dumpThread(lockerThread));
                }
                throw new IllegalStateException("sequencer error: timeout");
            }
            startBase = base;
            long now = System.currentTimeMillis();
            offsetCandidate = offset.incrementAndGet();
            StatisticsManager.getInstance().updateRequestStatistics("Sequencer [incrementAndGet]: " + name,
                    System.currentTimeMillis() - now);
            if (offsetCandidate < BLOCK && startBase == base) {
                break;
            } else {
                now = System.currentTimeMillis();
                boolean changed = false;
                try {
                    //new block maybe already reserved by other thread
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
                    throw new IllegalStateException("Cannot reserve next block", e);
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

    private int allocateNextBlock() {
        int cachedBlock = blockAllocator.getCachedBlock();
        if (cachedBlock < 0) {
            return sequencerPersister.allocateNextBlock(name, BLOCK);
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
        throw new UnsupportedOperationException("Unsupported method");
    }

    @Override
    public int getValue() {
        return getBase();
    }

    public void setName(String name) {
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
        return "CassandraIntegerSequencer" +
                "[name=" + name +
                ", base=" + base +
                ", cachedBlock=" + (blockAllocator == null ? "null" : blockAllocator.getCachedBlock()) +
                ", offset=" + offset +
                ", lock=" + lock +
                ']';
    }

    class CachedBlockAllocator implements Runnable {
        private volatile int cachedBlock = -1;

        @Override
        public void run() {
            if (offset.get() >= HALF_BLOCK && cachedBlock < 0) {
                cachedBlock = sequencerPersister.allocateNextBlock(name, BLOCK);
                LOG.info("CachedBlockAllocator: {}, allocated block={}", name, cachedBlock);
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
