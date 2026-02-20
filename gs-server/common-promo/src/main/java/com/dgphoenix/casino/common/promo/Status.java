package com.dgphoenix.casino.common.promo;

/**
 * User: flsh
 * Date: 15.11.16.
 */
public enum Status {
    READY(true),
    STARTED(true),
    QUALIFICATION(false),
    FINISHED(false),
    CANCELLED(false),
    PENDING(false);

    private boolean actual;

    Status(boolean actual) {
        this.actual = actual;
    }

    public boolean isActual() {
        return actual;
    }
}
