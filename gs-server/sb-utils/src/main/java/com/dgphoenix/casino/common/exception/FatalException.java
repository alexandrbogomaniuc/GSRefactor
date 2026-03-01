package com.abs.casino.common.exception;

import com.abs.casino.common.exception.CommonException;

/**
 * Created by ANGeL
 * Date: Sep 19, 2008
 * Time: 1:01:28 PM
 */
public class FatalException extends CommonException {
    public FatalException() {
    }

    public FatalException(String message) {
        super(message);
    }

    public FatalException(Throwable cause) {
        super(cause);
    }

    public FatalException(String message, Throwable cause) {
        super(message, cause);
    }
}
