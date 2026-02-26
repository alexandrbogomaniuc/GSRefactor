package com.abs.casino.actions.api.response;

import com.dgphoenix.casino.actions.api.response.Response;

public class SuccessResponse extends Response {

    private static final String SUCCESS_RESULT = "OK";

    public SuccessResponse() {
        super(SUCCESS_RESULT);
    }
}
