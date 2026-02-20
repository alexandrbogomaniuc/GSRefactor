package com.betsoft.casino.mp.model.friends;

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
