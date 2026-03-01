package com.abs.casino.common.exception;

import com.abs.casino.common.exception.CommonException;

/**
 * User: plastical
 * Date: 11.05.2010
 */
public class AccountException extends CommonException {
    public AccountException() {
    }

    public AccountException(String message) {
        super(message);
    }

    public AccountException(Throwable cause) {
        super(cause);
    }

    public AccountException(String message, Throwable cause) {
        super(message, cause);
    }
}
