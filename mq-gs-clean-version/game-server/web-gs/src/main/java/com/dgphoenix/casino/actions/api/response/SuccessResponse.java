package com.dgphoenix.casino.actions.api.response;

public class SuccessResponse extends Response {

    private static final String SUCCESS_RESULT = "OK";

    protected SuccessResponse() {
        super(SUCCESS_RESULT);
    }
}
