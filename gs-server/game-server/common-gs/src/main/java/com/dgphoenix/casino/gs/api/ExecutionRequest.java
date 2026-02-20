package com.dgphoenix.casino.gs.api;

import java.util.Date;
import java.util.concurrent.Callable;

public class ExecutionRequest {
    private final String key;
    private final Callable callable;
    private final long execServerId;

    public ExecutionRequest(Callable callable, long execServerId) {
        super();
        this.callable = callable;
        this.execServerId = execServerId;
        this.key = callable.getClass().getName() + "-" + execServerId + "-" + new Date().getTime();
    }

    public String getKey() {
        return key;
    }

    public Callable getCallable() {
        return callable;
    }

    public long getExecServerId() {
        return execServerId;
    }

}
