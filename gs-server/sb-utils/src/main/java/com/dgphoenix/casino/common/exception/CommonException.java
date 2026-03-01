package com.abs.casino.common.exception;

/**
 * Created by ANGeL
 * Date: Sep 16, 2008
 * Time: 5:08:35 PM
 */
public class CommonException extends Exception {
    public CommonException() {
        super();
    }

    public CommonException(String message) {
        super(message);
    }

    public CommonException(Throwable cause) {
        super(cause);
    }

    public CommonException(String message, Throwable cause) {
        super(message, cause);
    }
}
