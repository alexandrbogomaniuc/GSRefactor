package com.abs.casino.common.exception;

import com.abs.casino.common.exception.CommonException;

/**
 * Created by ANGeL
 * Date: Sep 19, 2008
 * Time: 5:44:29 PM
 */
public class ServiceNotAvailableException extends CommonException {
    public ServiceNotAvailableException() {
    }

    public ServiceNotAvailableException(String message) {
        super(message);
    }

    public ServiceNotAvailableException(Throwable cause) {
        super(cause);
    }

    public ServiceNotAvailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
