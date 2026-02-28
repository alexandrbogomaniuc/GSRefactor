package com.abs.casino.common.exception;

import com.dgphoenix.casino.common.exception.CommonException;

/**
 * Created
 * Date: Dec 4, 2008
 * Time: 10:41:48 PM
 */
public class PlayerHasBetsException extends CommonException {
    public PlayerHasBetsException() {
    }

    public PlayerHasBetsException(String message) {
        super(message);
    }

    public PlayerHasBetsException(Throwable cause) {
        super(cause);
    }

    public PlayerHasBetsException(String message, Throwable cause) {
        super(message, cause);
    }
}
