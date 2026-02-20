package com.dgphoenix.casino.common.client.canex.request.privateroom;

public enum Status {
    INVITED,
    ACCEPTED,
    REJECTED,
    KICKED,
    LOADING,
    READY,
    WAITING,
    PLAYING;

    public static Status fromStringToStatus(final String value) {
        return Status.valueOf(value.toUpperCase());
    }
}
