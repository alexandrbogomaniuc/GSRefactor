package com.abs.casino.common.exception;

import com.dgphoenix.casino.common.exception.AbstractSendAlertException;
/**
 * Used to indicate incorrect game session id.
 *
 * @author timur
 */
public class GameSessionNotFoundException extends AbstractSendAlertException {

    public GameSessionNotFoundException() {
    }

    public GameSessionNotFoundException(String message) {
        super(message);
    }

    public GameSessionNotFoundException(Throwable cause) {
        super(cause);
    }

    public GameSessionNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public GameSessionNotFoundException(String reason, String message) {
        super(reason + " :: " + message);
    }

    @Override
    public boolean isSendAlert() {
        return false;
    }
}
