package com.dgphoenix.casino.common.client.canex.request.friends;

public enum Status {
    sent,
    received,
    friend,
    rejected,
    blocked;

    public static Status fromStringToStatus(final String value) {
        return Status.valueOf(value.toLowerCase());
    }
}
