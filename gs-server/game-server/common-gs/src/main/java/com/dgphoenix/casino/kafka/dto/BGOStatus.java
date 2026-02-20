package com.dgphoenix.casino.kafka.dto;

public enum BGOStatus {
    online(0), offline(1);

    private final int value;

    private BGOStatus(int value) {
        this.value = value;
    }

    /**
     * Get the integer value of this enum value, as defined in the Thrift IDL.
     */
    public int getValue() {
        return value;
    }
}
