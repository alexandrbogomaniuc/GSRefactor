package com.dgphoenix.casino.common.exception;

/**
 * Created by ANGeL
 * Date: Sep 25, 2008
 * Time: 1:36:46 PM
 */
public class ObjectAlreadyExistsException extends CommonException {
    public ObjectAlreadyExistsException() {
    }

    public ObjectAlreadyExistsException(String message) {
        super(message);
    }

    public ObjectAlreadyExistsException(Throwable cause) {
        super(cause);
    }

    public ObjectAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}
