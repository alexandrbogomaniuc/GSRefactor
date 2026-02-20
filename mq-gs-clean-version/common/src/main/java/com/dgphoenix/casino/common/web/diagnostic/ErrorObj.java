package com.dgphoenix.casino.common.web.diagnostic;

import java.util.Objects;

public class ErrorObj {
    private final String errorMessage;
    private final String className;
    private final long datetime;

    public ErrorObj(String errorMessage, String className) {
        this.errorMessage = errorMessage;
        this.className = className;
        this.datetime = System.currentTimeMillis();
    }

    public long getDatetime() {
        return datetime;
    }

    public String getClassName() {
        return className;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ErrorObj errorObj = (ErrorObj) o;
        return datetime == errorObj.datetime &&
                Objects.equals(errorMessage, errorObj.errorMessage) &&
                Objects.equals(className, errorObj.className);
    }

    @Override
    public int hashCode() {
        return Objects.hash(errorMessage, className, datetime);
    }
}
