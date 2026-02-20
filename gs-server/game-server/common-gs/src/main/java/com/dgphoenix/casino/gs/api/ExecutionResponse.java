package com.dgphoenix.casino.gs.api;

public class ExecutionResponse {
    private final Object result;
    private final String exception;

    public ExecutionResponse(Object result, String exception) {
        super();
        this.result = result;
        this.exception = exception;
    }

    public Object getResult() {
        return result;
    }

    public String getException() {
        return exception;
    }

}
