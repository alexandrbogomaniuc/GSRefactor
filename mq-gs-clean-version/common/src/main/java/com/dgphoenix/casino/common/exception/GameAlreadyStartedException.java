package com.dgphoenix.casino.common.exception;

/**
 * Created
 * Date: Dec 8, 2008
 * Time: 2:12:45 PM
 */
public class GameAlreadyStartedException extends CommonException {
    public GameAlreadyStartedException() {
    }

    public GameAlreadyStartedException(String message) {
        super(message);
    }

    public GameAlreadyStartedException(Throwable cause) {
        super(cause);
    }

    public GameAlreadyStartedException(String message, Throwable cause) {
        super(message, cause);
    }
}
