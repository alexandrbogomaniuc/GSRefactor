package com.abs.casino.common.exception;

import com.dgphoenix.casino.common.exception.CommonException;

/**
 * Created
 * Date: 19.11.2008
 * Time: 17:47:45
 */
public class GameException extends CommonException {
    public GameException() {
    }

    public GameException(String message) {
        super(message);
    }

    public GameException(Throwable cause) {
        super(cause);
    }

    public GameException(String message, Throwable cause) {
        super(message, cause);
    }

    public GameException(String reason, String message) {
        super(reason + " :: " + message);
    }
}
