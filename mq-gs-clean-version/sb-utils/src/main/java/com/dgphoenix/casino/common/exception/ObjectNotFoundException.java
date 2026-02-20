package com.dgphoenix.casino.common.exception;

/**
 * Created by ANGeL
 * Date: Sep 19, 2008
 * Time: 5:44:29 PM
 */
public class ObjectNotFoundException extends CommonException {
    public ObjectNotFoundException() {
    }

    public ObjectNotFoundException(String message) {
        super(message);
    }

    public ObjectNotFoundException(Throwable cause) {
        super(cause);
    }

    public ObjectNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
