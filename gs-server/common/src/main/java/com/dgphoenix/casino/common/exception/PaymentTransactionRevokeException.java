package com.abs.casino.common.exception;

import com.abs.casino.common.exception.CommonException;

public class PaymentTransactionRevokeException extends CommonException {
    public PaymentTransactionRevokeException() {
    }

    public PaymentTransactionRevokeException(String message) {
        super(message);
    }

    public PaymentTransactionRevokeException(Throwable cause) {
        super(cause);
    }

    public PaymentTransactionRevokeException(String message, Throwable cause) {
        super(message, cause);
    }
}
