package com.abs.casino.kafka.dto;

import com.abs.casino.kafka.dto.KafkaRequest;
import java.util.List;

public class RemoveBotConfigInfoRequest implements KafkaRequest {
    private List<Long> botIds;

    public RemoveBotConfigInfoRequest() {}

    public RemoveBotConfigInfoRequest(List<Long> botIds) {
        this.setBotIds(botIds);
    }

    public List<Long> getBotIds() {
        return botIds;
    }

    public void setBotIds(List<Long> botIds) {
        this.botIds = botIds;
    }
}
