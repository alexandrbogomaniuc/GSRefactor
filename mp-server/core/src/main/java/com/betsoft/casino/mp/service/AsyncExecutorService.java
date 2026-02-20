package com.betsoft.casino.mp.service;

import com.betsoft.casino.mp.model.IAsyncExecutorService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.util.concurrent.*;

@Service
public class AsyncExecutorService implements IAsyncExecutorService {

    private final ExecutorService executor;
    private static final Logger logger = LogManager.getLogger(AsyncExecutorService.class);
    private int queueCapacity;

    public AsyncExecutorService(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, int queueCapacity, RejectedExecutionHandler handler) {
        this.queueCapacity = queueCapacity;
        BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<>(this.queueCapacity);
        this.executor = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, handler);
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        if (task == null) {
            logger.error("submit: invalid task has been submitted.");
            throw new RuntimeException("submit: invalid task has been submitted.");
        }
        checkCurrentQueueSize();
        Callable<T> wrappedTask = () -> {
            long startTime = System.currentTimeMillis();
            try {
                return task.call();
            } finally {
                long endTime = System.currentTimeMillis();
                logger.info("Task executed in {} ms", (endTime - startTime));
            }
        };
        return executor.submit(wrappedTask);
    }

    @Override
    public void execute(Runnable task) {
        if (task == null) {
            logger.error("execute: invalid task has been submitted.");
            return;
        }
        checkCurrentQueueSize();
        executor.execute(() -> {
            long startTime = System.currentTimeMillis();
            try {
                task.run();
            } finally {
                long endTime = System.currentTimeMillis();
                logger.info("Runnable executed in {} ms", (endTime - startTime));
            }
        });
    }

    // Check the current queue size
    private void checkCurrentQueueSize() {
        int currentQueueSize = ((ThreadPoolExecutor) executor).getQueue().size();
        if (this.queueCapacity / 2 < currentQueueSize) {
            logger.warn("IMPORTANT: Current queue size before submitting takes over 50% of total queue size. " +
                    "Current queue size: {}. Total queue capacity: {}", currentQueueSize, this.queueCapacity);
        } else {
            logger.debug("Current queue size before submitting: {}", currentQueueSize);
        }
    }

    @Override
    public void shutdown() {
        executor.shutdown();
    }

    @PreDestroy
    public void cleanUp() {
        logger.info("Initiating graceful shutdown of executor service.");
        executor.shutdown();
        try {
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                logger.warn("Executor did not terminate in the specified time.");
                executor.shutdownNow();
                if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                    logger.error("Executor did not terminate after shutdownNow.");
                }
            }
        } catch (InterruptedException ie) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        logger.info("Executor service shutdown completed.");
    }

    public ExecutorService getExecutor() {
        return executor;
    }

    public int getQueueCapacity() {
        return queueCapacity;
    }
}
