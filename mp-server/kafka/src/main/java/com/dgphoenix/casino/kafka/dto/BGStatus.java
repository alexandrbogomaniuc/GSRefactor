package com.abs.casino.kafka.dto;

public enum BGStatus {
    invited(0),
    accepted(1),
    rejected(2),
    kicked(3),
    loading(4),
    ready(5),
    waiting(6),
    playing(7);

    private final int value;

    private BGStatus(int value) {
      this.value = value;
    }

    public int getValue() {
      return value;
    }
}
