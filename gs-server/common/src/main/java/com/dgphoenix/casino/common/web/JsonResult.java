package com.abs.casino.common.web;

import com.abs.casino.common.web.ResultType;

public class JsonResult {

    private final String message;
    private final ResultType result;

    private JsonResult(String message, ResultType result) {
        this.message = message;
        this.result = result;
    }

    public static JsonResult createSuccessResult(String message) {
        return new JsonResult(message, ResultType.OK);
    }

    public static JsonResult createErrorResult(String message) {
        return new JsonResult(message, ResultType.ERROR);
    }

    public String getMessage() {
        return message;
    }

    public String getResult() {
        return result.toString().toLowerCase();
    }

}
