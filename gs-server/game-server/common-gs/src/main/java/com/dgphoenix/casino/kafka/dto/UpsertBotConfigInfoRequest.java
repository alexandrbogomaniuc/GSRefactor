package com.dgphoenix.casino.kafka.dto;

import java.util.List;

public class UpsertBotConfigInfoRequest implements KafkaRequest {
    private List<BotConfigInfoDto> botConfigInfos;

    public UpsertBotConfigInfoRequest() {}

    public UpsertBotConfigInfoRequest(List<BotConfigInfoDto> botConfigInfos) {
        this.setBotConfigInfos(botConfigInfos);
    }

    public List<BotConfigInfoDto> getBotConfigInfos() {
        return botConfigInfos;
    }

    public void setBotConfigInfos(List<BotConfigInfoDto> botConfigInfos) {
        this.botConfigInfos = botConfigInfos;
    }
}
