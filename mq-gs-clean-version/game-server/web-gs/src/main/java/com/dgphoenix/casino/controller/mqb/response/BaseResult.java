package com.dgphoenix.casino.controller.mqb.response;

public class BaseResult extends Result{
    private String message;

    public BaseResult(String result, String message) {
        super(result);
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
