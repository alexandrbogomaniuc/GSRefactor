package com.abs.casino.gs.socket.mq;

import com.abs.casino.common.exception.CommonException;

public class ForceCreateDetailsException extends Exception {
    private final CommonException exception;

    public ForceCreateDetailsException(CommonException exception) {
        this.exception = exception;
    }

    public CommonException getException() {
        return exception;
    }
}
