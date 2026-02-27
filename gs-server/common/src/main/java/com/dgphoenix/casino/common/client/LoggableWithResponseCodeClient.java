package com.abs.casino.common.client;


import com.dgphoenix.casino.gs.managers.payment.wallet.ILoggableResponseCode;

public class LoggableWithResponseCodeClient extends AbstractLoggableClient implements ILoggableResponseCode {
    @Override
    public void logResponseHTTPCode(Integer code) {
        if (loggableContainer instanceof ILoggableResponseCode) {
            log(() -> ((ILoggableResponseCode)loggableContainer).logResponseHTTPCode(code));
        } else if (loggableContainer != null){
            throw new UnsupportedOperationException("Operation unsupported by container");
        }

    }

    @Override
    public Integer getResponseHTTPCode() {
        if (loggableContainer instanceof ILoggableResponseCode) {
            return ((ILoggableResponseCode)loggableContainer).getResponseHTTPCode();
        } else if (loggableContainer == null) {
            return null;
        }
        throw new UnsupportedOperationException("Operation unsupported by container");
    }
}
