package com.abs.casino.kafka.dto;

import com.dgphoenix.casino.kafka.dto.KafkaRequest;

public class PingRequest implements KafkaRequest {
    private final String ping = "ping";

    public String getPing() {
        return ping;
    }
}
