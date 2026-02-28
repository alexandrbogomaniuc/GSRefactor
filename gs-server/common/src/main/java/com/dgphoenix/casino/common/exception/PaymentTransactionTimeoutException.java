package com.abs.casino.common.exception;

import com.dgphoenix.casino.common.exception.CommonException;

/**
 * Created by mic on 19.09.14.
 */
public class PaymentTransactionTimeoutException extends CommonException {
    public PaymentTransactionTimeoutException() {
    }

    public PaymentTransactionTimeoutException(String message) {
        super(message);
    }

    public PaymentTransactionTimeoutException(Throwable cause) {
        super(cause);
    }

    public PaymentTransactionTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }
}
