package com.abs.casino.kafka.dto;

import com.abs.casino.kafka.dto.KafkaRequest;

public class GetGameServersInfoRequest implements KafkaRequest {
    private final String get = "serversInfo";

    public String getGet() {
        return get;
    }
}
