package com.abs.casino.kafka.dto.privateroom.request;

public enum StatusOnlinePlayer {
    online(0),
    offline(1);

    private final int value;

    StatusOnlinePlayer(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static StatusOnlinePlayer findByValue(int value){
        switch (value) {
            case 0:
                return online;
            case 1:
                return offline;
            default:
                return null;
        }
    }

    public static StatusOnlinePlayer findByValue(boolean online) {
        if (online) {
            return StatusOnlinePlayer.online;
        } else {
            return StatusOnlinePlayer.offline;
        }
    }
}
