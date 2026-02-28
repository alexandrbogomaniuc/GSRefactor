package com.abs.casino.gs.socket;

/**
 * User: flsh
 * Date: 31.01.19.
 */
public class NotCriticalWalletException extends Exception {
    public NotCriticalWalletException() {
    }

    public NotCriticalWalletException(String message) {
        super(message);
    }

    public NotCriticalWalletException(String message, Throwable cause) {
        super(message, cause);
    }
}
