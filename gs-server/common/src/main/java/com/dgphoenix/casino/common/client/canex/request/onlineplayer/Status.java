package com.abs.casino.common.client.canex.request.onlineplayer;

public enum Status {
    online,
    offline;

    public static Status fromStringToStatus(final String value) {
        return Status.valueOf(value.toLowerCase());
    }

    public static boolean isOnline(final String value) {
        return Status.fromStringToStatus(value) == Status.online;
    }
}
