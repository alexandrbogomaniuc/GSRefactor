package com.dgphoenix.casino.kafka.dto.bots.request;

import com.dgphoenix.casino.kafka.dto.KafkaRequest;

public class ConfirmNextRoundBuyInRequest implements KafkaRequest {
    private long botId;
    private String sessionId;
    private String botNickName;
    private long roomId;
    private long roundId;

    public ConfirmNextRoundBuyInRequest() {}

    public ConfirmNextRoundBuyInRequest(long botId,
            String sessionId,
            String botNickName,
            long roomId,
            long roundId) {
        super();
        this.botId = botId;
        this.sessionId = sessionId;
        this.botNickName = botNickName;
        this.roomId = roomId;
        this.roundId = roundId;
    }

    public long getBotId() {
        return botId;
    }

    public void setBotId(long botId) {
        this.botId = botId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
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

    public long getRoundId() {
        return roundId;
    }

    public void setRoundId(long roundId) {
        this.roundId = roundId;
    }


}
