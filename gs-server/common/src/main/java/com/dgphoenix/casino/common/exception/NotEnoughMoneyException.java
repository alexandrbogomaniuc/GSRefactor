package com.dgphoenix.casino.common.exception;

/**
 * Created by ANGeL
 * Date: Oct 20, 2008
 * Time: 4:01:10 PM
 */
public class NotEnoughMoneyException extends CommonException {
    public NotEnoughMoneyException() {
    }

    public NotEnoughMoneyException(String message) {
        super(message);
    }

    public NotEnoughMoneyException(Throwable cause) {
        super(cause);
    }

    public NotEnoughMoneyException(String message, Throwable cause) {
        super(message, cause);
    }
}
