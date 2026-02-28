package com.dgphoenix.casino.kafka.dto;

import com.dgphoenix.casino.kafka.dto.KafkaRequest;

public class GetBotConfigInfoByUserNameRequest implements KafkaRequest {
    private String username;

    public GetBotConfigInfoByUserNameRequest() {}

    public GetBotConfigInfoByUserNameRequest(String username) {
        this.setUsername(username);
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
