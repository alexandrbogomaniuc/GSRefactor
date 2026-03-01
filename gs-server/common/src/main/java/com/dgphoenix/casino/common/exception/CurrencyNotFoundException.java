package com.abs.casino.common.exception;

import com.abs.casino.common.exception.CommonException;

/**
 * Created by ANGeL
 * Date: Sep 19, 2008
 * Time: 5:44:29 PM
 */
public class CurrencyNotFoundException extends CommonException {
    public CurrencyNotFoundException() {
    }

    public CurrencyNotFoundException(String message) {
        super(message);
    }

    public CurrencyNotFoundException(Throwable cause) {
        super(cause);
    }

    public CurrencyNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
