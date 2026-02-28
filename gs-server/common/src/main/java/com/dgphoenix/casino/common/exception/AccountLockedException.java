package com.abs.casino.common.exception;

/**
 * User: plastical
 * Date: 11.05.2010
 */
public class AccountLockedException extends AccountException {
    public AccountLockedException() {
    }

    public AccountLockedException(String message) {
        super(message);
    }

    public AccountLockedException(Throwable cause) {
        super(cause);
    }

    public AccountLockedException(String message, Throwable cause) {
        super(message, cause);
    }
}
