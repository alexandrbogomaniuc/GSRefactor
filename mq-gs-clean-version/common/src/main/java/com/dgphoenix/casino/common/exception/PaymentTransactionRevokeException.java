package com.dgphoenix.casino.common.exception;

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
