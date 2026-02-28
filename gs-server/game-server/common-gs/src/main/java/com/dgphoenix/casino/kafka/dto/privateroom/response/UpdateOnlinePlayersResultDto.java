package com.abs.casino.kafka.dto.privateroom.response;

import java.util.List;

import com.abs.casino.kafka.dto.BGOnlinePlayerDto;
import com.dgphoenix.casino.kafka.dto.BasicKafkaResponse;

public class UpdateOnlinePlayersResultDto extends BasicKafkaResponse {
    private List<BGOnlinePlayerDto> onlinePlayers;

    public UpdateOnlinePlayersResultDto(){}

    public UpdateOnlinePlayersResultDto(boolean success, int statusCode, String reasonPhrases) {
        super(success, statusCode, reasonPhrases);
    }

    public UpdateOnlinePlayersResultDto(List<BGOnlinePlayerDto> onlinePlayers) {
        this.onlinePlayers = onlinePlayers;
    }

    public UpdateOnlinePlayersResultDto(boolean success, int statusCode, String reasonPhrases, List<BGOnlinePlayerDto> onlinePlayers) {
        super(success, statusCode, reasonPhrases);
        this.onlinePlayers = onlinePlayers;
    }

    public List<BGOnlinePlayerDto> getOnlinePlayers() {
        return onlinePlayers;
    }

    public void setOnlinePlayers(List<BGOnlinePlayerDto> onlinePlayers) {
        this.onlinePlayers = onlinePlayers;
    }

    @Override
    public String toString() {
        return "UpdateOnlinePlayersResultDto{" +
                "onlinePlayers=" + onlinePlayers +
                '}';
    }
}