package com.abs.casino.kafka.dto;

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
