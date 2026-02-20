package com.dgphoenix.casino.common.util;

import org.apache.log4j.Logger;

import java.util.List;
import java.util.concurrent.*;

public class ExecutorUtils {
    private static final Logger LOG = Logger.getLogger(ExecutorUtils.class);
    private static final ExecutorService scheduler = Executors.newCachedThreadPool();

    public static void shutdownService(final String invoker, final ExecutorService service, final long timeOutInMillis) {
        scheduler.execute(new Runnable() {
            @Override
            public void run() {
                shutdownServiceSynchronously(invoker, service, timeOutInMillis);
            }
        });
    }

    public static void shutdownServiceSynchronously(String invoker, ExecutorService service, long timeOutInMillis) {
        LOG.info(invoker + " shutdown started");
        service.shutdown();
        try {
            service.awaitTermination(timeOutInMillis, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            LOG.error("destroy await termination was interrupted!!!", e);
        }
        if (!service.isTerminated()) {
            if(service instanceof ScheduledThreadPoolExecutor) {
                final ScheduledThreadPoolExecutor executor = (ScheduledThreadPoolExecutor) service;
                LOG.debug("shutdownServiceSynchronously: " + invoker + " taskCount=" + executor.getTaskCount() +
                        ", activeThreadsCount=" + executor.getActiveCount());
            }
            try {
                LOG.info("Service still running, immediate shutdown");
                final List<Runnable> unfinished = service.shutdownNow();
                LOG.info("Unfinished tasks: " + invoker + "=" + unfinished.size());
                for (Runnable runnable : unfinished) {
                    LOG.debug("shutdownServiceSynchronously, unfinished=" + invoker + ": " + runnable.toString());
                }
            } catch(Throwable e) {
                System.err.println("Cannot shutdown service: " + invoker + ": " + e);
                e.printStackTrace();
            }
        }
        LOG.info(invoker + " shutdown finished");
    }



    public static void finalizeExecutor() {
        LOG.info("finalizeExecutor started");
        shutdownServiceSynchronously("ExecutorUtils", scheduler, 5100);
        LOG.info("finalizeExecutor finished");
    }
}
