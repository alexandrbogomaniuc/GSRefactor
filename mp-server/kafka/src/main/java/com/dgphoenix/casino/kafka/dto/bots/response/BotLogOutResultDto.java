package com.dgphoenix.casino.kafka.dto.bots.response;

import com.dgphoenix.casino.kafka.dto.BasicKafkaResponse;

public class BotLogOutResultDto extends BasicKafkaResponse {

    public BotLogOutResultDto(){}

    public BotLogOutResultDto(boolean success, int statusCode, String reasonPhrases) {
        super(success, statusCode, reasonPhrases);
    }

}
