package com.dgphoenix.casino.exceptions;

import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.gs.managers.payment.wallet.CWError;

/**
 * User: isirbis
 * Date: 30.09.14
 */
public class LoginErrorException extends CommonException {
    private final CWError error;

    public LoginErrorException(CWError code) {
        this.error = code;
    }

    public LoginErrorException(CWError code, String message) {
        super(message);
        this.error = code;
    }

    public LoginErrorException(CWError code, Throwable cause) {
        super(cause);
        this.error = code;
    }

    public LoginErrorException(CWError code, String message, Throwable cause) {
        super(message, cause);
        this.error = code;
    }

    public Integer getErrorCode() {
        return error.getCode();
    }

    public String getDescription() {
        return error.getDescription();
    }

    public CWError getError() {
        return error;
    }

    @Override
    public String toString() {
        return "CallApiException [" + "error=" + error +
                "base error=" + super.toString() +
                ']';
    }
}
