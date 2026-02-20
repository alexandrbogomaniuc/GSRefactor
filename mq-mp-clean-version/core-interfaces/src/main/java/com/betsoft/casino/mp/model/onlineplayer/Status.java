package com.betsoft.casino.mp.model.onlineplayer;

public enum Status {
    online,
    offline;

    public static Status fromStringToStatus(final String value) {
        return Status.valueOf(value.toLowerCase());
    }

}
