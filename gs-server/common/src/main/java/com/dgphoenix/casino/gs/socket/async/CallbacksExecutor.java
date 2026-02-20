package com.dgphoenix.casino.gs.socket.async;

import com.dgphoenix.casino.common.util.ExecutorUtils;

import javax.annotation.PreDestroy;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class CallbacksExecutor implements ICallbacksExecutor {
    private final LinkedBlockingQueue<Runnable> workQueue;
    private final ThreadPoolExecutor callbacksExecutor;

    public CallbacksExecutor(int coreThreadsCount, int maxThreadsCount, long keepThreadsAliveTime) {
        workQueue = new LinkedBlockingQueue<>();
        callbacksExecutor = new ThreadPoolExecutor(coreThreadsCount, maxThreadsCount,
                keepThreadsAliveTime, TimeUnit.MILLISECONDS, workQueue);
    }

    @Override
    public void execute(Runnable callbackTask) {
        callbacksExecutor.execute(callbackTask);
    }

    @Override
    @PreDestroy
    public void shutdown() {
        ExecutorUtils.shutdownService(getClass().getSimpleName(), callbacksExecutor, 2000);
    }

    @Override
    public String getStatistic() {
        return getClass().getSimpleName() + ": callbacks in queue = " + workQueue.size() +
                ", callbacks executors count = " + callbacksExecutor.getPoolSize() +
                ", active executors handlers = " + callbacksExecutor.getActiveCount();
    }

    @Override
    public int getCallbacksExecutorRequestsCount() {
        return workQueue.size();
    }

    @Override
    public int getCallbacksExecutorHandlersCount() {
        return callbacksExecutor.getPoolSize();
    }
}
