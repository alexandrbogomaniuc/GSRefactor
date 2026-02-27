package com.dgphoenix.casino.common.util;

import com.dgphoenix.casino.common.config.UtilsApplicationContextHelper;
import com.abs.casino.common.configuration.IGameServerConfiguration;
import com.dgphoenix.casino.common.web.statistics.StatisticsManager;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class CommonExecutorService implements ScheduledExecutorService {
    private static final long MAX_TIME_OUT_MILLIS = TimeUnit.SECONDS.toMillis(90);
    private static final int DEFAULT_COMMON_EXECUTOR_SERVICE_POOL_SIZE = 10;

    private final int poolSize;
    private final ConcurrentHashMap<String, AtomicInteger> executionTaskCounter = new ConcurrentHashMap<>();
    private ScheduledThreadPoolExecutor pool;

    public CommonExecutorService() {
        IGameServerConfiguration gameServerConfiguration1 = UtilsApplicationContextHelper.getApplicationContext().getBean("gameServerConfiguration", IGameServerConfiguration.class);
        String poolSizeParam = gameServerConfiguration1.getStringPropertySilent("COMMON_EXECUTOR_SERVICE_POOL_SIZE");
        poolSize = poolSizeParam != null ? Integer.parseInt(poolSizeParam) : DEFAULT_COMMON_EXECUTOR_SERVICE_POOL_SIZE;
    }

    @PostConstruct
    private void init() {
        pool = new ScheduledThreadPoolExecutor(poolSize);
        StatisticsManager.getInstance().registerStatisticsGetter(this.getClass().getSimpleName(),
                () -> "poolSize=" + pool.getPoolSize()
                        + ", corePoolSize=" + pool.getCorePoolSize()
                        + ", largestPoolSize=" + pool.getLargestPoolSize()
                        + ", maximumPoolSize=" + pool.getMaximumPoolSize()
                        + ", completedTaskCount=" + pool.getCompletedTaskCount()
                        + ", taskCount=" + pool.getTaskCount()
                        + ", activeCount=" + pool.getActiveCount()
                        + ", executionTaskCounter= " + executionTaskCounter);
    }

    @Override
    public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
        executionTaskCounter.computeIfAbsent(command.getClass().getSimpleName(), k -> new AtomicInteger(0)).incrementAndGet();
        return pool.schedule(command, delay, unit);
    }

    @Override
    public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
        executionTaskCounter.computeIfAbsent(callable.getClass().getSimpleName(), k -> new AtomicInteger(0)).incrementAndGet();
        return pool.schedule(callable, delay, unit);
    }

    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
        executionTaskCounter.computeIfAbsent(command.getClass().getSimpleName(), k -> new AtomicInteger(0)).incrementAndGet();
        return pool.scheduleAtFixedRate(command, initialDelay, period, unit);
    }

    @Override
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
        executionTaskCounter.computeIfAbsent(command.getClass().getSimpleName(), k -> new AtomicInteger(0)).incrementAndGet();
        return pool.scheduleWithFixedDelay(command, initialDelay, delay, unit);
    }

    @Override
    @PreDestroy
    public void shutdown() {
        ExecutorUtils.shutdownService(this.getClass().getSimpleName(), pool, MAX_TIME_OUT_MILLIS);
    }

    @Override
    public List<Runnable> shutdownNow() {
        return pool.shutdownNow();
    }

    @Override
    public boolean isShutdown() {
        return pool.isShutdown();
    }

    @Override
    public boolean isTerminated() {
        return pool.isTerminated();
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return pool.awaitTermination(timeout, unit);
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        return pool.submit(task);
    }

    @Override
    public <T> Future<T> submit(Runnable task, T result) {
        return pool.submit(task, result);
    }

    @Override
    public Future<?> submit(Runnable task) {
        return pool.submit(task);
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
        return pool.invokeAll(tasks);
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
        return pool.invokeAll(tasks, timeout, unit);
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
        return pool.invokeAny(tasks);
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return pool.invokeAny(tasks, timeout, unit);
    }

    @Override
    public void execute(Runnable command) {
        pool.execute(command);
    }

}
