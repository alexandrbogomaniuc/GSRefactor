package com.dgphoenix.casino.common.exception;

/**
 * Created flash
 */
public class BadArgumentException extends CommonException {
    public BadArgumentException() {
    }

    public BadArgumentException(String message) {
        super(message);
    }

    public BadArgumentException(Throwable cause) {
        super(cause);
    }

    public BadArgumentException(String message, Throwable cause) {
        super(message, cause);
    }

    public BadArgumentException(String reason, String message) {
        super(reason + " :: " + message);
    }
}