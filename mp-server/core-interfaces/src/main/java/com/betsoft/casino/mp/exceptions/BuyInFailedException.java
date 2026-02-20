package com.betsoft.casino.mp.exceptions;

import com.dgphoenix.casino.common.exception.CommonException;

/**
 * User: flsh
 * Date: 21.09.18.
 */
public class BuyInFailedException extends CommonException {
    private boolean fatal;
    private int errorCode;
    private boolean playerAlreadySitOut;

    public BuyInFailedException(String message, Throwable cause, boolean fatal, int errorCode) {
        super(message, cause);
        this.fatal = fatal;
        this.errorCode = errorCode;
    }

    public BuyInFailedException(boolean fatal) {
        super();
        this.fatal = fatal;
    }

    public BuyInFailedException(String message, boolean fatal) {
        super(message);
        this.fatal = fatal;
    }

    public BuyInFailedException(String message, boolean fatal, boolean playerAlreadySitOut) {
        this(message, fatal);
        this.playerAlreadySitOut = playerAlreadySitOut;
    }

    public BuyInFailedException(String message, boolean fatal, int errorCode) {
        super(message);
        this.fatal = fatal;
        this.errorCode = errorCode;
    }

    public BuyInFailedException(Throwable cause, boolean fatal) {
        super(cause);
        this.fatal = fatal;
    }

    public BuyInFailedException(String message, Throwable cause, boolean fatal) {
        super(message, cause);
        this.fatal = fatal;
    }

    public boolean isFatal() {
        return fatal;
    }

    public boolean isPlayerAlreadySitOut() {
        return playerAlreadySitOut;
    }

    public int getErrorCode() {
        return errorCode;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("BuyInFailedException [");
        sb.append("fatal=").append(fatal);
        sb.append(", errorCode=").append(errorCode);
        sb.append(", message='").append(getMessage()).append("'");
        sb.append(']');
        return sb.toString();
    }
}
