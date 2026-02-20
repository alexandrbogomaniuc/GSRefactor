package com.dgphoenix.casino.common.exception;

/**
 * Created by ANGeL
 * Date: Sep 19, 2008
 * Time: 5:44:29 PM
 */
public class ServerNotFoundException extends CommonException {
    public ServerNotFoundException() {
    }

    public ServerNotFoundException(String message) {
        super(message);
    }

    public ServerNotFoundException(Throwable cause) {
        super(cause);
    }

    public ServerNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}