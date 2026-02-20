package com.dgphoenix.casino.common.exception;

/**
 * User: plastical
 * Date: 24.02.2010
 */
public class WebServiceException extends CommonException {
    public WebServiceException(String message) {
        super(message);
    }

    public WebServiceException(Throwable cause) {
        super(cause);
    }

    public WebServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public WebServiceException() {
    }
}
