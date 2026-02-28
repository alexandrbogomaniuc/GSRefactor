package com.abs.casino.kafka.dto.bots.request;

import com.abs.casino.kafka.dto.KafkaRequest;

public class GetDetailBotInfoRequest implements KafkaRequest {
    private long botId;
    private String botNickName;

    public GetDetailBotInfoRequest() {
        super();
    }

    public GetDetailBotInfoRequest(long botId, String botNickName) {
        this.botId = botId;
        this.botNickName = botNickName;
    }

    public long getBotId() {
        return botId;
    }

    public void setBotId(long botId) {
        this.botId = botId;
    }

    public String getBotNickName() {
        return botNickName;
    }

    public void setBotNickName(String botNickName) {
        this.botNickName = botNickName;
    }

}
