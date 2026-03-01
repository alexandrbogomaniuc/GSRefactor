package com.abs.casino.common.exception;

import com.abs.casino.common.exception.CommonException;

/**
 * Created by inter on 23.04.15.
 */



public class ExtendedCommonException extends CommonException {

    protected ErrorInfo internalErrorInfo;

    public ErrorInfo getErrorInfo() {
        return internalErrorInfo;
    }

    public void setErrorInfo( ErrorInfo error ) {
        internalErrorInfo = error;
    }

    public ExtendedCommonException() {
        super();
    }

    public ExtendedCommonException(String message) {
        super(message);
    }

    public ExtendedCommonException(Throwable cause) {
        super(cause);
    }

    public ExtendedCommonException(String message, Throwable cause) {
        super(message, cause);
    }

    public ExtendedCommonException(ErrorInfo errorInfo) {
        super();
        setErrorInfo(errorInfo);
    }

    public ExtendedCommonException(String message, ErrorInfo errorInfo) {
        super(message);
        setErrorInfo(errorInfo);
    }

    public ExtendedCommonException(Throwable cause, ErrorInfo errorInfo) {
        super(cause);
        setErrorInfo(errorInfo);
    }

    public ExtendedCommonException(String message, Throwable cause, ErrorInfo errorInfo) {
        super(message, cause);
        setErrorInfo(errorInfo);
    }

    public static void main(String[] agvr) {
        ExtendedCommonException e = new ExtendedCommonException();
        System.out.println( e.getErrorInfo() );
        e.setErrorInfo(ErrorInfo.UnknownError);
        System.out.println( e.getErrorInfo() );
    }
}
