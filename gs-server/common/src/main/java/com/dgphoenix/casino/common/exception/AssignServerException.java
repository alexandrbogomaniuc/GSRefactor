package com.dgphoenix.casino.common.exception;

/**
 * User: plastical
 * Date: 24.02.2010
 */
public class AssignServerException extends CommonException {
    public AssignServerException() {
    }

    public AssignServerException(String message) {
        super(message);
    }

    public AssignServerException(Throwable cause) {
        super(cause);
    }

    public AssignServerException(String message, Throwable cause) {
        super(message, cause);
    }

    public AssignServerException(String reason, String message) {
        super(reason + " :: " + message);
    }
}
