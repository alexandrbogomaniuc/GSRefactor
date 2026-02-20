package com.dgphoenix.casino.common.exception;

/**
 * Created
 * Date: 19.11.2008
 * Time: 17:47:45
 */
public class JackPotCannotBeWonException extends CommonException {
    public JackPotCannotBeWonException() {
    }

    public JackPotCannotBeWonException(String message) {
        super(message);
    }

    public JackPotCannotBeWonException(Throwable cause) {
        super(cause);
    }

    public JackPotCannotBeWonException(String message, Throwable cause) {
        super(message, cause);
    }

    public JackPotCannotBeWonException(String reason, String message) {
        super(reason + " :: " + message);
    }
}