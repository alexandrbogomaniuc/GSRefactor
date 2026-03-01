package com.abs.casino.gs.managers.payment.wallet;

import java.io.Serializable;

/**
 * User: flsh
 * Date: Jun 29, 2010
 */
public class CWError implements Serializable {
    private final int code;
    private final String description;
    private final boolean sendErrorMessageToClient;
    private final boolean cancelOperation;

    public CWError(int code, String description) {
        this.code = code;
        this.description = description;
        this.sendErrorMessageToClient = false;
        this.cancelOperation = false;
    }

    public CWError(int code, String description, boolean sendErrorMessageToClient, boolean cancelOperation) {
        this.code = code;
        this.description = description;
        this.sendErrorMessageToClient = sendErrorMessageToClient;
        this.cancelOperation = cancelOperation;
    }

    public static CWError fromPrototype(CWError prototype, boolean sendErrorMessageToClient, boolean cancelOperation) {
        return new CWError(prototype.getCode(), prototype.getDescription(), sendErrorMessageToClient, cancelOperation);
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public boolean needSendErrorMessageToClient() {
        return sendErrorMessageToClient;
    }

    public boolean needCancelOperation() {
        return cancelOperation;
    }

    @Override
    public String toString() {
        return "CWError[" +
                "code=" + code +
                ", description='" + description + '\'' +
                ", sendErrorMessageToClient=" + sendErrorMessageToClient +
                ", cancelOperation=" + cancelOperation +
                ']';
    }
}
