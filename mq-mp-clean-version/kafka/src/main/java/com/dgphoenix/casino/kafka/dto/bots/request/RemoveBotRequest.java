package com.dgphoenix.casino.kafka.dto.bots.request;

import com.dgphoenix.casino.kafka.dto.KafkaRequest;

public class RemoveBotRequest implements KafkaRequest {
    private long botId;
    private String botNickName;
    private long roomId;

    public RemoveBotRequest() {}

    public RemoveBotRequest(long botId,
            String botNickName,
            long roomId) {
        super();
        this.botId = botId;
        this.botNickName = botNickName;
        this.roomId = roomId;
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

    public long getRoomId() {
        return roomId;
    }

    public void setRoomId(long roomId) {
        this.roomId = roomId;
    }

}
