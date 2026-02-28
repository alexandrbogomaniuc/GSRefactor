package com.abs.casino.common.exception;

public class FRBException extends BonusException {

    private String errorCode;

    public FRBException(String message) {
        super(message);
    }

    public FRBException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public FRBException(Throwable cause) {
        super(cause);
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }


}
