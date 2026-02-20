package com.betsoft.casino.mp.model;

/**
 * User: flsh
 * Date: 03.11.18.
 */
public class SitOutResult implements ISitOutResult {
    public boolean success;
    public int errorCode;
    public String errorDetails;

    public SitOutResult(boolean success, int errorCode, String errorDetails) {
        this.success = success;
        this.errorCode = errorCode;
        this.errorDetails = errorDetails;
    }

    @Override
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    @Override
    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    @Override
    public String getErrorDetails() {
        return errorDetails;
    }

    public void setErrorDetails(String errorDetails) {
        this.errorDetails = errorDetails;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SitOutResult [");
        sb.append("success=").append(success);
        sb.append(", errorCode=").append(errorCode);
        sb.append(", errorDetails='").append(errorDetails).append('\'');
        sb.append(']');
        return sb.toString();
    }
}
