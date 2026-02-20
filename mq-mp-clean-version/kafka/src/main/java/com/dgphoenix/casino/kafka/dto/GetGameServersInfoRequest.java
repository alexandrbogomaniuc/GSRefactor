package com.dgphoenix.casino.kafka.dto;

public class GetGameServersInfoRequest implements KafkaRequest {
    private final String get = "serversInfo";

    public String getGet() {
        return get;
    }
}
