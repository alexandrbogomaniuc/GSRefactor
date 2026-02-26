package com.abs.casino.actions.api.response;

import com.dgphoenix.casino.actions.api.response.Response;

public class ErrorResponse extends Response {

    private static final String ERROR_RESULT = "ERROR";

    private int code;
    private String description;

    public ErrorResponse(int code, String description) {
        super(ERROR_RESULT);
        this.code = code;
        this.description = description;
    }
}
