package com.abs.casino.controller.frbonus.transport;

import java.io.Serializable;

public class AbstractResponse implements Serializable {
    protected final boolean success;

    public AbstractResponse(boolean success) {
        this.success = success;
    }

    public boolean isSuccess() {
        return success;
    }
}