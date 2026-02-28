package com.abs.casino.kafka.dto;

import java.util.List;

public class GetOnlineStatusRequest implements KafkaRequest {
    private List<BGOnlinePlayerDto> onlinePlayers;

    public GetOnlineStatusRequest() {}

    public GetOnlineStatusRequest(List<BGOnlinePlayerDto> onlinePlayers) {
        this.onlinePlayers = onlinePlayers;
    }

    public List<BGOnlinePlayerDto> getOnlinePlayers() {
        return onlinePlayers;
    }

    public void setOnlinePlayers(List<BGOnlinePlayerDto> onlinePlayers) {
        this.onlinePlayers = onlinePlayers;
    }
}
