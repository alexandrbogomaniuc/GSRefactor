package com.dgphoenix.casino.kafka.dto;

public class PingRequest implements KafkaRequest {
    private final String ping = "ping";

    public String getPing() {
        return ping;
    }
}
