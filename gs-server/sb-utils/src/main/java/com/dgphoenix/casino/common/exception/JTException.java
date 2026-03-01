package com.abs.casino.common.exception;

import com.abs.casino.common.exception.CommonException;

/**
 * Created by ANGeL
 * Date: Oct 15, 2008
 * Time: 4:31:57 PM
 */
public class JTException extends CommonException {
    public JTException() {
    }

    public JTException(String message) {
        super(message);
    }

    public JTException(Throwable cause) {
        super(cause);
    }

    public JTException(String message, Throwable cause) {
        super(message, cause);
    }
}
