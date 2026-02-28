package com.abs.casino.kafka.dto;

import com.dgphoenix.casino.kafka.dto.BasicKafkaResponse;

import java.util.Set;

public class GameServerInfoResponseDto extends BasicKafkaResponse {
    private Set<GameServerInfoDto> infos;

    public GameServerInfoResponseDto() {}

    public GameServerInfoResponseDto(Set<GameServerInfoDto> infos, boolean success, int statusCode, String reasonPhrases) {
        super(success, statusCode, reasonPhrases);
        this.infos = infos;
    }

    public GameServerInfoResponseDto(boolean success, int statusCode, String reasonPhrases) {
        super(success, statusCode, reasonPhrases);
    }

    public GameServerInfoResponseDto(Set<GameServerInfoDto> infos) {
        super(true, 0, "");
        this.infos = infos;
    }

    public Set<GameServerInfoDto> getInfos() {
        return infos;
    }

    public void setInfos(Set<GameServerInfoDto> infos) {
        this.infos = infos;
    }


}