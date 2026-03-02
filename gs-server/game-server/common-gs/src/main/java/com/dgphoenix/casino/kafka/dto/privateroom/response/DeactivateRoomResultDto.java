package com.abs.casino.kafka.dto.privateroom.response;

import com.abs.casino.kafka.dto.BasicKafkaResponse;

public class DeactivateRoomResultDto extends BasicKafkaResponse {
    public DeactivateRoomResultDto(){}

    public DeactivateRoomResultDto(boolean success, int statusCode, String reasonPhrases) {
        super(success, statusCode, reasonPhrases);
    }
}
