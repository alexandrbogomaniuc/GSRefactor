package com.abs.casino.kafka.dto;

import com.dgphoenix.casino.kafka.dto.KafkaRequest;

public class GetBotConfigInfoRequest implements KafkaRequest {
    private long botId;

    public GetBotConfigInfoRequest() {}

    public GetBotConfigInfoRequest(long botId) {
        this.setBotId(botId);
    }

    public long getBotId() {
        return botId;
    }

    public void setBotId(long botId) {
        this.botId = botId;
    }
}
