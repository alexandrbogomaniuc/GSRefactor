package com.abs.casino.gs.socket.async;

public interface ICallbacksExecutor {

    void execute(Runnable callbackTask);

    int getCallbacksExecutorRequestsCount();

    int getCallbacksExecutorHandlersCount();

    default void shutdown() {

    }

    default String getStatistic() {
        return "";
    }
}
