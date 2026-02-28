package com.abs.casino.common.exception;

import com.dgphoenix.casino.common.exception.CommonException;

/**
 * Created by ANGeL
 * Date: Oct 13, 2008
 * Time: 12:52:45 PM
 */
public class AlreadyExistsException extends CommonException {
    public AlreadyExistsException() {
    }

    public AlreadyExistsException(String message) {
        super(message);
    }

    public AlreadyExistsException(Throwable cause) {
        super(cause);
    }

    public AlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}
