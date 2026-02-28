package com.abs.casino.common.exception;

import com.dgphoenix.casino.common.exception.CommonException;

/**
 * Created
 * Date: 19.11.2008
 * Time: 17:47:45
 */
public class GameSessionNotStartedException extends CommonException {
    public GameSessionNotStartedException() {
    }

    public GameSessionNotStartedException(String message) {
        super(message);
    }

    public GameSessionNotStartedException(Throwable cause) {
        super(cause);
    }

    public GameSessionNotStartedException(String message, Throwable cause) {
        super(message, cause);
    }

    public GameSessionNotStartedException(String reason, String message) {
        super(reason + " :: " + message);
    }
}
