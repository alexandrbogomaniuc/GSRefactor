package com.abs.casino.common.exception;

import com.abs.casino.common.exception.CommonException;

/**
 * User: van0ss
 * Date: 09.09.2016
 */
public class MaintenanceModeException extends CommonException {
    private StartParameters parameters;

    public MaintenanceModeException() {
        super();
    }

    public MaintenanceModeException(String message) {
        super(message);
    }

    public MaintenanceModeException(String message, StartParameters parameters) {
        super(message);
        this.parameters = parameters;
    }

    public StartParameters getParameters() {
        return parameters;
    }

    public void setParameters(StartParameters parameters) {
        this.parameters = parameters;
    }

    public MaintenanceModeException(Throwable cause) {
        super(cause);
    }

    public MaintenanceModeException(String message, Throwable cause) {
        super(message, cause);
    }
}
