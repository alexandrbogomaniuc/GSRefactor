package com.abs.casino.common.exception;

import com.abs.casino.common.exception.CommonException;

/**
 * Created by ANGeL
 * Date: Sep 16, 2008
 * Time: 5:09:09 PM
 */
public class DBException extends CommonException {
    public DBException() {
    }

    public DBException(String message) {
        super(message);
    }

    public DBException(Throwable cause) {
        super(cause);
    }

    public DBException(String message, Throwable cause) {
        super(message, cause);
    }
}
