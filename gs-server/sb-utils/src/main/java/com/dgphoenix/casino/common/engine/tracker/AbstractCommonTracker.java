package com.abs.casino.common.engine.tracker;

import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.util.ExecutorUtils;
import com.dgphoenix.casino.common.web.statistics.IStatisticsGetter;
import com.dgphoenix.casino.common.web.statistics.StatisticsManager;
import org.apache.log4j.Logger;

import java.util.concurrent.*;

/**
 * User: plastical
 * Date: 20.05.2010
 */
public abstract class AbstractCommonTracker<K, T extends AbstractCommonTrackingTask> implements ICommonTracker<K> {
    private static final int DEFAULT_THREAD_POOL_SIZE = 15;
    protected static final long TERMINATION_TIMEOUT = 5000L;

    private volatile boolean initialized;
    protected final ConcurrentMap<K, Future<?>> keys;
    protected final ScheduledExecutorService threadPool;

    protected AbstractCommonTracker() {
        this.initialized = false;
        this.keys = new ConcurrentHashMap<K, Future<?>>(64);
        this.threadPool = createThreadPoolExecutor();
    }

    public void startup() {
        if (!isInitialized()) {
            getLog().info("startup started");
            this.initialized = true;
            chargeTasks();
            getLog().info("startup completed");
            StatisticsManager.getInstance().
                    registerStatisticsGetter(this.getClass().getSimpleName() + ": ", new IStatisticsGetter() {
                        public String getStatistics() {
                            StringBuilder sb = new StringBuilder();
                            sb.append("taskCount=").append(getTaskCount());
                            sb.append(", activeThreadsCount=").append(getActiveThreadsCount());
                            sb.append(", maxThreadsCount=").append(getMaxThreadsCount());
                            sb.append(", currentPoolSize=").append(getCurrentPoolSize());
                            sb.append(", completedTaskCount=").append(getCompletedTaskCount());
                            return sb.toString();
                        }
                    });
        }
    }

    public void shutdown() {
        if (isInitialized()) {
            getLog().info(":shutdown started");
            this.initialized = false;
            ExecutorUtils.shutdownService(this.getClass().getSimpleName(), threadPool, TERMINATION_TIMEOUT);
            getLog().info("shutdown completed");
        }
    }

    public void addTask(K key) {
        addTask(key, 0);
    }

    public void addTask(K key, long delayInMillis) {
        addTask(key, null, delayInMillis);
    }

    public void addTask(K key, T task, long delayInMillis) {
        if (isInitialized()) {
            addNewTask(key, task, delayInMillis);
        } else {
            getLog().warn("addTask task for key:" + key + " skipped, tracker is not initialized");
        }
    }

    protected abstract Logger getLog();

    protected ScheduledExecutorService createThreadPoolExecutor() {
        final ScheduledThreadPoolExecutor service = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(5);
        service.setMaximumPoolSize(getTrackerThreadPoolSize());
        return service;
    }

    protected void addNewTask(K key) {
        addNewTask(key, 0);
    }

    protected void addNewTask(K key, long delayInMillis) {
        addNewTask(key, null, delayInMillis);
    }

    protected void addNewTask(K key, T task, long delayInMillis) {
        if (key == null) {
            getLog().warn("addNewTask task for key: " + key + " is invalid");
            return;
        }

        if (containsKey(key)) {
            getLog().warn("addNewTask task for key:" + key + " already registered");
            return;
        }

        if (task == null) {
            task = createNewTask(key);
        }
        if (task != null) {
            if (delayInMillis <= 0) {
                keys.putIfAbsent(key, threadPool.submit(task));
            } else {
                keys.putIfAbsent(key, threadPool.schedule(task, delayInMillis, TimeUnit.MILLISECONDS));
            }
            getLog().debug("addNewTask task for key: " + key + " was registered");
        }
    }

    public void cancelTask(K key) {
        if (key == null) {
            getLog().warn("cancelTask task for key: " + key + " is invalid");
            return;
        }

        if (!containsKey(key)) {
            getLog().warn("cancelTask task for key: " + key + " is not " + "registered");
            return;
        }

        Future<?> future = keys.get(key);
        future.cancel(true);
        remove(key);
        if(getLog().isDebugEnabled()) {
            getLog().debug("cancelTask task for key:" + key + " was cancelled");
        }
    }

    public <R> R executeImmediately(Callable<R> task) throws CommonException {
        Future<R> future = threadPool.submit(task);
        try {
            return future.get();
        } catch (Exception e) {
            future.cancel(true);
            throw new CommonException(e);
        }
    }

    public <R> R executeImmediately(Callable<R> task, long timeoutMillis) throws CommonException {
        Future<R> future = threadPool.submit(task);
        try {
            return future.get(timeoutMillis, TimeUnit.MILLISECONDS);
        } catch (Throwable e) {
            future.cancel(true);
            throw new CommonException(e);
        }
    }

    public <R> R executeImmediately(Runnable task, R result) throws CommonException {
        Future<R> future = threadPool.submit(task, result);
        try {
            return future.get();
        } catch (Exception e) {
            future.cancel(true);
            throw new CommonException(e);
        }
    }

    public <R> R executeImmediately(Runnable task, R result, long timeoutMillis) throws CommonException {
        Future<R> future = threadPool.submit(task, result);
        try {
            return future.get(timeoutMillis, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            future.cancel(true);
            throw new CommonException(e);
        }
    }

    public Object executeImmediately(Runnable task) throws CommonException {
        return executeImmediately(task, null);
    }

    public Object executeImmediately(Runnable task, long timeoutMillis) throws CommonException {
        return executeImmediately(task, null, timeoutMillis);
    }

    protected ScheduledThreadPoolExecutor getThreadPool() {
        return (ScheduledThreadPoolExecutor) threadPool;
    }

    public long getTaskCount() {
        return getThreadPool().getTaskCount();
    }

    public int getActiveThreadsCount() {
        return getThreadPool().getActiveCount();
    }

    public int getMaxThreadsCount() {
        return getThreadPool().getMaximumPoolSize();
    }

    public int getCurrentPoolSize() {
        return getThreadPool().getPoolSize();
    }

    public long getCompletedTaskCount() {
        return getThreadPool().getCompletedTaskCount();
    }

    public int getTrackerThreadPoolSize() {
        int poolSize;
        try {
            poolSize = getThreadPoolSize();
        } catch (CommonException e) {
            poolSize = DEFAULT_THREAD_POOL_SIZE;
            getLog().error("getThreadPoolSize error:", e);
        }

        return poolSize;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void remove(K key) {
        keys.remove(key);
/*
        synchronized (keys) {
            if (containsKey(key)) {
                keys.remove(key);
            }
        }
*/
    }

    public boolean containsKey(K key) {
        return keys.containsKey(key);
    }

    protected abstract int getThreadPoolSize() throws CommonException;

    protected abstract void chargeTasks();

    protected abstract T createNewTask(K key);
}
