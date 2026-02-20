package com.dgphoenix.casino.kafka.dto;

public enum BGFStatus {
    sent(0),
    received(1),
    friend(2),
    rejected(3),
    blocked(4);

    private final int value;

    private BGFStatus(int value) {
      this.value = value;
    }

    public int getValue() {
      return value;
    }
}
