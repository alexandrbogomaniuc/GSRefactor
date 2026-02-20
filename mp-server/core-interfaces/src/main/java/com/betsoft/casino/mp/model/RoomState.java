package com.betsoft.casino.mp.model;

/**
 * User: flsh
 * Date: 21.09.17.
 */
public enum RoomState {
    WAIT,
    PLAY,
    QUALIFY,
    CLOSED,
    PAUSE,
    BUY_IN;

    public static RoomState valueOf(int ordinal) {
        return values()[ordinal];
    }
}
