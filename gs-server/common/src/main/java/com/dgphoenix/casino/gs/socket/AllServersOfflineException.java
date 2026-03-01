package com.abs.casino.gs.socket;

import com.abs.casino.common.exception.CommonException;

/**
 * User: flsh
 * Date: 28.11.2019.
 */
public class AllServersOfflineException extends CommonException {
    public AllServersOfflineException() {
    }

    public AllServersOfflineException(String message) {
        super(message);
    }

    public AllServersOfflineException(Throwable cause) {
        super(cause);
    }

    public AllServersOfflineException(String message, Throwable cause) {
        super(message, cause);
    }
}
