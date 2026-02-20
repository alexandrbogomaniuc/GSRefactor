package com.dgphoenix.casino.common.web;

import com.dgphoenix.casino.common.util.ExecutorUtils;
import com.dgphoenix.casino.common.web.statistics.StatisticsManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.*;

/**
 * User: flsh
 * Date: 15.03.19.
 */
public class SharedServletExecutorService {
    private static final Logger LOG = LogManager.getLogger(SharedServletExecutorService.class);

    private final ThreadPoolExecutor executor = new ThreadPoolExecutor(50, 500, 60L, TimeUnit.SECONDS,
            new SynchronousQueue<>());
    private ExecutorService wrappedExecutor = Executors.unconfigurableExecutorService(executor);
    private boolean initialized = false;

    public SharedServletExecutorService() {
    }

    @PostConstruct
    private void init() {
        StatisticsManager.getInstance().
                registerStatisticsGetter("SharedServletExecutorService: ", () -> {
                    StringBuilder sb = new StringBuilder();
                    sb.append(", completedTaskCount=").append(executor.getCompletedTaskCount());
                    sb.append(", activeThreadsCount=").append(executor.getActiveCount());
                    sb.append(", maxThreadsCount=").append(executor.getMaximumPoolSize());
                    sb.append(", currentPoolSize=").append(executor.getPoolSize());
                    sb.append(", largestPoolSize=").append(executor.getLargestPoolSize());
                    return sb.toString();
                });
        initialized = true;
    }

    @PreDestroy
    private void shutdown() {
        initialized = false;
        ExecutorUtils.shutdownService("SharedServletExecutorService", executor, 5000);
        LOG.info("All GameServlets destroyed");
    }

    public ExecutorService getExecutor() {
        return wrappedExecutor;
    }

    public boolean isInitialized() {
        return initialized;
    }
}
