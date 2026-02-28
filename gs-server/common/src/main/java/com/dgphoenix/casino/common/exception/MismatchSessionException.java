package com.abs.casino.common.exception;

import com.abs.casino.common.exception.ObjectNotFoundException;

/**
 * Created by grien on 23.07.15.
 */
public class MismatchSessionException extends ObjectNotFoundException {
    public MismatchSessionException() {
    }

    public MismatchSessionException(String message) {
        super(message);
    }

    public MismatchSessionException(Throwable cause) {
        super(cause);
    }

    public MismatchSessionException(String message, Throwable cause) {
        super(message, cause);
    }
}
