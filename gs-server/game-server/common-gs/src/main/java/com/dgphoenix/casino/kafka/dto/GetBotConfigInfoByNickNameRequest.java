package com.abs.casino.kafka.dto;

import com.abs.casino.kafka.dto.KafkaRequest;

public class GetBotConfigInfoByNickNameRequest implements KafkaRequest {
    private String nickname;

    public GetBotConfigInfoByNickNameRequest() {}

    public GetBotConfigInfoByNickNameRequest(String nickname) {
        this.setNickname(nickname);
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
}
