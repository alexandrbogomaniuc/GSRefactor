package com.abs.casino.kafka.dto;

import com.abs.casino.kafka.dto.KafkaRequest;
import com.abs.casino.kafka.dto.BGOnlinePlayerDto;

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
