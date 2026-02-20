package com.dgphoenix.casino.common.exception;

/**
 * Created flash
 */
public class CannotLockException extends CommonException {
    public CannotLockException() {
    }

    public CannotLockException(String message) {
        super(message);
    }

    public CannotLockException(Throwable cause) {
        super(cause);
    }

    public CannotLockException(String message, Throwable cause) {
        super(message, cause);
    }

    public CannotLockException(String reason, String message) {
        super(reason + " :: " + message);
    }
}