package com.abs.casino.kafka.dto.privateroom.request;

import com.abs.casino.kafka.dto.BGOnlinePlayerDto;
import com.abs.casino.kafka.dto.KafkaRequest;

import java.util.List;

public class UpdateOnlinePlayersDto implements KafkaRequest {
    private List<BGOnlinePlayerDto> onlinePlayers;

    public UpdateOnlinePlayersDto(){}

    public UpdateOnlinePlayersDto(List<BGOnlinePlayerDto> onlinePlayers) {
        this.onlinePlayers = onlinePlayers;
    }

    public List<BGOnlinePlayerDto> getOnlinePlayers() {
        return onlinePlayers;
    }

    public void setOnlinePlayers(List<BGOnlinePlayerDto> onlinePlayers) {
        this.onlinePlayers = onlinePlayers;
    }
}
