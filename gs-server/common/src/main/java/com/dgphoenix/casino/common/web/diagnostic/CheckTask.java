package com.dgphoenix.casino.common.web.diagnostic;

public abstract class CheckTask {
    protected String errorMessage;
    protected boolean warning;

    public CheckTask(String errorMessage, boolean warning) {
        this.errorMessage = errorMessage;
        this.warning = warning;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public boolean isWarning() {
        return warning;
    }

    public abstract boolean isOut(boolean strongValidation);
}
