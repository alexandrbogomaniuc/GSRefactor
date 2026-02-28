package com.abs.casino.common.exception;

/**
 * Created by inter on 23.04.15.
 */
public enum ErrorInfo {
    UnknownError(0, "Unknown error");
    protected int code;
    protected String messege;

    ErrorInfo(int code, String messege) {
        this.code = code;
        this.messege = messege;
    }

    public String toString() {
        return messege + " (" + code + ")";
    }
}
