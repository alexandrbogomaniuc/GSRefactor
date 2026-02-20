package com.dgphoenix.casino.common.exception;

/**
 * User: flsh
 * Date: Nov 12, 2009
 */
public abstract class AbstractSendAlertException extends CommonException {
    public AbstractSendAlertException() {
    }

    public AbstractSendAlertException(String message) {
        super(message);
    }

    public AbstractSendAlertException(Throwable cause) {
        super(cause);
    }

    public AbstractSendAlertException(String message, Throwable cause) {
        super(message, cause);
    }

    public AbstractSendAlertException(String reason, String message) {
        super(reason + " :: " + message);
    }

    public abstract boolean isSendAlert();

}
