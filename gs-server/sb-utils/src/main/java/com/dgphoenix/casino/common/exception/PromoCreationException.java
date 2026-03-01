package com.abs.casino.common.exception;

import com.abs.casino.common.exception.CommonException;

public class PromoCreationException extends CommonException {
    public PromoCreationException() {
        super();
    }

    public PromoCreationException(String message) {
        super(message);
    }

    public PromoCreationException(Throwable cause) {
        super(cause);
    }

    public PromoCreationException(String message, Throwable cause) {
        super(message, cause);
    }
}
